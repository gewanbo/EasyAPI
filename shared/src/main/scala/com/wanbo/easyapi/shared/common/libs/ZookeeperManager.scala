package com.wanbo.easyapi.shared.common.libs

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient

/**
 * The manager of Zookeeper.
 * Created by wanbo on 15/7/7.
 */
@Deprecated
class ZookeeperManager extends Logging {

    protected val app_root = "/easyapi"

    protected val server_root = "/servers"
    protected val client_root = "/clients"

    protected val leader_root = "/leaders"

    protected val setting_root = "/settings"

    /**
     * Initialize tree node structure.
     * @param conf    Configuration
     */
    protected def initNodeTree(conf: EasyConfig): Unit ={

        try {

            log.info("Initialize the tree node in Zookeeper.")

            val zk = new ZookeeperClient(conf.zkHosts, 3000, "/", Some(this.callback))

            if(!zk.exists(app_root.replace("/", ""))){
                zk.createPath(app_root.replace("/", ""))
            }

            val serversNode = (app_root + server_root).substring(1)

            if(!zk.exists(serversNode)){
                zk.createPath(serversNode)
            }

            val clientsNode = (app_root + client_root).substring(1)

            if(!zk.exists(clientsNode)){
                zk.createPath(clientsNode)
            }

            val leadersNode = (app_root + leader_root).substring(1)

            if(!zk.exists(leadersNode)){
                zk.createPath(leadersNode)
            }

            val settingsNode = (app_root + setting_root).substring(1)

            if(!zk.exists(settingsNode)){
                zk.createPath(settingsNode)
            }

            zk.close()

            log.info("Tree node initialization successful.")

        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
    }

    private def callback(zk: ZookeeperClient): Unit ={}
}
