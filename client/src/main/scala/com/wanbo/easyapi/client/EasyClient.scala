package com.wanbo.easyapi.client

import akka.actor.{Props, ActorSystem}
import com.wanbo.easyapi.client.actors.FarmWatcher
import org.slf4j.LoggerFactory

/**
 * EasyClient
 * Created by wanbo on 15/8/13.
 */
object EasyClient {

    private val log = LoggerFactory.getLogger(EasyClient.getClass.getSimpleName)

    def main(args: Array[String]) {

        log.info("Easyapi client start up ...")

        val system = ActorSystem("System")
        val farmWatcher = system.actorOf(Props(new FarmWatcher()), name = "FarmWatcher")

        farmWatcher ! "StartUp"

        Runtime.getRuntime.addShutdownHook(new Thread(){
            override def run(): Unit = {
                log.info("Shutting down ......")
                farmWatcher ! "ShutDown"
            }
        })

    }
}