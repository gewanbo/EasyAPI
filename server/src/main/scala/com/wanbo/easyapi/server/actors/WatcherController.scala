package com.wanbo.easyapi.server.actors

import akka.actor.{Props, ActorRef, Actor}
import com.wanbo.easyapi.server.messages._

/**
 * The controller of watchers
 * Created by wanbo on 15/4/3.
 */
class WatcherController(manager: ActorRef) extends Actor {

    val managerWatcher = context.actorOf(Props(new ManagerWatcher(manager)), name = "manager_watcher")
    val seedWatcher = context.actorOf(Props[SeedWatcher], name = "seed_watcher")

    override def receive: Receive = {
        case ListenerStart(conf) =>
            println("Starting up listeners ...")

            managerWatcher ! ListenerManagerStart(conf)
            seedWatcher ! ListenerWorkerStart(conf)

            sender() ! ListenerRunning

        case WatcherStop(conf) =>
            println("Stopping listeners ...")
            managerWatcher ! ListenerManagerStop
            seedWatcher ! ListenerWorkerStop
            context.stop(self)
    }
}
