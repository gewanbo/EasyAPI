package com.wanbo.easyapi.shared.common.libs

import java.io.File
import java.util.Properties

import org.slf4j.LoggerFactory

import scala.xml.XML

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
    var serverMode: String = _
    var serverUIPort: Int = _

    var workersPort: List[Int] = _
    var workersMaxThreads: Int = _
    var workersClasses: String = _

    var cache_type = "redis"

    var driverSettings = Map[String, Map[String, String]]()

    // Client
    var clientId: String = _
    var clientHost: String = _
    var clientPort: Int = _

    var minThreads: Int = _
    var maxThreads: Int = _

    private var _confProps: Properties = null

    private val log = LoggerFactory.getLogger(classOf[EasyConfig])

    def parseServerConf(confProps: Properties): Unit ={

        _confProps = confProps

        serverId = confProps.getProperty("server.id", "0")
        serverHost = confProps.getProperty("server.host", "localhost")
        serverPort = confProps.getProperty("server.port", "8860").toInt
        serverUIPort = confProps.getProperty("server.ui.port", "8800").toInt

        serverMode = confProps.getProperty("server.mode", "Standalone")

        val workers_port = confProps.getProperty("server.worker.port", "8801")

        workersPort = workers_port.split(";").toList.map(_.toInt)

        workersMaxThreads = confProps.getProperty("server.worker.max_threads", "10").toInt

        workersClasses = confProps.getProperty("server.worker.classes", "com.wanbo.easyapi.server.workers.Seeder_")

        zkEnable = confProps.getProperty("zookeeper.enable", "true").toBoolean
        zkHosts = confProps.getProperty("zookeeper.hosts", "localhost:2181")

        cache_type = confProps.getProperty("cache.type", "redis")

        val dbConf = confProps.getProperty("database.conf", "database.xml")
        
        try {
            val confFile = new File("../conf/" + dbConf)
            
            if(confFile.exists()){
                val xml = XML.loadFile(confFile)

                for (node <- xml \ "property") {
                    val dbType = (node \ "@type").text
                    dbType match {
                        case "mysql" =>

                            val settings = Map(
                                "type" -> dbType,
                                "host" -> (node \\ "host").text,
                                "port" -> (node \\ "port").text,
                                "uname" -> (node \\ "uname").text,
                                "upswd" -> (node \\ "upswd").text,
                                "dbname" -> (node \\ "dbname").text,
                                "writable" -> (node \ "@writable").text
                            )

                            val settingKey = settings.hashCode().toString

                            driverSettings = driverSettings.+(settingKey -> settings)

                        case "hbase" =>

                            val settings = Map("type" -> dbType, "zk" -> (node \\ "zookeeper").text)

                            val settingKey = settings.hashCode().toString

                            driverSettings = driverSettings.+(settingKey -> settings)

                        case "mongo" =>

                            val settings = Map(
                                "type" -> dbType,
                                "host" -> (node \\ "host").text,
                                "port" -> (node \\ "port").text,
                                "dbname" -> (node \\ "dbname").text
                            )

                            val settingKey = settings.hashCode().toString

                            driverSettings = driverSettings.+(settingKey -> settings)

                        case _ => // Ignore setup error.
                    }
                }
            }
        } catch {
            case e: Exception => // Ignore the file opening exceptions.
        }
        
    }

    def parseClientConf(confProps: Properties): Unit ={

        _confProps = confProps

        clientId = confProps.getProperty("client.id", "0")
        clientHost = confProps.getProperty("client.host", "localhost")
        clientPort = confProps.getProperty("client.port", "8890").toInt

        minThreads = confProps.getProperty("client.min_threads", "10").toInt
        maxThreads = confProps.getProperty("client.max_threads", "100").toInt

        zkEnable = confProps.getProperty("zookeeper.enable", "true").toBoolean
        zkHosts = confProps.getProperty("zookeeper.hosts", "localhost:2181")

    }

    def verifyConf(): Boolean ={
        var result = true

        if(serverId == "")
            result = false

        if(driverSettings.size < 1)
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
