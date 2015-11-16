package com.wanbo.easyapi.client.lib

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient

import scala.util.parsing.json.JSONObject

/**
 * Sync work count data.
 * Created by wanbo on 2015/11/16.
 */
object WorkCounterSync extends ZookeeperManager with Logging {

    def sync(conf: EasyConfig): Unit ={
        try {

            var missData = Map[String, Any]()
            WorkCounter.getSummary.foreach(server => {
                missData += server._1 -> server._2
            })

            val data = Map[String, Any]("miss" -> JSONObject(missData))

            val jsonData = JSONObject(data)

            log.info("-------sync data:" + jsonData.toString())

            val zk = new ZookeeperClient(conf.zkHosts, 3000, app_root, Some(this.callback))

            val serverNode = client_root + "/" + conf.clientId
            if(zk.exists(serverNode)){
                zk.set(serverNode, jsonData.toString().map(_.toByte).toArray)
            }

            zk.close()
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
    }

    def callback(zk: ZookeeperClient): Unit ={}
}
