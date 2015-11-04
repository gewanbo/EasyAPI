package com.wanbo.easyapi.server.lib

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient

/**
 * Sync work count data.
 * Created by wanbo on 2015/11/4.
 */
object WorkCounterSync extends ZookeeperManager with Logging {

    def sync(conf: EasyConfig): Unit ={
        try {
            val zk = new ZookeeperClient(conf.zkHosts, 3000, app_root, Some(this.callback))
            WorkCounter.getSummary.foreach(server => {
                val serverNode = server_root + "/" + server._1
                if(zk.exists(serverNode)){
                    zk.set(serverNode, server._2.toString.map(_.toByte).toArray)
                }
            })

            zk.close()
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
    }

    def callback(zk: ZookeeperClient): Unit ={}
}
