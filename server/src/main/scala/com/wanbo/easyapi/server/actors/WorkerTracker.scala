package com.wanbo.easyapi.server.actors

import akka.actor.Actor
import com.wanbo.easyapi.server.messages.{ListenerFailed, ListenerRunning, ShutDown, StartUp}

/**
 * Worker tracker
 * Created by wanbo on 15/4/3.
 */
class WorkerTracker extends Actor {

    override def receive: Receive = {
        case StartUp =>
            println("Starting up ...")
        case ListenerRunning(conf, workers) =>
            println("Listener is running ...")
            conf.workersPort.foreach(port => {
                var isWorking = false

                workers.children.foreach(worker => {

                    if(worker.toString().contains("watcher_" + port))
                        isWorking = true

                })

                if(isWorking)
                    println("Port --" + port + " is working")
                else
                    println("Port --" + port + " isn't working")
            })

//            Thread.sleep(3000)

//            self ! ListenerRunning(ports, workers)

        case ListenerFailed =>
            println("Listener starting failed ...")
            context.system.shutdown()
        case ShutDown(msg) =>
            println("Shutting down ... ")
            if(msg != null)
                println(msg)

            Thread.sleep(3000)

            println("Shutting down main process ... ")
            context.system.shutdown()
            System.exit(-1)
    }
}
