package com.wanbo.easyapi.server.lib

import org.apache.zookeeper.CreateMode
import org.slf4j.LoggerFactory

/**
 * The manager of ZooKeeper
 * Created by wanbo on 15/4/8.
 */
class ZooKeeperManager(servers: String) extends ZookeeperClient(servers) {

    private val server_root = "/easyapi/servers"

    private val log = LoggerFactory.getLogger(classOf[ZooKeeperManager])

    init()

    def init(): Unit ={
        if(!exists(server_root)){
            createPath(server_root)
        }
    }

    def registerWorkers(workers: List[String]): Unit ={
        workers.foreach(worker => {
            val workerPath = server_root + "/" + worker
            if(!exists(workerPath))
                create(workerPath, Array("1".toByte), CreateMode.EPHEMERAL)
        })
    }

}
