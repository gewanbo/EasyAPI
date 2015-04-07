package com.wanbo.easyapi.server.actors

import java.util.Properties

import akka.actor.{Props, ActorRef, Actor}
import com.wanbo.easyapi.server.messages._

/**
 * The controller of watchers
 * Created by wanbo on 15/4/3.
 */
class WatcherController(_conf: Properties, manager: ActorRef) extends Actor {

    val managerWatcher = context.actorOf(Props(new ManagerWatcher(manager)), name = "manager_watcher")
    var seedWatcherBox = List[ActorRef]()

    println(_conf)

    override def receive: Receive = {
        case ListenerStart(conf) =>
            println("Starting up listeners ...")

            managerWatcher ! ListenerManagerStart(conf)

            val workers_port = conf.getProperty("server.worker.port", "8801")

            val workerPorts = workers_port.split(";")

            workerPorts.foreach(port => {
                val seedWatcher = context.actorOf(Props[SeedWatcher], name = "seed_watcher_" + port)
                seedWatcher ! ListenerWorkerStart(port.toInt)
                seedWatcherBox = seedWatcherBox :+ seedWatcher
            })

            sender() ! ListenerRunning

        case ListenerFailed =>
            manager ! ListenerFailed

        case WatcherStop(conf) =>
            println("Stopping listeners ...")
            managerWatcher ! ListenerManagerStop
            seedWatcherBox.foreach(watcher => {
                watcher ! ListenerWorkerStop
            })
            context.stop(self)
    }
}
