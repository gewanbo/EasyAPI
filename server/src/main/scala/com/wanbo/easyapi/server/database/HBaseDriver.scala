package com.wanbo.easyapi.server.database

import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory}
import org.slf4j.LoggerFactory

/**
 * HBase driver.
 * Created by wanbo on 15/4/17.
 */
case class HBaseDriver() extends DbDriver with IDriver {
    private var db_zq: String = ""
    private var _conn: Connection = null

    private val log = LoggerFactory.getLogger(classOf[HBaseDriver])

    override def setConfiguration(conf: EasyConfig): Unit = {
        this._conf = conf

        val settings = conf.driverSettings.filter(x => x._2.get("type").get == "hbase").toList.map(_._2)

        if(settings.nonEmpty){
            db_zq = settings.head.getOrElse("zk", "")
        }

        if(db_zq == "")
            log.warn("The Zookeeper host for HBase is not found ---------- !!!")
    }

    def getConnector(dbName: String = "test", writable: Boolean = false): Connection ={

        try {

            if(db_zq != ""){
                val hConf = HBaseConfiguration.create()
                hConf.set("hbase.zookeeper.quorum", db_zq)
                hConf.set("hbase.client.operation.timeout", "30000")
                hConf.set("hbase.client.retries.number", "3")

                _conn = ConnectionFactory.createConnection(hConf)
            } else {
                throw new Exception("The configuration option [hbase.zookeeper.quorum] is empty!")
            }

        } catch {
            case e: Exception =>
                throw e
        }

        _conn
    }

    def close(): Unit ={
        try{
            if(_conn != null)
                _conn.close()
        } catch {
            case e: Exception =>
        }
    }
}
