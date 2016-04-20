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
                    case s: String =>
                        s
                    case i: Int =>
                        i
                    case y: List[_] =>
                        list2json(y.asInstanceOf[List[Map[String, Any]]])
                    case z: Map[_, _] =>
                        map2json(z.asInstanceOf[Map[String, Any]])
                    case null =>
                        ""
                    case m =>
                        m.toString
                }

                obj.put(x._1, data)
            })

            json.add(obj)
        })

        json
    }

    def map2json(map: Map[String, Any]): JSONObject ={
        val json = new JSONObject()

        map.foreach(x => {
            json.put(x._1, x._2)
        })

        json
    }
}