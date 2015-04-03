package com.wanbo.easyapi.server.messages

import java.net.Socket

/**
 * Manager command message
 * Created by wanbo on 15/4/3.
 */
case class ManagerCommand(client: Socket) extends SystemMessage
