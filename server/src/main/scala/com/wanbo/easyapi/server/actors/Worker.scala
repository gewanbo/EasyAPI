package com.wanbo.easyapi.server.actors

import java.io.{PrintWriter, InputStreamReader, BufferedReader}
import java.net.Socket

import akka.actor.Actor
import com.wanbo.easyapi.server.lib.SeederManager
import com.wanbo.easyapi.server.messages.{Seed, ShutDown}
import org.slf4j.LoggerFactory

/**
 * Worker
 * Created by wanbo on 15/4/3.
 */
class Worker extends Actor {

    private val log = LoggerFactory.getLogger(classOf[Worker])

    override def receive: Receive = {
        case Seed(client) =>
            log.info("I'm working ...")
            val ret = process(client)

    }

    def process(client: Socket): String = {

        var message = ""

        try {

            log.info("Receive a message:")

            // In message
            val in = new BufferedReader(new InputStreamReader(client.getInputStream))

            // Out message
            val out = new PrintWriter(client.getOutputStream, true)

            message = in.readLine()

            log.info("Message body is :" + message)

            val seederManager = new SeederManager(message)

            val fruits = seederManager.farming()

            // Response message
            out.println(fruits)

            out.close()
            in.close()
            client.close()
        } catch {
            case e: Exception =>
                log.error("Message worker process exception :", e)
        }

        message
    }
}
