package com.wanbo.easyapi.server

import java.io.{InputStreamReader, BufferedReader, PrintWriter}
import java.net.Socket

import org.slf4j.LoggerFactory

/**
 * Worker
 * Created by wanbo on 15/3/30.
 */
class Worker(client: Socket) extends Runnable {

    private val logger = LoggerFactory.getLogger(classOf[Worker])

    override def run(): Unit = {

        try {

            logger.info("Receive a message:")

            // In message
            val in = new BufferedReader(new InputStreamReader(client.getInputStream))

            // Out message
            val out = new PrintWriter(client.getOutputStream, true)

            val line = in.readLine()

            logger.info("Message body is :" + line)

            println(line)

            // Response message
            out.println(line)

            out.close()
            in.close()
            client.close()
        } catch {
            case e: Exception =>
                logger.error("Message worker process exception :", e)
        }
    }
}
