package com.wanbo.easyapi.server.lib

import java.io.FileInputStream
import java.util.Properties

import org.slf4j.LoggerFactory

/**
 * The configuration of EasyApi
 * Created by wanbo on 15/4/8.
 */
class EasyConfig() {
    var serverHost: String = _
    var serverPort: Int = _

    var zkEnable: Boolean = true
    var zkHosts: String = _

    var workersPort: List[Int] = _
    var workersMaxThreads: Int = _

    var driver_mysql = Map[String, String]()

    private val log = LoggerFactory.getLogger(classOf[EasyConfig])

    def getConfigure(key: String): String ={
        var value = ""

        try {

            val confProps = new Properties()
            val configFile = System.getProperty("easy.conf", "config.properties")
            confProps.load(new FileInputStream(configFile))

            if(confProps.containsKey(key))
                value = confProps.getProperty(key, "")

        } catch {
            case e: Exception =>
                log.error("There throws exception when load configure file:", e)
        }

        value
    }
}
