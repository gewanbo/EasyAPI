package com.wanbo.easyapi.ui

import com.wanbo.easyapi.shared.common.libs.EasyConfig
import com.wanbo.easyapi.ui.lib.HttpServer
import org.slf4j.LoggerFactory

/**
 * The UI of api server.
 * Created by wanbo on 15/8/21.
 */
object EasyUI {

    private val log = LoggerFactory.getLogger(EasyUI.getClass.getSimpleName)

    def main(args: Array[String]) {

        val conf = new EasyConfig

        val httpServer = new HttpServer(conf)
        httpServer.start()

        Runtime.getRuntime.addShutdownHook(new Thread(){
            override def run(): Unit = {
                log.info("Shutting down ......")
                httpServer.stop()
            }
        })
    }
}
