package com.wanbo.easyapi.client.actors

import akka.actor.Actor
import akka.io.Tcp.{Close, PeerClosed, Received, Write}
import akka.util.ByteString
import com.alibaba.fastjson.{JSON, JSONException, JSONObject}
import com.wanbo.easyapi.client.lib.SeedStorage.SeedData
import com.wanbo.easyapi.client.lib._
import com.wanbo.easyapi.shared.common.utils.Utils
import org.slf4j.LoggerFactory

/**
 * Farm
 * Created by wanbo on 2015/8/17.
 */
class Farm extends Actor {

    private val log = LoggerFactory.getLogger(classOf[Farm])

    override def receive: Receive = {
        case Received(data) =>

            log.info("Received a request ...")

            val msgData = data.decodeString("UTF-8")

            try {

                var responseBody = ""

                val msgBody = HttpUtility.parseBody(msgData)

                if (msgBody.isEmpty) {
                    responseBody = onAvailableServer()
                } else {

                    if (msgBody.startsWith("{miss")) {
                        responseBody = onMiss(msgBody)
                    } else {
                        responseBody = onRedirect(msgBody)
                    }

                }

                sender() ! Write(ByteString.fromString(responseBody, "UTF-8"))
                sender() ! Close

            } catch {
                case e: Exception =>
                    log.error("Error:", e)
                    sender() ! Write(ByteString.fromString(HttpUtility.jsonHeader, "UTF-8"))
                    sender() ! Close
            }

        case PeerClosed =>
            context stop self
    }

    private def onAvailableServer(): String ={

        log.info("Calling onAvailableServer ...")

        // return the best one
        val serverText = AvailableServer.availableServer

        if (serverText == "") {
            // Alarm
            log.error("Didn't find available server!")
        } else {
            log.info("The best server is:" + serverText)
        }

        HttpUtility.textHeader + serverText
    }

    private def onMiss(msgBody: String): String = {

        log.info("Calling onMiss ...")

        var server = ""
        val fields = msgBody.substring(1, msgBody.length - 1).split("#")

        if (fields.size > 1) {
            server = fields(1)

            if (server != "" && AvailableServer.serverList.contains(server)) {
                // Get current server list
                WorkCounter.push(server)
            }
        }

        HttpUtility.jsonHeader
    }

    private def onRedirect(msgBody: String): String ={

        log.info("Calling onRedirect ...")

        var responseMsg: String = "{\"body\":{\"oelement\":{\"errormsg\":\"\",\"errorcode\":\"30012\"}}}"
        var seedBox: JSONObject = null

        try {
            // Parse message

            seedBox = JSON.parseObject(msgBody)

            val head = seedBox.getJSONObject("head")

            // Get transaction type and parameters
            val transactionType = head.getString("transactiontype")

            log.info("TransactionType-------:" + transactionType)

            if(transactionType == null || transactionType == "")
                throw new Exception("Can't find the TransactionType!")
            else if (!transactionType.forall(_.isDigit))
                throw new Exception("The transaction type is not supported.")


            var _seed: Map[String, Any] = EasyConverts.json2map(seedBox.getJSONObject("body").getJSONObject("ielement"))

            val uuId = head.getString("uuid")
            if(uuId != null && uuId != "")
                _seed = _seed + ("uuid" -> uuId)

            val cookieId = head.getString("cookieid")
            if(cookieId != null && cookieId != "")
                _seed = _seed + ("cookieid" -> cookieId)

            //log.info("uuid------------:" + uuId)
            //log.info("cookieid----------:" + cookieId)
            //log.info("seed------:" + _seed.size)

            if(_seed == null)
                throw new Exception("Can't find the input element.")

            // Generate unique key
            val seedKey = Utils.MD5(transactionType + _seed.mkString)

            val seed = SeedStorage.pull(seedKey)

            if(seed.key.nonEmpty){
                if(System.currentTimeMillis() - seed.time < 10000) {
                    responseMsg = seed.data
                } else {
                    val response = HttpUtility.post(msgBody)

                    if(response.nonEmpty){

                        val resObj: JSONObject = JSON.parseObject(response)

                        val oelement = EasyConverts.json2map(resObj.getJSONObject("body").getJSONObject("oelement"))

                        val errorCode = oelement.getOrElse("errorcode", "-1")

                        if(errorCode == "0"){
                            // Store to local storage
                            // TODO: Need to filter write seeder, the size of odatalist is nonempty.
                            SeedStorage.push(SeedData(seedKey, response))
                        }

                        responseMsg = response
                    } else {
                        responseMsg = seed.data
                    }
                }
            } else {

                val response = HttpUtility.post(msgBody)

                if(response.nonEmpty){

                    val resObj: JSONObject = JSON.parseObject(response)

                    val oelement = EasyConverts.json2map(resObj.getJSONObject("body").getJSONObject("oelement"))

                    val errorCode = oelement.getOrElse("errorcode", "-1")

                    if(errorCode == "0"){
                        // Store to local storage
                        // TODO: Need to filter write seeder, the size of odatalist is nonempty.
                        SeedStorage.push(SeedData(seedKey, response))
                    }

                    responseMsg = response
                }
                //  } else { Response default error message. }
            }

        } catch {
            case je: JSONException =>
                log.warn("The seed string is :" + seedBox)
                log.error("Throws exception when parse seed:", je)
            case e: Exception =>
                log.error("The seed string is :" + seedBox, e)
        }

        HttpUtility.jsonHeader + responseMsg
    }
}
