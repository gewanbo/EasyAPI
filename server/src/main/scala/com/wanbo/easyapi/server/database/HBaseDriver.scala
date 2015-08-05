package com.wanbo.easyapi.server.database

import com.wanbo.easyapi.server.lib.EasyConfig
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration

/**
 * HBase driver.
 * Created by wanbo on 15/4/17.
 */
case class HBaseDriver() extends Driver {
    private var db_zq: String = _

    override def setConfiguration(conf: EasyConfig): Unit = {

        db_zq = conf.getConfigure("hbase.zookeeper.quorum")
    }

    def getHConf: Configuration ={
        val hConf = HBaseConfiguration.create()
        hConf.set("hbase.zookeeper.quorum", db_zq)
        hConf
    }
}
