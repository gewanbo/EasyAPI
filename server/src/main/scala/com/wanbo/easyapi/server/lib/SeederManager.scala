package com.wanbo.easyapi.server.lib

import com.alibaba.fastjson.{JSONObject, JSON}
import com.wanbo.easyapi.server.workers.Seeder_10001
import org.slf4j.LoggerFactory


/**
 * The manager of all types workers
 * Created by GWB on 2015/4/8.
 */
class SeederManager(seed: String) {

    private val log = LoggerFactory.getLogger(classOf[SeederManager])

    private var _seed: JSONObject = _

    private def loadSeed(): Unit = {
        try {
            _seed = JSON.parseObject(seed)
        } catch {
            case e: Exception =>
                log.error("Throws exception when parse seed:", e)
        }
    }

    def farming(): String ={

        loadSeed()

        val fruites = Seeder_10001.apply().onHandle(_seed)

        fruites.toJSONString
    }
}
