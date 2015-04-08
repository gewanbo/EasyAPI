package com.wanbo.easyapi.server.actors

import java.net.ServerSocket

import akka.actor.{Props, Actor}
import akka.routing.{DefaultResizer, RoundRobinRouter}
import com.wanbo.easyapi.server.lib.EasyConfig
import com.wanbo.easyapi.server.messages._

/**
 * Listener
 *
 * Created by wanbo on 15/4/3.
 */
class SeedWatcher(conf: EasyConfig) extends Actor {

    var socket: ServerSocket = _
    var isClose: Boolean = false

    val resizer = DefaultResizer(lowerBound=1, upperBound = conf.workersMaxThreads)

    val worker = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(resizer = Some(resizer))), name = "worker")

    override def receive: Receive = {

        case ListenerWorkerStart(port: Int) =>

            // Workers' watcher
            val stat = workerListen(port)

            if(!stat) {
                sender() ! ListenerFailed
            } else {
                self ! ListenerWorkProcess(port)
            }

        case ListenerWorkProcess(port) =>
            if(!isClose) {
                workerListen(port)
                self ! ListenerWorkProcess(port)
            }

        case ListenerWorkerStop =>
            isClose = true
            socket.close()
            context.stop(worker)
            context.stop(self)
    }

    def workerListen(port: Int): Boolean ={
        var ret = true

        try {

            if(!isClose && socket == null) {
                socket = new ServerSocket(port)
            }

            val client = socket.accept()
            if (client != null) {
                worker ! Seed(client)
            }
        } catch {
            case e: Exception =>
                println(e.getMessage)
                ret = false
        }

        ret
    }
}
