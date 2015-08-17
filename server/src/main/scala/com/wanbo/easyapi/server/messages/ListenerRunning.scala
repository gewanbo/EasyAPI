package com.wanbo.easyapi.server.messages

import akka.actor.ActorContext
import com.wanbo.easyapi.shared.common.libs.EasyConfig

/**
 * Listener running message
 * Created by wanbo on 15/4/3.
 */
case class ListenerRunning(conf: EasyConfig,workers: ActorContext) extends SystemMessage
