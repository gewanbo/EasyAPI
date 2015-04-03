package com.wanbo.easyapi.server.actors

import akka.actor.Actor
import com.wanbo.easyapi.server.messages.{ShutDown, StartUp}

/**
 * Worker tracker
 * Created by wanbo on 15/4/3.
 */
class WorkerTracker extends Actor {
    override def receive: Receive = {
        case StartUp =>
            println("Starting up ...")
        case ShutDown =>
            context.system.shutdown()
    }
}
