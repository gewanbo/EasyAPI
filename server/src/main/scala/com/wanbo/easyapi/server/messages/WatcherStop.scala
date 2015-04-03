package com.wanbo.easyapi.server.messages

import java.util.Properties

/**
 * Stop listener message
 * Created by wanbo on 15/4/3.
 */
case class WatcherStop(conf: Properties) extends SystemMessage
