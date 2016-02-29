package com.wanbo.easyapi.server.routing

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.routing._
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig

/**
 * Watcher
 * Created by wanbo on 16/2/29.
 */
class Watcher(conf: EasyConfig) extends Actor with Logging {

    import context.system

    val reSizer = DefaultResizer(lowerBound=1, upperBound = conf.workersMaxThreads)

    //val worker = context.actorOf(Props(new Worker(conf)).withRouter(RoundRobinRouter(conf.workersMaxThreads).withResizer(reSizer)), name = "worker")
    //val worker = context.actorOf(Props(new Worker(conf)).withRouter(RoundRobinPool(conf.workersMaxThreads).withResizer(reSizer)), name = "worker")
    //val worker = context.actorOf(Props(new Worker(conf)).withRouter(BalancingPool(conf.workersMaxThreads)), name = "worker")
    val worker = context.actorOf(Props(new Worker(conf)).withRouter(SmallestMailboxPool(conf.workersMaxThreads).withResizer(resizer = reSizer)), name = "worker")

    IO(Tcp) ! Bind(self, new InetSocketAddress(conf.serverHost, 10000))

    override def receive: Receive = {
        case b @ Bound(localAddress) =>
        // Bound success

        case CommandFailed(r: Bind) =>
            // Close listeners and then
            log.error("Listen failed.")
            log.error(r.localAddress.toString)

        case c @ Connected(remoteAddress, localAddress) =>
            log.info("-------------------------Connected Mark---------------")
            log.info("Child thread size:" + context.children.size)

            sender() ! Register(worker)

        case "start" =>
            log.info("Watcher starting up!")

        case "stop" =>
            context.stop(worker)
            context.stop(self)
    }
}
