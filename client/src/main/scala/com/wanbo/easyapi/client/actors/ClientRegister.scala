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

            workers.foreach(println)

            if(workers.size > 0) {
                AvailableServer.serverList = workers.toList

                register()
            } else {
                AvailableServer.serverList = null
                unregister()
            }
        })
    }

    private def register(){}

    private def unregister(){}

    override def receive: Receive = {
        case "StartUp" =>
            log.info("I'm starting up ...")

            while(AvailableServer.serverList == null){
                Thread.sleep(3000)
                log.info("Waiting for update ...")
            }

            sender() ! "Ready"

        case "ShutDown" =>
            unregister()

            if(_zk != null) {
                log.info("Shutting down the zk...")
                _zk.close()
            }
            context.stop(self)
    }
}
