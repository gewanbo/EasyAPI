package com.wanbo.easyapi.server.lib

import com.alibaba.fastjson.JSONObject
import com.wanbo.easyapi.server.database.Driver

/**
 * The trait of workers
 * Created by GWB on 2015/4/8.
 */
trait ISeeder {
    var driver: Driver = _

    def onHandle(seed: JSONObject): JSONObject
}
