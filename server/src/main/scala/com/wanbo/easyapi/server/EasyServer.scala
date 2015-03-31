package com.wanbo.easyapi.server

import java.io.FileInputStream
import java.net.{ServerSocket, Socket}
import java.util.Properties

import org.slf4j.LoggerFactory

import scala.actors.threadpool.Executors

/**
 * The server of EasyAPI
 * Created by wanbo on 15/3/30.
 */
object EasyServer {

    private val logger = LoggerFactory.getLogger(EasyServer.getClass.getSimpleName)

    var port = "8800"

    def main(args: Array[String]) {

        try {
            logger.info("Starting up ...")

            val properties = new Properties()
            val configFile = System.getProperty("easy.conf", "config.properties")

            properties.load(new FileInputStream(configFile))

            port = properties.getProperty("server.port", "8800")

            val socket = new ServerSocket(port.toInt)

            val execService = Executors.newFixedThreadPool(properties.getProperty("server.max_threads", "10").toInt)

            var client: Socket = socket.accept()
            while (client != null) {

                execService.submit(new Worker(client))

                // Accept next message
                client = socket.accept()
            }

            client.close()
            logger.info("Easyapi server stop done.")
        } catch {
            case e: Exception =>
                logger.error("Error:", e)
        }
    }
}