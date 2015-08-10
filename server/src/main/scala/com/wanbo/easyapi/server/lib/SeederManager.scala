package com.wanbo.easyapi.server.lib

import com.alibaba.fastjson.{JSONException, JSONObject, JSON}
import com.wanbo.easyapi.server.database.{HBaseDriver, MysqlDriver}
import org.slf4j.{MDC, LoggerFactory}

/**
 * The manager of all types workers
 * Created by GWB on 2015/4/8.
 */
class SeederManager(conf: EasyConfig, seed: String) {

    private val log = LoggerFactory.getLogger(classOf[SeederManager])

    MDC.put("destination", "system")

    private var messageid: String = _
    private var transactionType: String = _

    private var _seed: Map[String, Any] = _
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


            _seed = EasyConverts.json2map(seedBox.getJSONObject("body").getJSONObject("ielement"))

            val uuId = head.getString("uuid")
            if(uuId != null && uuId != "")
                _seed = _seed + ("uuid" -> uuId)

            val cookieId = head.getString("cookieid")
            if(cookieId != null && cookieId != "")
                _seed = _seed + ("cookieid" -> cookieId)

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

    /**
     * The farmer, to produce fruits.
     * @return
     */
    def farming(): String ={

        loadSeed()

        _fruit = new JSONObject()

        val head = new JSONObject()
        head.put("messageid", messageid)
        head.put("transactiontype", transactionType)

        val body = new JSONObject()
        val oelement = new JSONObject()

        try {

            var fruits: EasyOutput = null

            val cla = Class.forName("com.wanbo.easyapi.server.workers.Seeder_" + transactionType)

            val seederObj = cla.newInstance().asInstanceOf[ISeeder]

            if (seederObj == null)
                throw new Exception("The transaction type is not supported.")

            seederObj._conf = conf

            seederObj.manager = this

            seederObj.driver match {
                case MysqlDriver() =>
                    seederObj.driver.setConfiguration(conf)
                case HBaseDriver() =>
                    seederObj.driver.setConfiguration(conf)
            }

            fruits = seederObj.onHandle(_seed)

            val _eCode = fruits.oelement.get("errorcode").get.toInt
            if(_eCode == 0) {
                body.put("odatalist", EasyConverts.list2json(fruits.odata))

                if(fruits.oelement.contains("fromcache"))
                    oelement.put("fromcache", fruits.oelement.getOrElse("fromcache",""))
                if(fruits.oelement.contains("ttl"))
                    oelement.put("ttl", fruits.oelement.getOrElse("ttl",""))
                if(fruits.oelement.contains("duration"))
                    oelement.put("duration", fruits.oelement.getOrElse("duration",""))

                if(fruits.oelement.contains("itemtotal"))
                    oelement.put("itemtotal", fruits.oelement.getOrElse("itemtotal",""))

                if(fruits.oelement.contains("pagetotal"))
                    oelement.put("pagetotal", fruits.oelement.getOrElse("pagetotal",""))

                throw new EasyException("0")
            } else {
                val msg = fruits.oelement.getOrElse("errormsg", "")
                if(msg != null && msg != "")
                    throw new EasyException(_eCode.toString, msg)
                else
                    throw new EasyException(_eCode.toString)
            }

        } catch {
            case ee: EasyException =>
                oelement.put("errorcode", ee.getCode)
                oelement.put("errormsg", ee.getMessage)
            case cnfe: ClassNotFoundException =>
                log.error("Seeder not found Exception:", cnfe)
                oelement.put("errorcode", "10011")
                oelement.put("errormsg", cnfe.getMessage)
            case e: Exception =>
                log.error("SeederManager Exception:", e)
                oelement.put("errorcode", "99999")
                oelement.put("errormsg", e.getMessage)
        }

        body.put("oelement", oelement)

        _fruit.put("head", head)
        _fruit.put("body", body)

        _fruit.toJSONString
    }

    /**
     * To call another seeder inside.
     * @param seeder The name of seeder.
     * @param seed   The input map.
     * @return
     */
    def transform(seeder: String, seed: Map[String, Any]): EasyOutput ={

        var fruits: EasyOutput = new EasyOutput

        try {
            val cla = Class.forName("com.wanbo.easyapi.server.workers.Seeder_" + seeder)

            val seederObj = cla.newInstance().asInstanceOf[ISeeder]

            seederObj.driver match {
                case MysqlDriver() =>
                    seederObj.driver.setConfiguration(conf)
                case HBaseDriver() =>
                    seederObj.driver.setConfiguration(conf)
            }

            fruits = seederObj.onHandle(seed)

        } catch {
            case ee: ClassNotFoundException =>
                log.error("Seeder not found Exception:", ee)
            case e: Exception =>
        }

        fruits
    }

    def updateCache(seeder: String, seed: Map[String, Any]): EasyOutput ={

        var fruits: EasyOutput = new EasyOutput

        try {
            val cla = Class.forName("com.wanbo.easyapi.server.workers.Seeder_" + seeder)

            val seederObj = cla.newInstance().asInstanceOf[ISeeder]

            seederObj.driver match {
                case MysqlDriver() =>
                    seederObj.driver.setConfiguration(conf)
                case HBaseDriver() =>
                    seederObj.driver.setConfiguration(conf)
            }

            seederObj.isUpdateCache = true

            fruits = seederObj.onHandle(seed)

        } catch {
            case ee: ClassNotFoundException =>
                log.error("Seeder not found Exception:", ee)
            case e: Exception =>
        }

        fruits
    }
}
