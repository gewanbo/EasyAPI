package com.wanbo.easyapi.server.actors

import akka.actor.{Actor, Cancellable}
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import org.slf4j.MDC

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._

/**
 * Node balancer
 * Created by wanbo on 4/12/16.
 */
class NodeBalancer(conf: EasyConfig) extends Actor with Logging{

    import context.system

    private var _zk: ZookeeperClient = null

    private var _scheduler: Cancellable = null

    override def preStart(): Unit ={

    }

    override def receive: Receive = {

        case "init" =>
            MDC.put("destination", "balancer")
            log.info("Start to initialize node balancer.")

            if(_scheduler == null || _scheduler.isCancelled) {
                _scheduler = system.scheduler.schedule(10 seconds, 3 seconds, self, "sync")
            }

            MDC.clear()
        case "sync" =>
            MDC.put("destination", "balancer")
            log.info("Sync all nodes priority ....")
            log.info("-----    Balancer  !!!!!! ")

            println(_zk)

            zkConnect()

            val server_node = "/servers"

            if(_zk.exists(server_node)){
                val nodes = _zk.getChildren(server_node)

                val dataList = nodes.map(node => {
                    var nodeData = 0L

                    val dataBytes = _zk.get(server_node + "/" + node)
                    if(dataBytes != null){
                        val dataStr = new String(dataBytes)
                        if(dataStr!="" && dataStr.forall(_.isDigit)){
                            nodeData = dataStr.toLong
                        }
                    }
                    val hosts = node.split(":")
                    (hosts(0), nodeData)
                })

                val dataEachServer = dataList.groupBy(_._1).map(x => {
                    (x._1, x._2.map(_._2).sum)
                })

                if(dataEachServer.nonEmpty) {
                    val values = dataEachServer.values
                    val min = values.min
                    val max = values.max
                    val gap = max - min
                    var k = 0F
                    if(max > 0) {
                        k = min / max.toFloat
                    }

                    log.info("----min:" + min)
                    log.info("----max:" + max)
                    log.info("----gap:" + gap)
                    log.info("----k:" + k)

                    if(k > 0 && k < 0.8 && gap > 10) {
                        // Rebalance
                        log.info("The cluster is unhealthy, starting to rebalance the priority of every node.")
                    } else {
                        log.info("Current nodes in cluster are balanced.")
                    }
                } else {
                    log.info("No priority data was found.")
                }
            } else {
                log.info("No server nodes should balance.")
            }

            MDC.clear()
        case "stop" =>
            if(_scheduler != null && !_scheduler.isCancelled)
                _scheduler.cancel()
    }

    private def zkConnect(): Unit ={
        if(_zk == null || !_zk.isAlive){
            _zk = new ZookeeperClient(conf.zkHosts, 3000, "/easyapi", Some(this.callback))
        }
    }

    def callback(zk: ZookeeperClient): Unit ={}
}
