package com.wanbo.easyapi.server

import akka.actor.{Props, ActorSystem}
import com.wanbo.easyapi.server.actors.{Manager, WorkerTracker}
import com.wanbo.easyapi.server.messages.StartUp

/**
 * The server of EasyAPI
 * Created by wanbo on 15/3/30.
 */
object EasyServer {

    var port = "8800"

    def main(args: Array[String]) {

        val system = ActorSystem("System")

        val workTracker = system.actorOf(Props[WorkerTracker], name = "work_tracker")

        val manager = system.actorOf(Props(new Manager(workTracker)), name = "manager")

        manager ! StartUp

    }
}