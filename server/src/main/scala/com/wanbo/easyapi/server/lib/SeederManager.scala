package com.wanbo.easyapi.server.lib

import com.alibaba.fastjson.{JSONException, JSONObject, JSON}
import com.wanbo.easyapi.server.database.{HBaseDriver, MysqlDriver}
import org.slf4j.LoggerFactory

/**
 * The manager of all types workers
 * Created by GWB on 2015/4/8.
 */
class SeederManager(conf: EasyConfig, seed: String) {

    private val log = LoggerFactory.getLogger(classOf[SeederManager])

    private var messageid: String = _
    private var transactionType: String = _

    private var _seed: JSONObject = _
    private var _fruit: JSONObject = _

    private def loadSeed(): Unit = {
        var seedBox: JSONObject = null

        try {
            seedBox = JSON.parseObject(seed)

            val head = seedBox.getJSONObject("head")

            messageid = head.getString("messageid")

            if(messageid == null)
                messageid = "139287742832"

            transactionType = head.getString("transactiontype")

            if(transactionType == null || transactionType == "")
                throw new Exception("Can't find the TransactionType!")
            else if (!transactionType.forall(_.isDigit))
                throw new Exception("The transaction type is not supported.")


            _seed = seedBox.getJSONObject("body").getJSONObject("ielement")

            if(_seed == null)
                throw new Exception("Can't find the input element.")


        } catch {
            case je: JSONException =>
                log.warn("The seed string is :" + seedBox)
                log.error("Throws exception when parse seed:", je)
            case e: Exception =>
                log.error("The seed string is :" + seedBox, e)
        }
    }

    def farming(): String ={

        loadSeed()

        _fruit = new JSONObject()

        val head = new JSONObject()
        head.put("messageid", messageid)
        head.put("transactiontype", transactionType)

        val body = new JSONObject()
        val oelement = new JSONObject()

        try {

            var fruits: JSONObject = null

            val cla = Class.forName("com.wanbo.easyapi.server.workers.Seeder_" + transactionType)

            val seederObj = cla.newInstance().asInstanceOf[ISeeder]

            seederObj.driver match {
                case MysqlDriver() =>
                    seederObj.driver.setConfiguration(conf)
                case HBaseDriver() =>
                    seederObj.driver.setConfiguration(conf)
            }

            fruits = seederObj.onHandle(_seed)

            if (fruits == null)
                throw new Exception("The transaction type is not supported.")


            val _eCode = fruits.getString("errorcode").toInt
            if(_eCode == 0) {
                body.put("odatalist", fruits.getJSONArray("data"))

                if(fruits.containsKey("fromcache"))
                    oelement.put("fromcache", fruits.getString("fromcache"))

                throw new EasyException("0")
            } else {
                val msg = fruits.getString("errormsg")
                if(msg != null && msg != "")
                    throw new EasyException(_eCode.toString, msg)
                else
                    throw new EasyException(_eCode.toString)
            }

        } catch {
            case ee: EasyException =>
                oelement.put("errorcode", ee.getCode)
                oelement.put("errormsg", ee.getMessage)
            case e: Exception =>
                e.printStackTrace()
                oelement.put("errorcode", "99999")
                oelement.put("errormsg", e.getMessage)
        }

        body.put("oelement", oelement)

        _fruit.put("head", head)
        _fruit.put("body", body)

        _fruit.toJSONString
    }
}
