package com.wanbo.easyapi.server.actors

import java.util.{Calendar, TimeZone}

import akka.actor.Actor
import com.wanbo.easyapi.server.lib.{ServerSetting, WorkerStatus}
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs._
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.apache.curator.utils.ZKPaths

/**
 * The worker register.
 *
 * Register workers or re-register
 *
 * @author wanbo<gewanbo@gmail.com>
 * @param conf System configuration.
 */
class WorkerRegister(conf: EasyConfig) extends EasyZKManager with Actor with Logging {

    private val _workerList = conf.workersPort.map(port => ServerNode(conf.serverHost, port))

    private var _status = WorkerStatus.NOT_START

    init()

    def init(): Unit = {

        try {

            this.open(conf.zkHosts)

            client.start()

            this.initNodeTree(conf)

            cache = new PathChildrenCache(client, server_root, true)

            cache.start()

            cache.getListenable.addListener(new PathChildrenCacheListener {
                override def childEvent(curatorFramework: CuratorFramework, pathChildrenCacheEvent: PathChildrenCacheEvent): Unit = {
                    pathChildrenCacheEvent.getType match {
                        case PathChildrenCacheEvent.Type.CHILD_ADDED =>
                            addServer(pathChildrenCacheEvent)

                        case PathChildrenCacheEvent.Type.CHILD_UPDATED =>
                            updateServer(pathChildrenCacheEvent)

                        case PathChildrenCacheEvent.Type.CHILD_REMOVED =>
                            removeServer(pathChildrenCacheEvent)

                        case _ =>
                            log.info("...")
                    }
                }
            })

        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
    }

    def addServer(pathChildrenCacheEvent: PathChildrenCacheEvent): Unit ={

        val node = ZKPaths.getNodeFromPath(pathChildrenCacheEvent.getData.getPath)

        log.info("New worker [%s] node added, now updating worker list ...".format(node))

    }
    def updateServer(pathChildrenCacheEvent: PathChildrenCacheEvent): Unit ={
        //log.info("Server nodes data were updated, now updating each worker's rate ...")
    }
    def removeServer(pathChildrenCacheEvent: PathChildrenCacheEvent): Unit ={

        val node = ZKPaths.getNodeFromPath(pathChildrenCacheEvent.getData.getPath)

        log.info("Worker node [%s] was deleted.".format(node))

        if(_workerList.contains(ServerNodeFactory.parse(node))){
            log.warn("The lost worker [%s] belongs to current server, start to register it again.".format(node))
            reRegister(node)
        }
    }

    private def updateClientSettings(): Unit ={
        val settings_server_root = ZKPaths.makePath(setting_root, "servers")

        client.createContainers(settings_server_root)

        val currentServerSettingNode = ZKPaths.makePath(settings_server_root, conf.serverHost)

        val serverSetting = new ServerSetting

        serverSetting.version = System.getProperty("appVersion", "0.0.0")
        serverSetting.host = conf.serverHost
        serverSetting.startTime = Calendar.getInstance(TimeZone.getTimeZone("Asin/Shanghai")).getTime.toString

        if (client.checkExists().forPath(currentServerSettingNode) != null) {
            // Override all setting data
            val stat = setData(currentServerSettingNode, serverSetting.toJson.getBytes())
            if(stat != null){
                log.info("Update current client settings successful!")
            } else {
                log.info("Update current client settings failed!")
            }
        } else {
            create(currentServerSettingNode, serverSetting.toJson.getBytes())
            log.warn("The ZNode [%s] does not exists, has created yet!".format(currentServerSettingNode))
        }
    }

    private def reRegister(workerName: String): Boolean ={
        var ret = false

        try {

            if(_status != WorkerStatus.STOPPED && _workerList.contains(ServerNodeFactory.parse(workerName))) {

                val serverNode = ZKPaths.makePath(server_root, workerName)

                if (client.checkExists().forPath(serverNode) == null) {
                    val creNode = createEphemeral(serverNode, Array[Byte]())
                    if (creNode.nonEmpty) {
                        ret = true
                        log.info("The worker [%s] re-register successful!".format(workerName))
                    }
                }
            }

        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }

        ret
    }

    /**
      * Register workers to Zk
      *
      * *** Need after leader confirmation complete.
      *
      * @return
      */
    private def register(): Boolean ={
        var ret = false

        try {

            var count = 0
            _workerList.foreach { case worker: ServerNode =>

                val workerName = worker.toString
                val serverNode = ZKPaths.makePath(server_root, workerName)

                if (client.checkExists().forPath(serverNode) != null) {
                    log.warn("The worker [%s] has been registered. No need to register again!".format(workerName))
                    ret = true
                } else {
                    val creNode = createEphemeral(serverNode, Array[Byte]())
                    if (creNode.nonEmpty) {
                        ret = true
                        log.info("The worker [%s] register successful!".format(workerName))
                    } else {
                        count += 1
                    }
                }

            }

            if(count == 0) {
                ret = true
            }

        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }

        ret
    }

    private def unregister(): Unit ={

        try {

            _workerList.foreach { case worker: ServerNode =>

                val workerName = worker.toString
                val serverNode = ZKPaths.makePath(server_root, workerName)

                if (client.checkExists().forPath(serverNode) != null) {
                    deleteWithGuaranteed(serverNode)
                }

            }

        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
    }

    override def receive: Receive = {
        case "StartUp" =>

            // Register workers to ZK
            while (!register()) {
                log.info("Waiting for registering workers to ZK cluster complete ...")
                Thread.sleep(3000)
            }

            log.info("Register workers to ZK cluster successful!")

            // Update server settings
            updateClientSettings()

            // After every work is ready.
            _status = WorkerStatus.STARTED

            sender() ! "Ready"

        case "ShutDown" =>

            // Before every thing start to close.
            _status = WorkerStatus.STOPPED

            unregister()

            this.close()

            context.stop(self)
    }
 }
