package com.wanbo.easyapi.server.messages

import java.net.Socket

/**
 * Seed message
 * Created by wanbo on 15/4/3.
 */
case class Seed(client: Socket) extends IMessage
