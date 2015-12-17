package com.wanbo.easyapi.client.lib

import com.wanbo.easyapi.shared.common.Logging

import scala.util.Random

/**
 * Available server class.
 * Created by wanbo on 15/8/18.
 */
object AvailableServer extends Logging {
    var serverList: Map[String, Long] = Map[String, Long]()

    private var lastServer = ""

    def availableServer: String ={
        // Get current server list
        val servers = serverList

        // return the best one
        var serverText = ""

        if (servers != null && servers.nonEmpty) {
            if (servers.size > 2) {
                // Remove the biggest one, and random one form the rest.
                val biggest = servers.maxBy(_._2)
                val restServers = servers.filter(x => x != biggest)
                serverText = randomUniqueOneServer(restServers)
            } else {
                serverText = randomUniqueOneServer(servers)
            }
        }

        if (serverText == "") {
            // Alarm
            log.error("Didn't find available server!")
        } else {
            log.info("The best server is:" + serverText)
        }

        serverText
    }

    /**
     * Generate a server name different with last time.
     * @param serverList Server list.
     * @return
     */
    private def randomUniqueOneServer(serverList: Map[String, Long]): String ={
        var availableServer = ""

        if(serverList.nonEmpty){
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
