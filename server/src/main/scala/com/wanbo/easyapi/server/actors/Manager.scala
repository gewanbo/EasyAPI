package com.wanbo.easyapi.server.actors

import akka.actor.{ActorRef, Props, Actor}
import com.wanbo.easyapi.server.messages.{ShutDown, StartUp, Work}

/**
 * Manager
 * Created by wanbo on 15/4/3.
 */
class Manager(listener: ActorRef) extends Actor {

    val worker = context.actorOf(Props[Worker], name = "worker")

    override def receive: Receive = {
        case StartUp =>
            listener ! StartUp
        case Work =>
            worker ! Work
        case ShutDown =>
            println("Shutting down ...")
            listener ! ShutDown
            context.stop(self)
    }
}
