package com.wanbo.easyapi.server.actors

import java.net.ServerSocket

import akka.actor.{Props, Actor}
import com.wanbo.easyapi.server.messages.{Seed, ListenerRunning, ListenerStart, ShutDown}

/**
 * Listener
 *
 * Created by wanbo on 15/4/3.
 */
class SeedWatcher extends Actor {

    val worker = context.actorOf(Props[Worker], name = "worker")

    override def receive: Receive = {
        case ListenerStart =>
            println("Starting up listeners ...")
            listenPort()
            sender() ! ListenerRunning
        case ShutDown =>
            context.system.shutdown()
    }

    def listenPort(): Unit ={
        val socket = new ServerSocket(8800)
        val client = socket.accept()

        while (client != null) {
            worker ! Seed(client)
        }
    }
}
