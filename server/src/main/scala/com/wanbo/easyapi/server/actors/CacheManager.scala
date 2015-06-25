package com.wanbo.easyapi.server.actors

import akka.actor.Actor
import com.wanbo.easyapi.server.lib.{SeederManager, EasyConfig}
import com.wanbo.easyapi.server.messages.UpdateCache
import org.slf4j.{MDC, LoggerFactory}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._

/**
 * Cache update manager
 * Created by wanbo on 15/5/6.
 */
class CacheManager(conf: EasyConfig) extends Actor {

    import context.system

    val log = LoggerFactory.getLogger(classOf[CacheManager])

    MDC.put("destination", "cache")

    override def receive: Receive = {

        case "init" =>

            system.scheduler.schedule(10 seconds, 200 seconds, self, UpdateCache("10003", Map("days" ->"7", "num" ->"10")))

        case UpdateCache(x, y) =>
            log.info("Update cache ....")

            log.info("-----" + x + " " + y)

            val seederManager = new SeederManager(conf, "")
            val ret = seederManager.updateCache(x, y)
            log.info(ret.oelement.toString())
    }
}
