package com.wanbo.easyapi.server.actors

import akka.actor.Actor
import akka.io.Tcp.{Close, PeerClosed, Write, Received}
import akka.util.ByteString
import com.wanbo.easyapi.server.lib.SeederManager
import com.wanbo.easyapi.server.messages.Seed
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.slf4j.LoggerFactory

/**
 * Worker
 * Created by wanbo on 15/4/3.
 */
class Worker(conf: EasyConfig) extends Actor {

    private val log = LoggerFactory.getLogger(classOf[Worker])

    override def receive: Receive = {
        case Seed(client) =>
            log.info("I'm working ...")

        case Received(data) =>
            var msgBody = ""

            val msgData = data.decodeString("UTF-8")
            msgData.split("\r").foreach(x => {
                val body = x.trim
                if(body.startsWith("{") && body.endsWith("}"))
                    msgBody = body
            })

            val fruits = process(msgBody)

            val responseBody = "HTTP/1.1 200 OK\nContent-Type: application/json\n" + "\n" + fruits

            sender() ! Write(ByteString.fromString(responseBody, "UTF-8"))
            sender() ! Close
        case PeerClosed =>
            context stop self
    }

    def process(message: String): String = {

        var fruits = ""

        try {

            log.info("Receive a message:")

            if (message == "") {
                throw new Exception("Request body is empty!")
            }

            val seederManager = new SeederManager(conf, message)

            fruits = seederManager.farming()

        } catch {
            case e: Exception =>
                fruits = """{"errorcode": 99999, "errormsg": "%s"}""".format(e.getMessage)
                log.error("Message worker process exception :", e)
        }

        log.info("Input message is:" + message)
        log.info("Output message is:" + fruits.replaceFirst("\\[.*\\]", "[...more...]"))

        fruits
    }
}
