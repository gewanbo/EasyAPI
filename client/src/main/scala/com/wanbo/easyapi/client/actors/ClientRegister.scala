package com.wanbo.easyapi.client.actors

import java.util.{Calendar, TimeZone}

import akka.actor.Actor
import com.wanbo.easyapi.client.lib.{AvailableServer, ClientSetting}
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.utils.{CloseableUtils, ZKPaths}
import org.apache.zookeeper.CreateMode

/**
 * Client register
 * Created by wanbo on 15/8/17.
 */
class ClientRegister(conf: EasyConfig) extends ZookeeperManager with Actor with Logging {

    private var _client: CuratorFramework = null
    private var _cache: PathChildrenCache = null

    val _serverRoot = ZKPaths.makePath(app_root, server_root)
    val _clientRoot = ZKPaths.makePath(app_root, client_root)
    val _clientNode = ZKPaths.makePath(app_root, client_root, conf.clientId)

    init()

    def init(): Unit ={
        try {
            _client = CuratorFrameworkFactory.newClient(conf.zkHosts, new ExponentialBackoffRetry(1000, 3))

            _client.start()

            _cache = new PathChildrenCache(_client, _serverRoot, true)

            _cache.start()

            _cache.getListenable.addListener(new PathChildrenCacheListener {
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

    private def addServer(pathChildrenCacheEvent: PathChildrenCacheEvent): Unit ={
        log.info("New server node added, now updating available server list ...")

        val node = ZKPaths.getNodeFromPath(pathChildrenCacheEvent.getData.getPath)
        val nodeData = pathChildrenCacheEvent.getData.getData

        if(nodeData != null) {
            val dataStr = new String(nodeData)
            if(dataStr != "")
                AvailableServer.serverList = AvailableServer.serverList.updated(node, dataStr.toLong)
        } else
            AvailableServer.serverList = AvailableServer.serverList.updated(node, 0L)

        log.info("The final server list is:")
        AvailableServer.serverList.foreach(s => log.info(s._1 + "=>" + s._2))
    }

    private def updateServer(pathChildrenCacheEvent: PathChildrenCacheEvent): Unit ={
        log.info("Server nodes data were updated, now updating each available server's rate ...")

        val node = ZKPaths.getNodeFromPath(pathChildrenCacheEvent.getData.getPath)
        val nodeData = pathChildrenCacheEvent.getData.getData

        if(nodeData != null) {
            val dataStr = new String(nodeData)

            log.info("node:" + node + " nodeData: " + dataStr)

            if(dataStr != "")
                AvailableServer.serverList = AvailableServer.serverList.updated(node, dataStr.toLong)
        }

        log.info("The final server list is:")
        AvailableServer.serverList.foreach(s => log.info(s._1 + "=>" + s._2))
    }

    private def removeServer(pathChildrenCacheEvent: PathChildrenCacheEvent): Unit ={
        log.info("Server node was deleted, now updating available server list ...")

        val node = ZKPaths.getNodeFromPath(pathChildrenCacheEvent.getData.getPath)

        if(AvailableServer.serverList.contains(node)) {
            AvailableServer.serverList = AvailableServer.serverList.-(node)
        }

        log.info("The final server list is:")
        AvailableServer.serverList.foreach(s => log.info(s._1 + "=>" + s._2))
    }

    private def updateClientSettings(): Unit ={
        val setting_client_root = ZKPaths.makePath(app_root, setting_root, "clients")

        _client.createContainers(setting_client_root)

        val currentClientSettingNode = ZKPaths.makePath(setting_client_root, conf.clientId)

        val clientSetting = new ClientSetting

        clientSetting.version = System.getProperty("appVersion", "0.0.0")
        clientSetting.host = conf.clientId
        clientSetting.startTime = Calendar.getInstance(TimeZone.getTimeZone("Asin/Shanghai")).getTime.toString

        if (_client.checkExists().forPath(currentClientSettingNode) != null) {
            // Override all setting data
            val stat = _client.setData().forPath(currentClientSettingNode, clientSetting.toJson.getBytes())
            if(stat != null){
                log.info("Update current client settings successful!")
            } else {
                log.info("Update current client settings failed!")
            }
        } else {
            _client.create().withMode(CreateMode.PERSISTENT).forPath(currentClientSettingNode, clientSetting.toJson.getBytes())
            log.warn("The ZNode [%s] does not exists, has created yet!".format(currentClientSettingNode))
        }
    }

    private def register(): Boolean ={
        var ret = false

        try {

            if (_client.checkExists().forPath(_clientNode) != null) {
                log.warn("The client [%s] has been registered. Can't register same client twice!".format(conf.clientId))
                ret = true
            } else {
                val creNode = _client.create.withMode(CreateMode.EPHEMERAL).forPath(_clientNode, "{}".map(_.toByte).toArray)
                if (creNode.nonEmpty) {
                    ret = true
                    updateClientSettings()
                }
            }
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }

        ret
    }

    private def unregister(): Unit ={

        try {
            var retry = 3

            while (_client.checkExists().forPath(_clientNode) != null && retry > 0) {
                _client.delete().guaranteed().forPath(_clientNode)
                retry -= 1
            }
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
    }

    override def receive: Receive = {
        case "StartUp" =>
            log.info("I'm starting up ...")

            if(!register()){
                sender() ! "ShutDown"
            } else {

                while (AvailableServer.serverList.size < 1) {
                    Thread.sleep(3000)
                    log.info("Waiting for update ...")
                }

                sender() ! "Ready"
            }

        case "ShutDown" =>
            unregister()

            CloseableUtils.closeQuietly(_cache)
            CloseableUtils.closeQuietly(_client)

            context.stop(self)
    }
}
