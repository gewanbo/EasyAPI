package com.wanbo.easyapi.server

import akka.actor.{Props, ActorSystem}
import com.wanbo.easyapi.server.actors.{Listener, Manager}
import com.wanbo.easyapi.server.messages.{StartUp, Work}

/**
 * Test
 * Created by wanbo on 15/4/3.
 */
object Test {
    def main(args: Array[String]) {

        val system = ActorSystem("System")

        val listener = system.actorOf(Props[Listener], name = "listener")

        val manager = system.actorOf(Props(new Manager(listener)), name = "manager")

        manager ! StartUp

        manager ! Work

        //system.shutdown()
    }
}
