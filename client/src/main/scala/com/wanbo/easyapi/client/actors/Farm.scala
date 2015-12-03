package com.wanbo.easyapi.client.actors

import akka.actor.Actor
import akka.io.Tcp.{Close, Write, PeerClosed, Received}
import akka.util.ByteString
import com.wanbo.easyapi.client.lib.{WorkCounter, AvailableServer}
import org.slf4j.LoggerFactory

import scala.util.Random

/**
 * Farm
 * Created by wanbo on 2015/8/17.
 */
class Farm extends Actor {

    private val log = LoggerFactory.getLogger(classOf[Farm])

    private val jsonHeader = "HTTP/1.1 200 OK\nContent-Type: application/json\n"
    private val textHeader = "HTTP/1.1 200 OK\nContent-Type: text/plain\n"

    override def receive: Receive = {
        case Received(data) =>

            log.info("Received a request ...")

            val msgData = data.decodeString("UTF-8")

            try {

                var responseBody = ""



                var msgBody = ""

                log.info("---------------Body:" + msgData)

                var mark = false
                msgData.split("\r").foreach(x => {
                    val body = x.trim
                    if(body.isEmpty)
                        mark = true
                    if (mark)
                        msgBody += body
                })

                log.info("---------------K:" + msgBody)
                log.info("---------------K:" + msgBody.isEmpty)

                if (msgBody.isEmpty) {
                    responseBody = onAvailableServer()
                } else {

                    if (msgBody.startsWith("{miss")) {
                        responseBody = onMiss(msgBody)
                    }

                }

                sender() ! Write(ByteString.fromString(responseBody, "UTF-8"))
                sender() ! Close

            } catch {
                case e: Exception =>
                    log.error("Error:", e)
                    sender() ! Write(ByteString.fromString("HTTP/1.1 200 OK\nContent-Type: application/json\n" + "\n", "UTF-8"))
                    sender() ! Close
            }

        case PeerClosed =>
            context stop self
    }

    private def onAvailableServer(): String ={

        // Get current server list
        val servers = AvailableServer.serverList

        // return the best one
        var serverText = ""

        if (servers != null && servers.size > 0) {
            if (servers.size > 2) {
                // Remove the biggest one, and random one form the rest.
                val biggest = servers.maxBy(_._2)
                val restServers = servers.filter(x => x != biggest)
                serverText = Farm.randomUniqueOneServer(restServers)
            } else {
                serverText = Farm.randomUniqueOneServer(servers)
            }
        }

        if (serverText == "") {
            // Alarm
            log.error("Didn't find available server!")
        } else {
            log.info("The best server is:" + serverText)
        }

        textHeader + "\n" + serverText
    }

    private def onMiss(msgBody: String): String = {
        var server = ""
        val fields = msgBody.substring(1, msgBody.size - 1).split("#")

        if (fields.size > 1) {
            server = fields(1)

            AvailableServer.serverList.foreach(println)

            if (server != "" && AvailableServer.serverList.contains(server)) {
                // Get current server list
                WorkCounter.push(server)
            }
        }

        jsonHeader + "\n"
    }
}

object Farm {
    private var lastServer = ""

    /**
     * Generate a server name different with last time.
     * @param serverList Server list.
     * @return
     */
    private def randomUniqueOneServer(serverList: Map[String, Long]): String ={
        var availableServer = ""

        if(serverList.size > 0){
            if(serverList.size == 1){
                availableServer = serverList.head._1
            } else {
                val restList = serverList.filter(_._1 != lastServer)
                availableServer = Random.shuffle(restList).head._1
            }
        }

        lastServer = availableServer

        availableServer
    }
}