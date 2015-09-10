package com.wanbo.easyapi.server.database

import com.wanbo.easyapi.shared.common.libs.EasyConfig
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

        val settings = conf.driverSettings.filter(x => x._2.get("type").get == "hbase").toList.map(_._2)

        if(settings.size > 0){
            db_zq = settings.apply(0).getOrElse("zk", "")
        }

        if(db_zq == "")
            log.warn("The Zookeeper host for HBase is not found ---------- !!!")
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
