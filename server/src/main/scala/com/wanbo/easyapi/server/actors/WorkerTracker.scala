package com.wanbo.easyapi.server.actors

import akka.actor.Actor
import com.wanbo.easyapi.server.lib.{ZooKeeperManager, ZookeeperClient}
import com.wanbo.easyapi.server.messages.{ListenerFailed, ListenerRunning, ShutDown, StartUp}
import org.slf4j.LoggerFactory

/**
 * Worker tracker
 * Created by wanbo on 15/4/3.
 */
class WorkerTracker extends Actor {

    private val log = LoggerFactory.getLogger(classOf[WorkerTracker])
    
    override def receive: Receive = {
        case StartUp =>
            log.info("Starting up ...")
        case ListenerRunning(conf, workers) =>
            log.info("Listener is running ...")
            conf.workersPort.foreach(port => {
                var isWorking = false

                workers.children.foreach(worker => {

                    if(worker.toString().contains("watcher_" + port))
                        isWorking = true

                })

                if(isWorking)
                    log.info("Port --" + port + " is working")
                else
                    log.info("Port --" + port + " isn't working")
            })

//            Thread.sleep(3000)

//            self ! ListenerRunning(ports, workers)

            val workerList = conf.workersPort.map(port => conf.serverHost + ":" + port)
            val zkManager = new ZooKeeperManager(conf.zkHosts)

            zkManager.registerWorkers(workerList)

        case ListenerFailed =>
            log.info("Listener starting failed ...")
            context.system.shutdown()
        case ShutDown(msg) =>
            log.info("Shutting down ... ")
            if(msg != null)
                log.info(msg)

            Thread.sleep(3000)

            log.info("Shutting down main process ... ")
            context.system.shutdown()
            System.exit(-1)
    }
}
