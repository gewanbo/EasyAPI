package com.wanbo.easyapi.server.database

import com.wanbo.easyapi.server.lib.EasyConfig
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.slf4j.LoggerFactory

/**
 * HBase driver.
 * Created by wanbo on 15/4/17.
 */
case class HBaseDriver() extends Driver {
    private var db_zq: String = ""

    private val log = LoggerFactory.getLogger(classOf[HBaseDriver])

    override def setConfiguration(conf: EasyConfig): Unit = {

        db_zq = conf.getConfigure("hbase.zookeeper.quorum")
    }

    def getHConf: Configuration ={
        val hConf = HBaseConfiguration.create()

        try {

            if(db_zq == "")
                throw new Exception("The configuration option [hbase.zookeeper.quorum] is empty!")

            hConf.set("hbase.zookeeper.quorum", db_zq)
        } catch {
            case e: Exception =>
                log.error("HBase configure exception:", e)
        }

        hConf
    }
}
