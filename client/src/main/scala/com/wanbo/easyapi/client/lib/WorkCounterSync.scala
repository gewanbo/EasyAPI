package com.wanbo.easyapi.client.lib

import com.alibaba.fastjson.JSONObject
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ZookeeperManager}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.utils.CloseableUtils

/**
 * Sync work count data.
 * Created by wanbo on 2015/11/16.
 */
object WorkCounterSync extends ZookeeperManager with Logging {

    def sync(conf: EasyConfig): Unit ={
        var client: CuratorFramework = null

        try {

            val summary = WorkCounter.getSummary

            val successData = new JSONObject()
            val failureData = new JSONObject()

            summary.foreach(x => successData.put(x._1, x._2._1))
            summary.foreach(x => failureData.put(x._1, x._2._2))

            val jsonData = new JSONObject()
            jsonData.put("success", successData)
            jsonData.put("failure", failureData)

            client = CuratorFrameworkFactory.newClient(conf.zkHosts, new ExponentialBackoffRetry(3000, 3))

            val clientNode = app_root + client_root + "/" + conf.clientId

            client.start()

            if(client.checkExists().forPath(clientNode) != null){
                client.setData().forPath(clientNode, jsonData.toJSONString.map(_.toByte).toArray)
            }

        } catch {
            case e: Exception =>
                log.error("Error:", e)
        } finally {
            CloseableUtils.closeQuietly(client)
        }
    }
}
