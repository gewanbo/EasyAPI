package com.wanbo.easyapi.server.workers

import com.wanbo.easyapi.server.lib.{EasyOutput, Seeder, ISeeder}

/**
 * Number 10001 seeder
 * Created by GWB on 2015/4/8.
 */
final class Seeder_10001() extends Seeder with ISeeder {

    name = "10001"

    override def onHandle(seed: Map[String, Any]): EasyOutput = {
        fruits = new EasyOutput()

        //fruits.put("messageid", "msg102039")

        fruits
    }

    override def onDBHandle(): Unit = ???
}
