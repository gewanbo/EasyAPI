package com.wanbo.easyapi.server.workers

import com.wanbo.easyapi.server.cache.CacheManager
import com.wanbo.easyapi.server.database.MysqlDriver
import com.wanbo.easyapi.server.lib.{EasyException, EasyOutput, ISeeder, Seeder}
import org.slf4j.LoggerFactory

/**
 * Number 10008 seeder
 * Created by wanbo on 15/4/15.
 */
final class Seeder_10008() extends Seeder with ISeeder {

    name = "10008"
    driver = new MysqlDriver

    private var _uuId = ""
    private var _cookieId = ""

    private var _storyId = ""

    private val log = LoggerFactory.getLogger(classOf[Seeder_10008])

    override def onHandle(seed: Map[String, Any]): EasyOutput = {
        var dataList = List[Map[String, Any]]()

        try {

            val uuId = seed.getOrElse("uuid", "")
            val cookieId = seed.getOrElse("cookieid", "")
            val storyId = seed.getOrElse("storyid", "")

            if(uuId != null)
                _uuId = uuId.toString

            if(cookieId != null)
                _cookieId = cookieId.toString

            if(storyId != null)
                _storyId = storyId.toString

            if(_uuId == "" && _cookieId == "" && _storyId == "")
                throw new EasyException("20001")

            // Cache
            val cache_name = this.getClass.getSimpleName + _uuId + _cookieId + _storyId

            val cacher = new CacheManager()
            val cacheData = cacher.cacheData(cache_name)

            if (cacheData != null && cacheData.oelement.get("errorcode").get == "0" && !isUpdateCache) {
                cacheData.odata.foreach(x => {
                    var obj = Map[String, Any]()
                    x.foreach(y => {
                        obj = obj + (y._1 -> y._2)
                    })
                    dataList = dataList :+ obj
                })
                fruits.oelement = fruits.oelement + ("fromcache" -> "true")
            } else {

                // From 61001
                var data_61001: EasyOutput = new EasyOutput

                if(_uuId != "")
                    data_61001 = this.manager.transform("61001", Map("uuid" -> _uuId))
                else if (_cookieId != "")
                    data_61001 = this.manager.transform("61001", Map("cookieid" -> _cookieId))

                if(data_61001.oelement.get("errorcode").get == "0" && data_61001.odata.size > 0){
                    dataList = dataList ++ data_61001.odata
                }

                // From 10008 self
                if(dataList.size < 10) {
                    val data = onDBHandle()

                    if (data.size > 0) {
                        data.foreach(x => {
                            var obj = Map[String, Any]()
                            obj = obj + ("storyid" -> x._1)
                            obj = obj + ("cheadline" -> x._2)
                            dataList = dataList :+ obj
                        })
                    }
                }

                if(dataList.size < 1)
                    throw new EasyException("20100")

                val cache_data = new EasyOutput
                cache_data.odata = dataList

                cache_data.oelement = cache_data.oelement.updated("errorcode", "0")
                cacher.cacheData(cache_name, cache_data)
            }

            fruits.oelement = fruits.oelement.updated("errorcode", "0")
            fruits.odata = util.Random.shuffle(dataList).slice(0, 10)
        } catch {
            case ee: EasyException =>
                fruits.oelement = fruits.oelement.updated("errorcode", ee.getCode)
            case e: Exception =>
                log.error("Seeder has exception:", e)
                fruits.oelement = fruits.oelement.updated("errorcode", "-1")
                fruits.oelement = fruits.oelement.updated("errormsg", e.getMessage)
        }

        fruits
    }

    override protected def onDBHandle(): List[(String, String)] = {
        var dataList = List[(String, String)]()

        try {
            val driver = this.driver.asInstanceOf[MysqlDriver]
            driver.setDB("cmstmp01")
            val conn = driver.getConnector

            var storyList = List[String]()

            if (_storyId == "")
                throw new EasyException("20100")

            // Get related ids
            val sqlRelated = "select `relate_id`,`st_id` from `story2story` where `st_id` = '%s' or `relate_id` = '%s';".format(_storyId, _storyId)

            var ps = conn.prepareStatement(sqlRelated)
            var rs = ps.executeQuery()

            while (rs.next()){
                storyList = storyList :+ rs.getString(1) :+ rs.getString(2)
            }

            // Get related story
            if(storyList.size > 0) {

                val sqlStory = "select id,cheadline,eheadline,last_publish_time from `story` where `id` in (%s) and `publish_status` = 'publish' order by `id` desc limit 10;".format(storyList.mkString("'", "','", "'"))

                ps = conn.prepareStatement(sqlStory)
                rs = ps.executeQuery()

                while (rs.next()){
                    val tmpStoryId = rs.getString(1)
                    if(_storyId != tmpStoryId) {
                        dataList = dataList :+ (tmpStoryId, rs.getString(2))
                    }
                }

            } else {
                rs.close()
                ps.close()
                throw new EasyException("20100")
            }
            rs.close()
            ps.close()

        } catch {
            case e: Exception =>
                throw e
        }

        dataList
    }
}
