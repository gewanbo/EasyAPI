package com.wanbo.easyapi.server.actors

import akka.actor.Actor
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import org.apache.zookeeper.CreateMode

/**
 * The worker register.
 *
 * Register workers or re-register
 *
 * @author wanbo<gewanbo@gmail.com>
 * @param conf System configuration.
 */
class WorkerRegister(conf: EasyConfig) extends ZookeeperManager with Actor with Logging {

     private var _zk: ZookeeperClient = null

     init()

     def init(): Unit ={

         try {

             this.initNodeTree(conf)

             _zk = new ZookeeperClient(conf.zkHosts, 3000, app_root, Some(callback))

         } catch {
             case e: Exception =>
                 log.error("Error:", e)
         }
     }

     def callback(zk: ZookeeperClient): Unit ={
         zk.watchChildren(server_root, (workers: Seq[String]) =>{
             val workerList = conf.workersPort.map(x => conf.serverHost + ":" + x)

             val lostWorkers = workerList.filter(x => !workers.contains(x))

             println("sys:" + workerList.mkString("-"))
             println("current:" + workers.mkString("-"))


             if(lostWorkers.size > 0) {println("lost:" + lostWorkers.mkString("-"))
                 if(log != null) {
                     lostWorkers.foreach(log.info)
                     log.info("These workers above were lost, try to register again.")
                 }
                 lostWorkers.foreach(worker => {
                     val workerPath = server_root + "/" + worker
                     if(!zk.exists(workerPath))
                         zk.create(workerPath, "".map(_.toByte).toArray, CreateMode.EPHEMERAL)
                 })
             }
         })
     }

     override def receive: Receive = {
         case "" =>
             // Do something
         case "shutdown" =>
             if(_zk != null) {
                 log.info("Shutting down the zk...")
                 _zk.close()
             }
             context.stop(self)
     }
 }
