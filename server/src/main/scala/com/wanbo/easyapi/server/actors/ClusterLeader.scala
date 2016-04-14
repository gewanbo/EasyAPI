package com.wanbo.easyapi.server.actors

import akka.actor.{Actor, Props}
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import org.apache.zookeeper.CreateMode

/**
 * Elect a leader from cluster, and control the cache updating.
 * Created by wanbo on 15/7/1.
 */
private[server] class ClusterLeader(conf: EasyConfig) extends ZookeeperManager with Actor with Logging {

    private var _zk: ZookeeperClient = null

    private var _serverId = ""

    private val _cacheManagerActorName = "cache_manager"
    private val _nodeBalancerActorName = "node_balancer"

    init()

    def init(): Unit ={

        _serverId = conf.serverId

        _zk = new ZookeeperClient(conf.zkHosts, 3000, app_root, Some(callback))

    }

    def callback(zk: ZookeeperClient): Unit ={

        if(zk != null) {
            zk.watchChildren(leader_root, (nodes: Seq[String]) => {

                try {
                    if (nodes != null && nodes.nonEmpty) {

                        log.info("Total number of workers:" + nodes.length)

                        val serverList = nodes.map(x => {
                            val fields = x.split("-")
                            (fields(0), fields(1))
                        }).sortBy(x => x._2).toList

                        serverList.foreach(x => {
                            log.info("node:" + x.toString)
                        })

                        if(serverList.count(x => x._1 == _serverId) < 1){
                            log.info("Can not find my id, start to register.")
                            zk.create("%s/%s-".format(leader_root, _serverId), Array("1".toByte), CreateMode.EPHEMERAL_SEQUENTIAL)
                        } else {

                            val headNode = serverList.head._1

                            log.info("Tmp leader is:" + headNode)
                            log.info("The smallest one is:" + headNode)

                            if (headNode == _serverId) {

                                if(!ClusterLeader.isLeader) {
                                    log.info("I became the leader!!!")
                                    ClusterLeader.isLeader = true
                                    gotLeader()
                                } else {
                                    log.info("I had already become the leader!!!")
                                }
                            } else {
                                log.info("I became the follower!!....")
                                lostLeader()
                            }
                        }
                    } else {
                        zk.create("%s/%s-".format(leader_root, _serverId), Array("1".toByte), CreateMode.EPHEMERAL_SEQUENTIAL)
                    }
                } catch {
                    case e: Exception =>
                        log.error("Error:", e)
                }
            })
        }
    }

    private def gotLeader() ={
        log.info("Start to do the leading work.")
        openCacheUpdate()
        openNodeBalance()
    }

    private def lostLeader(): Unit ={
        log.info("Stop doing the leading work.")
        closeCacheUpdate()
        closeNodeBalance()
    }

    private def openCacheUpdate(): Unit ={

        log.info("------------- I'm ready to update cache -----")

        val cacheOption = context.child(_cacheManagerActorName)

        if(cacheOption.isEmpty) {
            val cacheManager = context.actorOf(Props(new CacheManager(conf)), name = _cacheManagerActorName)
            cacheManager ! "init"
        }

    }

    private def closeCacheUpdate(): Unit ={

        log.info("------------- Stop updating cache -----")

        val cacheOption = context.child(_cacheManagerActorName)

        if(cacheOption.isDefined) {
            context.stop(cacheOption.get)
        }

    }

    private def openNodeBalance(): Unit ={
        log.info("-------------- Start to run node balancer...")

        val nodeBalancerOption = context.child(_nodeBalancerActorName)

        if(nodeBalancerOption.isEmpty) {
            val nodeBalancer = context.actorOf(Props(new NodeBalancer(conf)), name = _nodeBalancerActorName)
            nodeBalancer ! "init"
        }
    }

    private def closeNodeBalance(): Unit ={
        log.info("-------------- Stopping node balancer...")

        val nodeBalancerOption = context.child(_nodeBalancerActorName)

        if(nodeBalancerOption.isDefined){
            nodeBalancerOption.get ! "stop"
            context.stop(nodeBalancerOption.get)
        }
    }

    override def receive: Receive = {
        case "" =>
        // Do something
        case "shutdown" =>

            closeCacheUpdate()
            closeNodeBalance()

            if(_zk != null) {
                log.info("Shutting down the zk...")
                _zk.close()
            }
            context.stop(self)
    }
}

private[server] object ClusterLeader{
    private var isLeader = false
}