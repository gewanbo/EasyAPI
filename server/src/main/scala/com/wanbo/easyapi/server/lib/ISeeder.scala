package com.wanbo.easyapi.server.lib

import com.alibaba.fastjson.JSONObject
import com.wanbo.easyapi.server.database.Driver

/**
 * The trait of workers
 * Created by GWB on 2015/4/8.
 */
trait ISeeder {
    var _conf: EasyConfig = _

    var driver: Driver = _

    var isUpdateCache = false

    def onHandle(seed: JSONObject): JSONObject
}
