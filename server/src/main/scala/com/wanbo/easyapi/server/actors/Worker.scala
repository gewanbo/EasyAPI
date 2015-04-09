package com.wanbo.easyapi.server.actors

import java.io.{PrintWriter, InputStreamReader, BufferedReader}
import java.net.Socket

import akka.actor.Actor
import com.wanbo.easyapi.server.lib.SeederManager
import com.wanbo.easyapi.server.messages.Seed
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

            var fruits = ""

            try {
                var msgLength = 10
                //val headers = in.readLine()
                message = in.readLine().trim

                while (msgLength > 0 && message != null && !message.startsWith("{") && !message.endsWith("}")) {
                    msgLength -= 1
                    println(message)
                    message = in.readLine().trim
                }

                if (message == "") {
                    throw new Exception("Request body is empty!")
                }

                val seederManager = new SeederManager(message)

                fruits = seederManager.farming()

            } catch {
                case e: Exception =>
                    fruits = """{"errorcode": 99999, "errormsg": %s}""".format(e.getMessage)
            }
            // Response message
            out.println(fruits)

            log.info("Input message is:" + message)
            log.info("Output message is:" + fruits)

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
