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
                    serverText = Random.shuffle(restServers).head._1
                } else {
                    serverText = Random.shuffle(servers).head._1
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
