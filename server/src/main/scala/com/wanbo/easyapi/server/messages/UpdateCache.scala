package com.wanbo.easyapi.server.messages

/**
 * Update cache message.
 * Created by wanbo on 15/5/6.
 */
case class UpdateCache(Id: String, Params: Map[String, Any]) extends SystemMessage