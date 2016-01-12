package com.wanbo.easyapi.server.messages

/**
 * Cache update message
 * Created by wanbo on 2016/1/11.
 */
case class CacheUpdate(seeder: String = "", seed: Map[String, Any] = Map[String, Any]()) extends MsgQee
