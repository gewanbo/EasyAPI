package com.wanbo.easyapi.client.actors

import akka.actor.Actor
import com.wanbo.easyapi.client.lib.AvailableServer
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import org.apache.zookeeper.CreateMode

/**
 * Client register
 * Created by wanbo on 15/8/17.
 */
class ClientRegister(conf: EasyConfig) extends ZookeeperManager with Actor with Logging {

    private val _zk = new ZookeeperClient(conf.zkHosts, 3000, app_root, Some(callback))

    def callback(zk: ZookeeperClient): Unit ={

        zk.watchChildren(server_root, (workers: Seq[String]) =>{

            try {

                if (workers.nonEmpty) {

//                    log.info("Servers were updated .... -----------------")
//
//                    workers.foreach(log.info)
//
//                    log.info("Current available servers ================")
//
//                    AvailableServer.serverList.foreach(x => log.info(x.toString()))

                    // Remove the servers were lost.
                    AvailableServer.serverList = AvailableServer.serverList.filter(x => workers.contains(x._1))

                    val availableServers = AvailableServer.serverList.map(_._1).toList

                    // Add new servers and update number of hits.
                    workers.foreach(server => {
                        if(availableServers.contains(server)){
                            val serverNode = server_root + "/" + server

                            val nodeBytes = zk.get(serverNode)
                            if (nodeBytes != null) {
                                val nodeStr = new String(nodeBytes)

                                if (nodeStr.isEmpty)
                                    AvailableServer.serverList = AvailableServer.serverList.updated(server, 0L)
                                else
                                    AvailableServer.serverList = AvailableServer.serverList.updated(server, nodeStr.toLong)
                            } else {
                                AvailableServer.serverList = AvailableServer.serverList.updated(server, 0L)
                            }
                        } else {
                            AvailableServer.serverList = AvailableServer.serverList + (server -> 0L)
                        }
                    })

                    AvailableServer.serverList.foreach{ case(server: String, num: Long) =>
                        val serverNode = server_root + "/" + server

                        zk.watchNode(serverNode, data => {

                            if (data.isDefined && data.get != null) {
                                val nodeStr = new String(data.get)

                                if (nodeStr.isEmpty)
                                    AvailableServer.serverList = AvailableServer.serverList.updated(server, 0L)
                                else
                                    AvailableServer.serverList = AvailableServer.serverList.updated(server, nodeStr.toLong)
                            } else {
                                // Ignore, because the servers are miss.
                            }
                        })
                    }

                } else {
                    AvailableServer.serverList = Map[String, Long]()
                }
            } catch {
                case e: Exception =>
                    e.printStackTrace()
            }
        })

        zk.watchChildren(client_root, (clients: Seq[String]) => {
            try {
                if(clients.nonEmpty && clients.contains(conf.clientId)) {
                    clients.foreach(log.info)
                } else {
                    val clientNode = client_root + "/" + conf.clientId
                    zk.create(clientNode, "{}".map(_.toByte).toArray, CreateMode.EPHEMERAL)
                }
            } catch {
                case e: Exception =>
                    e.printStackTrace()
            }
        })
    }

    private def register(): Boolean ={
        var ret = false
        val clientNode = client_root + "/" + conf.clientId

        try {

            if (_zk.exists(clientNode)) {
                log.warn("The client [%s] has been registered. Can't register same client twice!".format(conf.clientId))
                ret = true
            } else {
                val creNode = _zk.create(clientNode, "{}".map(_.toByte).toArray, CreateMode.EPHEMERAL)
                if (!creNode.isEmpty)
                    ret = true
            }
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }

        ret
    }

    private def unregister(): Unit ={

        val clientNode = client_root + "/" + conf.clientId

        try {
            var retry = 3

            while (_zk.exists(clientNode) && retry > 0) {
                _zk.delete(clientNode)
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

            if(_zk != null) {
                log.info("Shutting down the zk...")
                _zk.close()
            }
            context.stop(self)
    }
}
