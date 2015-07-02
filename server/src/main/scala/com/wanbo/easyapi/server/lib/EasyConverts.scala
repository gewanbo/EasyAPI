package com.wanbo.easyapi.server.lib

import com.alibaba.fastjson.{JSONArray, JSONObject}
import scala.collection.immutable._

/**
 * Converts
 * Created by wanbo on 15/4/29.
 */
object EasyConverts {

    /**
     * Convert json object to map
     * @param jsonObj The JSON object.
     * @return
     */
    def json2map(jsonObj: JSONObject): Map[String, Any] ={
        var map = Map[String, Any]()

        jsonObj.keySet().toArray.foreach(x => {
            map = map + (x.toString -> jsonObj.get(x))
        })

        map
    }

    /**
     * Convert list to json array
     *
     * Only support data types: List[Map[String, Any]].
     * @todo Pattern match should be upgrade.
     */
    def list2json(list: List[Map[String, Any]]): JSONArray = {
        val json = new JSONArray()

        list.foreach(item => {

            val obj = new JSONObject()

            item.map(x => {
                val data = x._2 match {
                    case y: String =>
                        y
                    case y: List[_] =>
                        list2json(y.asInstanceOf[List[Map[String, Any]]])
                    case null =>
                        ""
                }

                obj.put(x._1, data)
            })

            json.add(obj)
        })

        json
    }
}