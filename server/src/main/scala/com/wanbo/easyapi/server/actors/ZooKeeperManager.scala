package com.wanbo.easyapi.server.actors

import akka.actor.Actor
import com.wanbo.easyapi.server.lib.{EasyConfig, ZookeeperClient}
import org.apache.zookeeper.CreateMode
import org.slf4j.LoggerFactory

/**
 * The manager of ZooKeeper
 * Created by wanbo on 15/4/8.
 */
class ZooKeeperManager(conf: EasyConfig) extends Actor{

    private val server_root = "/servers"
    private val client_root = "/clients"

    private val _zk = new ZookeeperClient(conf.zkHosts, 3000, "/easyapi", Some(callback))

    private val log = LoggerFactory.getLogger(classOf[ZooKeeperManager])

    init()

    def init(): Unit ={
        if(!_zk.exists(server_root)){
            _zk.createPath(server_root)
        }
        if(!_zk.exists(client_root)){
            _zk.createPath(client_root)
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
                        zk.create(workerPath, Array("1".toByte), CreateMode.EPHEMERAL)
                })
            }
        })
    }

    override def receive: Receive = {
        case "" =>
            // Do something
    }
}
