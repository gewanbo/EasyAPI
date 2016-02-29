package com.wanbo.easyapi.server.routing

import akka.actor.Actor
import akka.io.Tcp.{Close, PeerClosed, Received}
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig

/**
 * Worker
 * Created by wanbo on 16/2/29.
 */
class Worker(conf: EasyConfig) extends Actor with Logging {

    override def receive: Receive ={

        case Received(data) =>
            log.info("Received a message!")
            log.info("Message:" + data.toString())

            sender() ! Close

            //context.stop(self)

        case PeerClosed =>
            context stop self
    }
}
