package com.wanbo.easyapi.server.lib

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

/**
 * Sync work count data.
 * Created by wanbo on 2015/11/4.
 */
object WorkCounterSync extends ZookeeperManager with Logging {

    private var _zk: CuratorFramework = null
    private var _conf: EasyConfig = null

    def init(conf: EasyConfig): Unit = {
        _conf = conf
    }

    def sync(): Unit ={
        try {
            zkConnect()

            var dataList: Map[String, Long] = _conf.workersPort.map(port => {
                (_conf.serverHost + ":" + port, 0L)
            }).toMap

            WorkCounter.getSummary.foreach(server => {
                dataList = dataList.updated(server._1, server._2.toLong)
            })

            dataList.foreach(item => {
                val serverNode = app_root + server_root + "/" + item._1
                if(_zk.checkExists().forPath(serverNode) != null) {
                    _zk.setData().forPath(serverNode, item._2.toString.getBytes)
                }
            })

            //_zk.close()
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
    }

    private def zkConnect(): Unit ={

        if(_zk == null){
            val retryPolicy = new ExponentialBackoffRetry(1000, 3)
            _zk = CuratorFrameworkFactory.newClient(_conf.zkHosts, retryPolicy)
            _zk.start()
        }
    }

}
