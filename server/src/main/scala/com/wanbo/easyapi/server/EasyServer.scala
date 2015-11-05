package com.wanbo.easyapi.server

import akka.actor.{Props, ActorSystem}
import com.wanbo.easyapi.server.actors.{Manager, WorkerTracker}
import com.wanbo.easyapi.server.messages.{ShutDown, StartUp}
import org.slf4j.LoggerFactory

/**
 * The server of EasyAPI
 * Created by wanbo on 15/3/30.
 */
object EasyServer {

    private val log = LoggerFactory.getLogger(EasyServer.getClass.getSimpleName)

    def main(args: Array[String]) {

        log.info("Starting up .........----------------------")

        val system = ActorSystem("System")

        val workTracker = system.actorOf(Props[WorkerTracker], name = "work_tracker")

        val manager = system.actorOf(Props(new Manager(workTracker)), name = "manager")

        manager ! StartUp

        Runtime.getRuntime.addShutdownHook(new Thread(){
            override def run(): Unit = {
                log.info("Shutting down ......")
                manager ! ShutDown("Shut down by kill command.")
            }
        })

    }
}