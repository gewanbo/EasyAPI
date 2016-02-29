package com.wanbo.easyapi.server.routing

import akka.actor.{Props, ActorSystem}
import com.wanbo.easyapi.shared.common.libs.EasyConfig

/**
 * Test
 * Created by wanbo on 16/2/29.
 */
object RunTest {
    def main(args: Array[String]) {
        println("Starting up!")

        val conf = new EasyConfig()
        conf.serverHost = "localhost"
        conf.workersMaxThreads = 2

        val system = ActorSystem()
        val watcher = system.actorOf(Props(new Watcher(conf)), name = "watcher")

        watcher ! "start"

        Runtime.getRuntime.addShutdownHook(new Thread(){
            override def run(): Unit = {
                println("Shutting down!")
            }
        })
    }
}
