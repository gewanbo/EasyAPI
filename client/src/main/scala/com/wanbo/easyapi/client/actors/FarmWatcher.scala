package com.wanbo.easyapi.client.actors

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.routing.{DefaultResizer, RoundRobinRouter}
import com.wanbo.easyapi.client.lib.{SeedStorage, WorkCounter}
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.slf4j.LoggerFactory

/**
 * Farm watcher
 * Created by wanbo on 15/8/17.
 */
class FarmWatcher(conf: EasyConfig) extends Actor {

    private val log = LoggerFactory.getLogger(classOf[FarmWatcher])

    private var _client: ActorRef = null

    val resizer = DefaultResizer(lowerBound = conf.minThreads, upperBound = conf.maxThreads)

    val farm = context.actorOf(Props(new Farm()).withRouter(RoundRobinRouter(resizer = Some(resizer))), name = "farm")

    override def receive: Receive = {
        case "StartUp" =>

            log.info("I'm starting up ...")

            // Start up work counter.
            val workCounter = new WorkCounter(conf)
            workCounter.start()

            // Start up storage cleaner.
            val seederStorage = new SeedStorage(conf)
            seederStorage.start()

            // Initialize ClientRegister
            _client = context.actorOf(Props(new ClientRegister(conf)), name = "ClientRegister")

            _client ! "StartUp"

        case "Ready" =>

            log.info("Ready to bound socket...")

            import context.system
            IO(Tcp) ! Bind(self, new InetSocketAddress(conf.clientHost, conf.clientPort))

        case b @ Bound(localAddress) =>
            // Bound success
            log.info("Socket bound successful!")

        case CommandFailed(_: Bind) =>
            // Close listeners and then
            sender() ! "Failed"

        case c @ Connected(remoteAddress, localAddress) =>

            log.info(remoteAddress.hashCode() + " connecting ...")

            sender() ! Register(farm)

        case "Failed" =>
            log.error("Socket bound failed!")
        case "ShutDown" =>
            import context.system
            IO(Tcp) ! Unbind
            context stop farm
            _client ! "ShutDown"
            context stop self
    }
}
