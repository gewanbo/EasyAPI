package com.wanbo.easyapi.server.actors

import akka.actor.Actor
import com.wanbo.easyapi.server.lib.SeederManager
import com.wanbo.easyapi.server.messages.CacheUpdate
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig

/**
 * The worker for consuming message.
 * Created by wanbo on 16/3/1.
 */
class ConsumerWorker(conf: EasyConfig) extends Actor with Logging {
    override def receive: Receive = {
        case CacheUpdate(seeder, seed) =>

            log.info("----------- Dynamic to update working .... .... !")
            log.info(seeder.toString)

            val seederManager = new SeederManager(conf, "")
            val ret = seederManager.updateCache(seeder, seed)

        case _ =>
            log.info("Unknown message.!!!")
    }
}
