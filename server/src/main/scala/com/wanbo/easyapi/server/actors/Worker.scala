package com.wanbo.easyapi.server.actors

import java.io.{PrintWriter, InputStreamReader, BufferedReader}
import java.net.Socket

import akka.actor.Actor
import com.wanbo.easyapi.server.messages.{Seed, ShutDown}
import org.slf4j.LoggerFactory

/**
 * Worker
 * Created by wanbo on 15/4/3.
 */
class Worker extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[Worker])

    override def receive: Receive = {
        case Seed(client) =>
            println("I'm working ...")
            val ret = process(client)

            if(ret.trim == "shutdown")
                sender() ! ShutDown
    }

    def process(client: Socket): String = {

        var message = ""

        try {

            logger.info("Receive a message:")

            // In message
            val in = new BufferedReader(new InputStreamReader(client.getInputStream))

            // Out message
            val out = new PrintWriter(client.getOutputStream, true)

            message = in.readLine()

            logger.info("Message body is :" + message)

            println(message)

            // Response message
            out.println(message + " - OK")

            out.close()
            in.close()
            client.close()
        } catch {
            case e: Exception =>
                logger.error("Message worker process exception :", e)
        }

        message
    }
}
