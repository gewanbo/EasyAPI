package com.wanbo.easyapi.ui

import java.io.FileInputStream
import java.util.Properties

import com.wanbo.easyapi.shared.common.libs.EasyConfig
import com.wanbo.easyapi.ui.lib.HttpServer
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.slf4j.LoggerFactory

/**
 * The UI of api server.
 * Created by wanbo on 15/8/21.
 */
object EasyUI {

    private val log = LoggerFactory.getLogger(EasyUI.getClass.getSimpleName)

    def main(args: Array[String]) {

        log.info("Easyapi server ui is starting up ...")

        val conf = new EasyConfig

        val confProps = new Properties()
        val configFile = System.getProperty("easy.conf", "config.properties")
        confProps.load(new FileInputStream(configFile))

        // Load configuration
        conf.parseServerConf(confProps)

        val server = new HttpServer(conf)

        val resourceHandler = new ResourceHandler
        resourceHandler.setDirectoriesListed(true)
        resourceHandler.setResourceBase("../webapp/static")

        val staticContext = new ServletContextHandler()
        staticContext.setContextPath("/static")
        staticContext.setHandler(resourceHandler)

        server.attachHandler(staticContext)

        val context = new ServletContextHandler()
        context.setContextPath("/")
        context.setHandler(new HomeHandler(conf))

        server.attachHandler(context)
        server.start()

        Runtime.getRuntime.addShutdownHook(new Thread(){
            override def run(): Unit = {
                log.info("Shutting down ......")
                server.stop()
            }
        })
    }
}
