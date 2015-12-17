package com.wanbo.easyapi.client.lib

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import org.apache.zookeeper.CreateMode

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

            val clientNode = client_root + "/" + conf.clientId
            if(zk.exists(clientNode)){
                zk.set(clientNode, jsonData.toString().map(_.toByte).toArray)
            } else {
                zk.create(clientNode, jsonData.toString().map(_.toByte).toArray, CreateMode.EPHEMERAL)
            }

            zk.close()
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
    }

    def callback(zk: ZookeeperClient): Unit ={}
}
