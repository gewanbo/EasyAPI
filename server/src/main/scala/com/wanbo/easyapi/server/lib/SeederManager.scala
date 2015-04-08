package com.wanbo.easyapi.server.lib

import com.alibaba.fastjson.{JSONException, JSONObject, JSON}
import com.wanbo.easyapi.server.workers.Seeder_10001
import org.slf4j.LoggerFactory


/**
 * The manager of all types workers
 * Created by GWB on 2015/4/8.
 */
class SeederManager(seed: String) {

    private val log = LoggerFactory.getLogger(classOf[SeederManager])

    private var messageid: String = _

    private var _seed: JSONObject = _
    private var _fruit: JSONObject = _

    private def loadSeed(): Unit = {
        try {
            _seed = JSON.parseObject(seed)

            messageid = _seed.getString("messageid")

            if(messageid == null)
                messageid = "139287742832"

            _fruit = new JSONObject()
            val head = new JSONObject()
            head.put("messageid", messageid)
            _fruit.put("head", head)
        } catch {
            case je: JSONException =>
                log.warn("The seed string is :" + _seed)
                log.error("Throws exception when parse seed:", je)
            case _ =>
                log.warn("The seed string is :" + _seed)
        }
    }

    def farming(): String ={

        loadSeed()

        val fruits = Seeder_10001.apply().onHandle(_seed)

        _fruit.put("body", fruits)

        _fruit.toJSONString
    }
}
