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

    val worker = context.actorOf(Props[Worker], name = "worker")

    override def receive: Receive = {

        case ListenerWorkerStart(conf) =>
            val worker_port = conf.getProperty("server.worker.port", "8801")

            // Workers' watcher
            workerListen(worker_port.toInt)

            println("worker listening on 8801")

        case ListenerWorkerStop =>
            context.stop(worker)
            context.stop(self)
    }

    def workerListen(port: Int): Unit ={
        val socket = new ServerSocket(port)
        var client = socket.accept()

        while (client != null) {
            worker ! Seed(client)
            client = socket.accept()
        }
    }
}
