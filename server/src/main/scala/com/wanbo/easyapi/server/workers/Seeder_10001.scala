package com.wanbo.easyapi.server.workers

import com.alibaba.fastjson.JSONObject
import com.wanbo.easyapi.server.lib.{Seeder, ISeeder}

/**
 * Number 10001 seeder
 * Created by GWB on 2015/4/8.
 */
case class Seeder_10001() extends Seeder with ISeeder {
    override def onHandle(seed: JSONObject): JSONObject = {
        fruits = new JSONObject()

        fruits.put("messageid", "msg102039")

        fruits
    }

    override def onDBHandle(): Unit = ???
}
