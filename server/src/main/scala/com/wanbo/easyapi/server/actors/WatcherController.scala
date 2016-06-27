package com.wanbo.easyapi.server.actors

import akka.actor.{Props, ActorRef, Actor}
import com.wanbo.easyapi.server.messages._
import com.wanbo.easyapi.shared.common.libs.EasyConfig

/**
 * The controller of watchers
 * Created by wanbo on 15/4/3.
 */
class WatcherController(conf: EasyConfig, manager: ActorRef) extends Actor {

    val managerWatcher = context.actorOf(Props(new ManagerWatcher(conf, manager)), name = "manager_watcher")
    var seedWatcherBox = List[ActorRef]()

    override def receive: Receive = {
        case ListenerStart =>
            println("Starting up listeners ...")

            managerWatcher ! ListenerManagerStart

            try {

                if (conf.workersPort.nonEmpty) {

                    conf.workersPort.foreach(port => {
                        val seedWatcher = context.actorOf(Props(new SeedWatcher(conf, port)), name = "seed_watcher_" + port)
                        seedWatcherBox = seedWatcherBox :+ seedWatcher
                    })

                    manager ! ListenerRunning(null, context)
                } else {
                    manager ! ListenerFailed
                }
            } catch {
                case e: Exception =>
                    manager ! ListenerFailed
            }

        case ListenerFailed =>
            manager ! ListenerFailed

        case WatcherStop =>
            println("Stopping listeners ...")
            managerWatcher ! ListenerManagerStop
            seedWatcherBox.foreach(watcher => {
                watcher ! ListenerWorkerStop
            })
            context.stop(self)
    }
}
