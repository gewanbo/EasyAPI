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
    private var transactionType: String = _

    private var _seed: JSONObject = _
    private var _fruit: JSONObject = _

    private def loadSeed(): Unit = {
        try {
            _seed = JSON.parseObject(seed)

            messageid = _seed.getString("messageid")

            if(messageid == null)
                messageid = "139287742832"

            transactionType = _seed.getString("transactiontype")

//            if(transactionType == null || transactionType == "")
//                throw new Exception("Can't find the TransactionType!")





        } catch {
            case je: JSONException =>
                log.warn("The seed string is :" + _seed)
                log.error("Throws exception when parse seed:", je)
            case e: Exception =>
                log.error("The seed string is :" + _seed, e)
        }
    }

    def farming(): String ={

        loadSeed()

        val fruits = Seeder_10001.apply().onHandle(_seed)


        _fruit = new JSONObject()
        val head = new JSONObject()
        head.put("messageid", messageid)
        head.put("transactiontype", transactionType)

        val body = new JSONObject()
        val oelement = new JSONObject()
        oelement.put("errorcode", 0)
        oelement.put("errormsg", "")
        body.put("oelement", oelement)
        body.put("odata", {fruits})


        _fruit.put("head", head)
        _fruit.put("body", body)

        _fruit.toJSONString
    }
}
