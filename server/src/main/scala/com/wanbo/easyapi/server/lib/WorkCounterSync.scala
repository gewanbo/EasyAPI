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
            WorkCounter.getSummary.foreach(server => {
                val serverNode = server_root + "/" + server._1
                if(_zk.exists(serverNode)){
                    _zk.set(serverNode, server._2.toString.map(_.toByte).toArray)
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
