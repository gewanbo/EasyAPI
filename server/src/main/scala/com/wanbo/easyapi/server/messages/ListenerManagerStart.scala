package com.wanbo.easyapi.server.messages

import com.wanbo.easyapi.server.lib.EasyConfig

/**
 * Start listener message
 * Created by wanbo on 15/4/3.
 */
case class ListenerManagerStart(conf: EasyConfig) extends SystemMessage
