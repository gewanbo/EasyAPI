package com.wanbo.easyapi.client.lib

import com.alibaba.fastjson.JSONObject
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient

/**
 * Sync work count data.
 * Created by wanbo on 2015/11/16.
 */
object WorkCounterSync extends ZookeeperManager with Logging {

    def sync(conf: EasyConfig): Unit ={
        try {

            val summary = WorkCounter.getSummary

            val successData = new JSONObject()
            val failureData = new JSONObject()

            summary.foreach(x => successData.put(x._1, x._2._1))
            summary.foreach(x => failureData.put(x._1, x._2._2))

            val jsonData = new JSONObject()
            jsonData.put("success", successData)
            jsonData.put("failure", failureData)

            val zk = new ZookeeperClient(conf.zkHosts, 3000, app_root, Some(this.callback))

            val clientNode = client_root + "/" + conf.clientId
            if(zk.exists(clientNode)){
                zk.set(clientNode, jsonData.toJSONString.map(_.toByte).toArray)
            }

            zk.close()
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
    }

    def callback(zk: ZookeeperClient): Unit ={}
}
