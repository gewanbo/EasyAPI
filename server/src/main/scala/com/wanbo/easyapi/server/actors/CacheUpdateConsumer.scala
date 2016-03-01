package com.wanbo.easyapi.server.actors

import akka.actor.{Props, Actor}
import akka.routing.{RoundRobinPool, DefaultResizer}
import com.wanbo.easyapi.server.lib.{MessageQ, SeederManager}
import com.wanbo.easyapi.server.messages.CacheUpdate
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.slf4j.MDC

import scala.actors.threadpool.Executors

/**
 * The consumer for update cache
 * Created by wanbo on 16/1/12.
 */
class CacheUpdateConsumer(conf: EasyConfig) extends Actor with Logging {

    private val maxThread = 3

    val threadPool = Executors.newFixedThreadPool(maxThread)

    val reSizer = DefaultResizer(lowerBound=3, upperBound = 30)
    val consumers = context.actorOf(Props(new ConsumerWorker(conf)).withRouter(RoundRobinPool(10).withResizer(reSizer)), name = "MessageConsumer")

    for(i <- 1 until maxThread) {

        threadPool.submit(new Runnable {
            override def run(): Unit = {
                try {

                    val qName = "UpdateCache"

                    while (true) {
                        val msg = MessageQ.pull(qName)

                        if (msg != null && msg.isInstanceOf[CacheUpdate]) {

                            val msgObj = msg.asInstanceOf[CacheUpdate]

                            consumers ! msgObj

//                            MDC.put("destination", "cache")
//                            log.info("Update cache ....")
//
//                            val seederManager = new SeederManager(conf, "")
//                            val ret = seederManager.updateCache(msgObj.seeder, msgObj.seed)
//                            MDC.put("destination", "cache")
//                            log.info(ret.oelement.toString())
//                            MDC.clear()

                            log.info("Current message queue size: " + MessageQ.getSize(qName))
                        } else {
                            Thread.sleep(500)
                        }

                    }

                } catch {
                    case e: Exception =>
                        log.error("Error:", e)
                }
            }
        })

    }

    override def receive: Receive = {

        case _ =>
            log.info("Unknown message.!!!")
    }
}
