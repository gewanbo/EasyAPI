package com.wanbo.easyapi.server.actors

import akka.actor.{Props, Actor}
import com.wanbo.easyapi.server.messages._
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.slf4j.{MDC, LoggerFactory}

/**
 * Worker tracker
 * Created by wanbo on 15/4/3.
 */
class WorkerTracker extends Actor {

    private val log = LoggerFactory.getLogger(classOf[WorkerTracker])

    var _conf: EasyConfig = null

    MDC.put("destination", "system")

    override def receive: Receive = {
        case StartUp =>
            log.info("Starting up ...")
        case ListenerRunning(conf, workers) =>
            log.info("Listener is running ...")

            _conf = conf

            conf.workersPort.foreach(port => {
                var isWorking = false

                workers.children.foreach(worker => {

                    if(worker.toString().contains("watcher_" + port))
                        isWorking = true

                })

                if(isWorking)
                    log.info("Port - " + port + " is working")
                else
                    log.info("Port - " + port + " isn't working")
            })

            if(conf.serverMode == "Cluster" && conf.zkEnable) {
                val workerUpdate = context.actorOf(Props(new WorkerRegister(conf)), "worker_register")
                workerUpdate ! ""
                val cacheLeader = context.actorOf(Props(new ClusterLeader(conf)), "cache_leader")
                cacheLeader ! ""
            }

        case ListenerFailed =>
            log.info("Listener starting failed ...")
            context.system.shutdown()
        case ShutDown(msg) =>
            log.info("[%s] Shutting down ... ".format(classOf[WorkerTracker]))

            if(msg != null)
                log.info(msg)

            if(_conf.zkEnable){
                context.child("worker_register").get ! "shutdown"
                context.child("cache_leader").get ! "shutdown"
            }

            Thread.sleep(3000)

            log.info("Shutting down main process ... ")
            context.system.shutdown()
            System.exit(-1)
    }
}
