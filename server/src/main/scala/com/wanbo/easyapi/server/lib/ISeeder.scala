package com.wanbo.easyapi.server.lib

import com.alibaba.fastjson.JSONObject

/**
 * The trait of workers
 * Created by GWB on 2015/4/8.
 */
trait ISeeder {
    def onHandle(seed: JSONObject): JSONObject
}
