package com.wanbo.easyapi.client.actors

import akka.actor.Actor
import akka.io.Tcp.{Close, Write, PeerClosed, Received}
import akka.util.ByteString
import com.wanbo.easyapi.client.lib.AvailableServer
import org.slf4j.LoggerFactory

import scala.util.Random

/**
 * Farm
 * Created by wanbo on 15/8/17.
 */
class Farm extends Actor {

    private val log = LoggerFactory.getLogger(classOf[Farm])

    override def receive: Receive = {
        case Received(data) =>

            log.info("Received a request ...")

            // Get current server list
            val servers = AvailableServer.serverList

            // return the best one
            var serverText = ""

            if(servers != null && servers.size > 0) {
                if(servers.size > 2) {
                    // Remove the biggest one, and random one form the rest.
                    val biggest = servers.maxBy(_._2)
                    val restServers = servers.filter(x => x != biggest)
                    serverText = Farm.randomUniqueOneServer(restServers)
                } else {
                    serverText = Farm.randomUniqueOneServer(servers)
                }
            }

            if(serverText == ""){
                // Alarm
                log.error("Didn't find available server!")
            } else {
                log.info("The best server is:" + serverText)
            }

            val responseBody = "HTTP/1.1 200 OK\nContent-Type: application/json\n" + "\n" + serverText

            sender() ! Write(ByteString.fromString(responseBody, "UTF-8"))
            sender() ! Close

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