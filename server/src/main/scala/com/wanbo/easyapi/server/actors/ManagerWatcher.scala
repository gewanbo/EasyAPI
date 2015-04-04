package com.wanbo.easyapi.server.actors

import java.net.ServerSocket

import akka.actor.{ActorRef, Actor}
import com.wanbo.easyapi.server.messages._

/**
 * Listener
 *
 * Created by wanbo on 15/4/3.
 */
class ManagerWatcher(manager: ActorRef) extends Actor {

    var socket: ServerSocket = _
    var isClose: Boolean = false

    override def receive: Receive = {

        case ListenerManagerStart(conf) =>
            val server_port = conf.getProperty("server.port", "8800")

            // Manager watcher
            val stat = managerListen(server_port.toInt)

            if(!stat)
                manager ! ListenerFailed
            else
                self ! ListenerManagerProcess(conf)

        case ListenerManagerProcess(conf) =>
            if(!isClose) {
                managerListen(8800)
                self ! ListenerManagerProcess(conf)
            }

        case ListenerManagerStop =>
            isClose = true
            socket.close()
            context.stop(self)

    }

    def managerListen(port: Int): Boolean ={
        var ret = true

        try {

            if(!isClose && socket == null)
                socket = new ServerSocket(port)

            val client = socket.accept()
            if (client != null) {
                manager ! ManagerCommand(client)
            }
        } catch {
            case e: Exception =>
                println(e.getMessage)
                ret = false
        }

        ret
    }
}
