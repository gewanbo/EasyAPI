package com.wanbo.easyapi.server.actors

import java.util.{Calendar, TimeZone}

import akka.actor.Actor
import com.wanbo.easyapi.server.lib.ServerSetting
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

             log.info("System configured workers:" + workerList.mkString("-"))
             log.info("Current running workers:" + workers.mkString("-"))

             if(lostWorkers.nonEmpty) {
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

         // ### Update current server settings

         val settings_server_root = setting_root + "/servers"
         if(!zk.exists(settings_server_root)){
             zk.create(settings_server_root, "".map(_.toByte).toArray, CreateMode.PERSISTENT)
             log.warn("The ZNode [%s] does not exists, has created yet!".format(settings_server_root))
         }

         val currentServerSettingRoot = settings_server_root + "/" + conf.serverHost

         val serverSetting = new ServerSetting

         serverSetting.version = System.getProperty("appVersion", "0.0.0")
         serverSetting.host = conf.serverHost
         serverSetting.startTime = Calendar.getInstance(TimeZone.getTimeZone("Asin/Shanghai")).getTime.toString

         if(!zk.exists(currentServerSettingRoot)){
             zk.create(currentServerSettingRoot, serverSetting.toJson.getBytes(), CreateMode.PERSISTENT)
             log.warn("The ZNode [%s] does not exists, has created yet!".format(currentServerSettingRoot))
         } else {
             // Override all setting data
             zk.set(currentServerSettingRoot, serverSetting.toJson.getBytes())
         }

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
