package com.wanbo.easyapi.server.actors

import java.net.ServerSocket

import akka.actor.{ActorRef, Actor}
import com.wanbo.easyapi.server.lib.EasyConfig
import com.wanbo.easyapi.server.messages._
import org.slf4j.LoggerFactory

/**
 * Listener
 *
 * Created by wanbo on 15/4/3.
 */
class ManagerWatcher(conf: EasyConfig,manager: ActorRef) extends Actor {

    var socket: ServerSocket = _
    var isClose: Boolean = false

    private val log = LoggerFactory.getLogger(classOf[ManagerWatcher])

    override def receive: Receive = {

        case ListenerManagerStart =>
            // Manager watcher
            val stat = managerListen(conf.serverPort)

            if(!stat)
                manager ! ListenerFailed
            else
                self ! ListenerManagerProcess

        case ListenerManagerProcess =>
            if(!isClose) {
                managerListen(8800)
                self ! ListenerManagerProcess
            }

        case ListenerManagerStop =>
            isClose = true
            socket.close()
            context.stop(self)

    }

    def managerListen(port: Int): Boolean ={
        var ret = true

        try {

            if(isClose) {
                if(!socket.isClosed)
                    socket.close()
                throw new Exception("Socket has closed.")
            }

            if(!isClose && socket == null) {
                socket = new ServerSocket(port)
            }

            val client = socket.accept()
            if (client != null) {
                manager ! ManagerCommand(client)
            } else {
                log.warn("Manager watcher: The socket client was null.")
            }
        } catch {
            case e: Exception =>
                log.error("Manager watcher exception:", e)
                ret = false
        }

        ret
    }
}
