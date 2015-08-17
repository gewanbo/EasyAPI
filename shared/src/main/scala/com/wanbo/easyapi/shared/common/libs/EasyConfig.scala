package com.wanbo.easyapi.shared.common.libs

import java.util.Properties

import org.slf4j.LoggerFactory

/**
 * The configuration of EasyApi
 * Created by wanbo on 15/4/8.
 */
class EasyConfig() {

    // Common
    var zkEnable: Boolean = true
    var zkHosts: String = _

    // Server
    var serverId: String = _
    var serverHost: String = _
    var serverPort: Int = _

    var workersPort: List[Int] = _
    var workersMaxThreads: Int = _

    var cache_type = "redis"

    var driver_mysql = Map[String, String]()

    // Client
    var clientId: String = _

    private var _confProps: Properties = null

    private val log = LoggerFactory.getLogger(classOf[EasyConfig])

    def parseServerConf(confProps: Properties): Unit ={

        _confProps = confProps

        serverId = confProps.getProperty("server.id", "0")
        serverHost = confProps.getProperty("server.host", "localhost")
        serverPort = confProps.getProperty("server.port", "8800").toInt

        val workers_port = confProps.getProperty("server.worker.port", "8801")

        workersPort = workers_port.split(";").toList.map(_.toInt)

        workersMaxThreads = confProps.getProperty("server.worker.max_threads", "10").toInt

        zkEnable = confProps.getProperty("zookeeper.enable", "true").toBoolean
        zkHosts = confProps.getProperty("zookeeper.hosts", "localhost:2181")

        cache_type = confProps.getProperty("cache.type", "redis")

        driver_mysql = driver_mysql.+("mysql.db.host" -> confProps.getProperty("mysql.db.host", "localhost"))
        driver_mysql = driver_mysql.+("mysql.db.port" -> confProps.getProperty("mysql.db.port", "3306"))
        driver_mysql = driver_mysql.+("mysql.db.username" -> confProps.getProperty("mysql.db.username", "root"))
        driver_mysql = driver_mysql.+("mysql.db.password" -> confProps.getProperty("mysql.db.password", ""))
    }

    def parseClientConf(confProps: Properties): Unit ={

        _confProps = confProps

        clientId = confProps.getProperty("client.id", "0")

        zkEnable = confProps.getProperty("zookeeper.enable", "true").toBoolean
        zkHosts = confProps.getProperty("zookeeper.hosts", "localhost:2181")

    }

    def verifyConf(): Boolean ={
        var result = true

        if(serverId == "")
            result = false

        result
    }

    def getConfigure(key: String): String ={
        var value = ""

        try {

            if(_confProps != null && _confProps.containsKey(key))
                value = _confProps.getProperty(key, "")

        } catch {
            case e: Exception =>
                log.error("There throws exception when load configure file:", e)
        }

        value
    }
}
