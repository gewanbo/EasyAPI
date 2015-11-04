package com.wanbo.easyapi.client.actors

import akka.actor.Actor
import com.wanbo.easyapi.client.lib.AvailableServer
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import org.apache.zookeeper.CreateMode
import org.slf4j.LoggerFactory

/**
 * Client register
 * Created by wanbo on 15/8/17.
 */
class ClientRegister(conf: EasyConfig) extends ZookeeperManager with Actor {

    private val _zk = new ZookeeperClient(conf.zkHosts, 3000, app_root, Some(callback))

    private val log = LoggerFactory.getLogger(classOf[ClientRegister])

    def callback(zk: ZookeeperClient): Unit ={

        zk.watchChildren(server_root, (workers: Seq[String]) =>{

            try {

                if (workers.size > 0) {

                    workers.foreach(server => {
                        val serverNode = server_root + "/" + server

                        val nodeBytes = zk.get(serverNode)
                        if (nodeBytes != null) {
                            val nodeStr = new String(nodeBytes)

                            if (nodeStr.isEmpty)
                                AvailableServer.serverList :+=(server, 0L)
                            else
                                AvailableServer.serverList :+=(server, nodeStr.toLong)
                        } else {
                            AvailableServer.serverList :+=(server, 0L)
                        }
                    })

                } else {
                    AvailableServer.serverList = null
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
                log.error("The client [%s] has been registered. Can't register same client twice!")
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

    private def unregister(){}

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
