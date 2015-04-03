package com.wanbo.easyapi.server.actors

import akka.actor.{ActorRef, Actor}

/**
 * The controller of watchers
 * Created by wanbo on 15/4/3.
 */
class WatcherController(manager: ActorRef) extends Actor {

    override def receive: Receive = {
        case ListenerManagerStart(conf) =>
            val server_port = conf.getProperty("server.port", "8800")

            // Manager watcher
            managerListen(server_port.toInt)

            println("manager listening on 8800")
    }
}
