package com.wanbo.easyapi.server.actors

import java.net.ServerSocket

import akka.actor.{Props, Actor}
import com.wanbo.easyapi.server.messages._

/**
 * Listener
 *
 * Created by wanbo on 15/4/3.
 */
class SeedWatcher extends Actor {

    var socket: ServerSocket = _
    var isClose: Boolean = false

    val worker = context.actorOf(Props[Worker], name = "worker")

    override def receive: Receive = {

        case ListenerWorkerStart(port: Int) =>

            println("worker listening on %s".format(port))

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

            if(!isClose && socket == null)
                socket = new ServerSocket(port)

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
