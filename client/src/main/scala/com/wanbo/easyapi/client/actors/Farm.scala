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

    override def receive: Receive = {
        case Received(data) =>

            log.info("Received a request ...")

            var msgBody = data.decodeString("UTF-8")

            val msgData = data.decodeString("UTF-8")
            msgData.split("\r").foreach(x => {
                val body = x.trim
                if(body.startsWith("{") && body.endsWith("}"))
                    msgBody = body
            })

            try {

                if (msgBody.startsWith("{miss")) {

                    var server = ""
                    val fields = msgBody.substring(1, msgBody.size -1).split("#")

                    if (fields.size > 1) {
                        server = fields(1)

                        AvailableServer.serverList.foreach(println)

                        if (server != "" && AvailableServer.serverList.contains(server)) {
                            // Get current server list
                            WorkCounter.push(server)
                        }
                    }

                    val responseBody = "HTTP/1.1 200 OK\nContent-Type: application/json\n" + "\n"

                    sender() ! Write(ByteString.fromString(responseBody, "UTF-8"))
                    sender() ! Close
                } else {

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

                    val responseBody = "HTTP/1.1 200 OK\nContent-Type: application/json\n" + "\n" + serverText

                    sender() ! Write(ByteString.fromString(responseBody, "UTF-8"))
                    sender() ! Close
                }
            } catch {
                case e: Exception =>
                    log.error("Error:", e)
                    sender() ! Write(ByteString.fromString("HTTP/1.1 200 OK\nContent-Type: application/json\n" + "\n", "UTF-8"))
                    sender() ! Close
            }

        case PeerClosed =>
            context stop self
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