package com.wanbo.easyapi.server.actors

import akka.actor.Actor
import com.wanbo.easyapi.server.messages.{ShutDown, Work}

/**
 * Worker
 * Created by wanbo on 15/4/3.
 */
class Worker extends Actor {
    override def receive: Receive = {
        case Work =>
            println("I'm working ...")
            sender() ! ShutDown
    }
}
