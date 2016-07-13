package com.wanbo.easyapi.shared.common.libs

import com.wanbo.easyapi.shared.common.Logging
import org.apache.curator.RetryPolicy
import org.apache.curator.framework.recipes.cache.PathChildrenCache
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.utils.CloseableUtils
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.data.Stat

/**
 * The manager of Zookeeper.
 * Created by wanbo on 16/7/13.
 */
class EasyZKManager extends Logging {

    protected val app_namespace = "easyapi"

    protected val app_root = "/easyapi"

    protected val server_root = "/servers"
    protected val client_root = "/clients"
    protected val leader_root = "/leaders"
    protected val setting_root = "/settings"

    protected var client: CuratorFramework = null
    protected var cache: PathChildrenCache = null

    protected def open(connectionString: String): Unit ={
        client = CuratorFrameworkFactory.builder()
            .connectString(connectionString)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .namespace(app_namespace)
            .build()
    }

    protected def open(connectionString: String, retryPolicy: RetryPolicy): Unit ={
        client = CuratorFrameworkFactory.builder()
            .connectString(connectionString).retryPolicy(retryPolicy).namespace(app_namespace).build()
    }

    /**
      * Create the given ZNode with the given data
      * @param path         The ZNode path.
      * @param payload      The data set to ZNode.
      * @return             The ZNode name.
      */
    protected def create(path: String, payload: Array[Byte]): String ={
        client.create().forPath(path, payload)
    }

    /**
      * Create the given EPHEMERAL ZNode with the given data
      * @param path         The ZNode path.
      * @param payload      The data set to ZNode.
      * @return             The ZNode name.
      */
    protected def createEphemeral(path: String, payload: Array[Byte]): String ={
        client.create().withMode(CreateMode.EPHEMERAL).forPath(path, payload)
    }

    /**
      * Set data for the given ZNode.
      * @param path         The ZNode path.
      * @param payload      The data set to ZNode.
      * @return             The ZNode state.
      */
    protected def setData(path: String, payload: Array[Byte]): Stat ={
        client.setData().forPath(path, payload)
    }

    /**
      * Delete the given ZNode.
      * @param path         The ZNode path.
      * @return
      */
    protected def delete(path: String): Unit ={
        client.delete().forPath(path)
    }

    /**
      * Delete the given ZNode and guarantee that it completes
      * @param path         The ZNode path.
      * @return
      */
    protected def deleteWithGuaranteed(path: String): Unit ={
        client.delete().guaranteed().forPath(path)
    }

    /**
     * Initialize tree node structure.
     * @param conf    Configuration
     */
    protected def initNodeTree(conf: EasyConfig): Unit ={

        try {

            log.info("Initialize the tree node in Zookeeper.")

            if(client == null)
                throw new Exception("The zk client is empty, initialize it first!")

            client.createContainers(server_root)
            client.createContainers(client_root)
            client.createContainers(leader_root)
            client.createContainers(setting_root)

            log.info("Tree node initialization successful.")

        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
    }

    protected def close(): Unit ={
        CloseableUtils.closeQuietly(cache)
        CloseableUtils.closeQuietly(client)
    }
}
