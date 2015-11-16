package com.wanbo.easyapi.client.actors

import java.io.FileInputStream
import java.net.InetSocketAddress
import java.util.Properties

import akka.actor.{Props, ActorRef, Actor}
import akka.io.Tcp._
import akka.io.{Tcp, IO}
import akka.routing.{RoundRobinRouter, DefaultResizer}
import com.wanbo.easyapi.client.lib.WorkCounter
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.slf4j.LoggerFactory

/**
 * Farm watcher
 * Created by wanbo on 15/8/17.
 */
class FarmWatcher extends Actor {

    private val log = LoggerFactory.getLogger(classOf[FarmWatcher])

    private val _conf = new EasyConfig
    private var _client: ActorRef = null

    val resizer = DefaultResizer(lowerBound=1, upperBound = 10)

    val farm = context.actorOf(Props(new Farm()).withRouter(RoundRobinRouter(resizer = Some(resizer))), name = "farm")

    override def receive: Receive = {
        case "StartUp" =>

            log.info("I'm starting up ...")

            // Initialize configuration
            val confProps = new Properties()
            val configFile = System.getProperty("easy.conf", "config.properties")
            confProps.load(new FileInputStream(configFile))

            // Load configuration
            _conf.parseClientConf(confProps)

            // Start up work counter.
            val workCounter = new WorkCounter(_conf)
            workCounter.start()

            // Initialize ClientRegister
            _client = context.actorOf(Props(new ClientRegister(_conf)), name = "ClientRegister")

            _client ! "StartUp"

        case "Ready" =>

            log.info("Ready to bound socket...")

            import context.system
            IO(Tcp) ! Bind(self, new InetSocketAddress(_conf.clientHost, _conf.clientPort))

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
