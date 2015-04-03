package com.wanbo.easyapi.server.actors

import java.net.ServerSocket

import akka.actor.{ActorRef, Actor, Props}
import com.wanbo.easyapi.server.messages._

/**
 * Listener
 *
 * Created by wanbo on 15/4/3.
 */
class ManagerWatcher(manager: ActorRef) extends Actor {


    override def receive: Receive = {

        case ListenerManagerStart(conf) =>
            val server_port = conf.getProperty("server.port", "8800")

            // Manager watcher
            managerListen(server_port.toInt)

            println("manager listening on 8800")

    }

    def managerListen(port: Int): Boolean ={
        var ret = true

        try {
            val socket = new ServerSocket(port)
            var client = socket.accept()

            while (client != null) {
                manager ! ManagerCommand(client)
                client = socket.accept()
            }
        } catch {
            case e: Exception =>
                ret = false
        }

        ret
    }

}
