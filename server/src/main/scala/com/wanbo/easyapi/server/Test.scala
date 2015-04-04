package com.wanbo.easyapi.server

import akka.actor.{Props, ActorSystem}
import com.wanbo.easyapi.server.actors.{WorkerTracker, Manager}
import com.wanbo.easyapi.server.messages.StartUp

/**
 * Test
 * Created by wanbo on 15/4/3.
 */
object Test {
    def main(args: Array[String]) {

        val system = ActorSystem("System")

        val workTracker = system.actorOf(Props[WorkerTracker], name = "work_tracker")

        val manager = system.actorOf(Props(new Manager(workTracker)), name = "manager")

        manager ! StartUp

//        system.shutdown()
    }
}
