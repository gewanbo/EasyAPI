package com.wanbo.easyapi.server.workers

import java.util.{Calendar, TimeZone}

import com.wanbo.easyapi.server.cache.CacheManager
import com.wanbo.easyapi.server.database.MysqlDriver
import com.wanbo.easyapi.server.lib.{EasyException, EasyOutput, ISeeder, Seeder}
import com.wanbo.easyapi.shared.common.utils.Utils
import org.slf4j.LoggerFactory


/**
 * Get the hot news by days.
 * Created by wanbo on 2015/10/14.
 */
final class Seeder_60006 extends Seeder with ISeeder {

    name = "60006"

    driver = new MysqlDriver

    private var _days: Int = 0
    private var _topNum: Int = 10

    private val log = LoggerFactory.getLogger(classOf[Seeder_60006])

    override def onHandle(seed: Map[String, Any]): EasyOutput = {

        var dataList = List[Map[String, Any]]()

        try {
            val days: String = seed.getOrElse("days", "1").toString

            if (days != null && !days.forall(_.isDigit))
                throw new EasyException("20001")

            _days = days.toInt

            if(_days > 100)
                _days = 100

            val num = seed.getOrElse("num", "10").toString
            if (num == null || !num.forall(_.isDigit)){
                _topNum = 10
            } else {
                _topNum = num.toInt
            }

            if(_topNum > 30)
                _topNum = 30

            // Cache
            val cache_name = this.getClass.getSimpleName + _days + _topNum

            val cacher = new CacheManager(conf = _conf, expire = 10800)

            val cacheData = cacher.cacheData(cache_name)

            if (cacheData != null && cacheData.oelement.get("errorcode").get == "0" && !isUpdateCache) {
                dataList = cacheData.odata
                fruits.oelement = fruits.oelement + ("fromcache" -> "true") + ("ttl" -> cacher.ttl.toString)
            } else {

                val data = onDBHandle()

                if (data.size < 1)
                    throw new EasyException("20100")
                else {

                    val idList = data.map(x => x.get("storyid").get).mkString(",")

                    val storyData = manager.transform("10002", Map(("storyid", idList)))

                    if(storyData.oelement.get("errorcode").get == "0"){
                        val data1 = data.map(x => (x.get("storyid").get,x.get("totalpv").get))
                        val data2 = storyData.odata.map(x => (x.get("id").get,x.get("cheadline").get))

                        val mergeData = data1 ++ data2
                        dataList = mergeData.groupBy(_._1).map(x => x._1 -> x._2.map(_._2)).map(y => Map("storyid" -> y._1, "totalpv" -> y._2(0), "title" -> y._2(1))).toList

                        val cache_data = new EasyOutput
                        cache_data.odata = dataList
                        cache_data.oelement = cache_data.oelement.updated("errorcode", "0")
                        cacher.cacheData(cache_name, cache_data)
                    } else {
                        throw new EasyException(storyData.oelement.get("errorcode").get)
                    }

                }
            }
            cacher.close()

            fruits.oelement = fruits.oelement.updated("errorcode", "0")
            fruits.odata = dataList
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
    override protected def onDBHandle(): List[Map[String, String]] = {
        var dataList = List[Map[String, String]]()

        try {
            val driver = this.driver.asInstanceOf[MysqlDriver]
            val conn = driver.getConnector("analytic")

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"))
            val tables = Utils.formatTablesNameByDate(days = _days, tabPrefix = "access_info_")

            if(tables.size > 0) {

                var sql = "SELECT x.ai_storyid storyid, sum(x.num) numx FROM ("

                var where = " ai_accesstime > %d and ".format(calendar.getTimeInMillis / 1000 - _days * 86400)
                var and = ""

                tables.foreach(tab => {
                    sql += and + "select ai_storyid,count(ai_storyid) num from analytic.`%s` where %s ai_storyid != '' group by ai_storyid ".format(tab, where)
                    where = ""
                    and = " union all "
                })

                sql += ") x group by x.ai_storyid order by numx desc limit %d;".format(_topNum)

                val ps = conn.prepareStatement(sql)
                val rs = ps.executeQuery()

                while (rs.next()) {
                    var tmpMap = Map[String, String]()
                    tmpMap = tmpMap + ("storyid" -> rs.getString(1))
                    tmpMap = tmpMap + ("totalpv" -> rs.getString(2))
                    dataList = dataList :+ tmpMap
                }

                rs.close()
                ps.close()
            }

            conn.close()

        } catch {
            case e: Exception =>
                throw e
        }

        dataList
    }

}