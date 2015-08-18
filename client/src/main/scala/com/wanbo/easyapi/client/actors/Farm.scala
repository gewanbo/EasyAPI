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

            log.info("Receive a request ...")

            // Get current server list
            val servers = AvailableServer.serverList

            // return the best one
            var serverText = ""

            if(servers.size > 0) {
                serverText = Random.shuffle(servers).head
            }

            log.info("The best server is:" + serverText)

            val responseBody = "HTTP/1.1 200 OK\nContent-Type: application/json\n" + "\n" + serverText

            sender() ! Write(ByteString.fromString(responseBody, "UTF-8"))
            sender() ! Close

        case PeerClosed =>
            context stop self
    }
}
