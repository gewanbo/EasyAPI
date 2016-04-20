package com.wanbo.easyapi.server.lib

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient

/**
 * Sync work count data.
 * Created by wanbo on 2015/11/4.
 */
object WorkCounterSync extends ZookeeperManager with Logging {

    private var _zk: ZookeeperClient = null
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
                val serverNode = server_root + "/" + item._1
                if(_zk.exists(serverNode)){
                    _zk.set(serverNode, item._2.toString.getBytes)
                }
            })

            //zk.close()
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
    }

    private def zkConnect(): Unit ={
        if(_zk == null || !_zk.isAlive){
            _zk = new ZookeeperClient(_conf.zkHosts, 3000, app_root, Some(this.callback))
        }
    }

    def callback(zk: ZookeeperClient): Unit ={}
}
