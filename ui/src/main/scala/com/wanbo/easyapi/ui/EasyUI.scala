package com.wanbo.easyapi.ui

import java.io.FileInputStream
import java.util.Properties

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import com.wanbo.easyapi.ui.lib.{EasyUI, HttpServer}
import org.eclipse.jetty.server.handler.{ContextHandler, ResourceHandler}
import org.eclipse.jetty.servlet.ServletContextHandler
import org.slf4j.LoggerFactory

/**
 * The UI of api server.
 * Created by wanbo on 15/8/21.
 */
object EasyUI extends Logging {

    def main(args: Array[String]) {

        log.info("Easyapi server ui is starting up ...")

        val conf = new EasyConfig

        val confProps = new Properties()
        val configFile = System.getProperty("easy.conf", "config.properties")
        confProps.load(new FileInputStream(configFile))

        // Load configuration
        conf.parseServerConf(confProps)

        val easyUI = new EasyUI(conf)

        easyUI.start()

        Runtime.getRuntime.addShutdownHook(new Thread(){
            override def run(): Unit = {
                log.info("Shutting down ......")
                easyUI.stop()
            }
        })
    }
}
