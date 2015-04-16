package com.wanbo.easyapi.server.lib

import com.alibaba.fastjson.JSONObject

/**
 * The abstract class of seeders
 * Created by GWB on 2015/4/8.
 */
abstract class Seeder {

    var name: String = _

    protected var fruits: JSONObject = new JSONObject()

    protected def onDBHandle(): Any
}
