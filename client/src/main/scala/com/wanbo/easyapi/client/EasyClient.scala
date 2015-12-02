package com.wanbo.easyapi.client

import java.io.FileInputStream
import java.util.Properties

import akka.actor.{Props, ActorSystem}
import com.wanbo.easyapi.client.actors.FarmWatcher
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.slf4j.LoggerFactory

/**
 * EasyClient
 * Created by wanbo on 15/8/13.
 */
object EasyClient {

    private val log = LoggerFactory.getLogger(EasyClient.getClass.getSimpleName)

    def main(args: Array[String]) {

        log.info("Easyapi client start up ...")

        val conf = new EasyConfig()

        try{

            // Initialize configuration
            val confProps = new Properties()
            val configFile = System.getProperty("easy.conf", "config.properties")
            confProps.load(new FileInputStream(configFile))

            // Load configuration
            conf.parseClientConf(confProps)

        } catch {
            case e: Exception =>
                log.error("Load configure file throws exception:", e)
        }

        val system = ActorSystem("System")
        val farmWatcher = system.actorOf(Props(new FarmWatcher(conf)), name = "FarmWatcher")

        farmWatcher ! "StartUp"

        Runtime.getRuntime.addShutdownHook(new Thread(){
            override def run(): Unit = {
                log.info("Shutting down ......")
                farmWatcher ! "ShutDown"
                try {
                    Thread.sleep(3000)
                } catch {
                    case e: Exception => // Ignore
                }
            }
        })

    }
}