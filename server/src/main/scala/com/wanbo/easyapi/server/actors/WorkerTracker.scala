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
        case ListenerRunning =>
            println("Listener is running ...")
        case ListenerFailed =>
            println("Listener starting failed ...")
            context.system.shutdown()
        case ShutDown(msg) =>
            println("Shutting down ... ")
            if(msg != null)
                println(msg)
            context.system.shutdown()
    }
}
