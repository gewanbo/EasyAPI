package com.wanbo.easyapi.server.workers

import com.wanbo.easyapi.server.cache.CacheManager
import com.wanbo.easyapi.server.database.MysqlDriver
import com.wanbo.easyapi.server.lib.{EasyException, EasyOutput, ISeeder, Seeder}
import org.slf4j.LoggerFactory


/**
 * Number 11006 seeder
 * Created by wanbo on 26/6/15.
 */
final class Seeder_11006 extends Seeder with ISeeder {

    name = "11006"

    driver = new MysqlDriver

    private val log = LoggerFactory.getLogger(classOf[Seeder_11006])

    private var _chanelId: Int = 1
    private var _pageIndex: Int = 1
    private var _pageSize: Int = 10

    private var _pageTotal: Int = 0
    private var _itemTotal: Int = 0

    override def onHandle(seed: Map[String, Any]): EasyOutput = {

        var dataList = List[Map[String, Any]]()

        try {

            val startRunTime = System.currentTimeMillis()

            val chaId: String = seed.getOrElse("id", "1").toString

            if (chaId != null && !chaId.forall(_.isDigit))
                throw new EasyException("20001")

            _chanelId = chaId.toInt

            if(_chanelId < 1)
                throw new EasyException("20001")

            if(_chanelId > 100)
                _chanelId = 100

            // Page
            val pageIndex: String = seed.getOrElse("pageindex", "1").toString
            val pageSize: String = seed.getOrElse("pagesize", "10").toString

            if (pageIndex != null && !pageIndex.forall(_.isDigit))
                throw new EasyException("20001")

            if (pageSize != null && !pageSize.forall(_.isDigit))
                throw new EasyException("20001")

            _pageIndex = pageIndex.toInt
            _pageSize = pageSize.toInt

            if(_pageIndex > 100)
                _pageIndex = 100
            if(_pageIndex < 1)
                _pageIndex = 1
            if(_pageSize > 100)
                _pageSize = 100
            if(_pageSize < 1)
                _pageSize = 1

            // Cache
            val cache_name = this.getClass.getSimpleName + _chanelId + _pageIndex + _pageSize
            val cacher = new CacheManager(expire = 600)

            val cacheData = cacher.cacheData(cache_name)

            if (cacheData != null && cacheData.oelement.get("errorcode").get == "0" && !isUpdateCache) {
                dataList = cacheData.odata
                fruits.oelement = fruits.oelement + ("fromcache" -> "true") + ("ttl" -> cacher.ttl.toString)
                fruits.oelement = fruits.oelement + ("itemtotal" -> cacheData.oelement.getOrElse("itemtotal", "0"))
                fruits.oelement = fruits.oelement + ("pagetotal" -> cacheData.oelement.getOrElse("pagetotal", "0"))
            } else {
                dataList = onDBHandle()

                if (dataList.size < 1)
                    throw new EasyException("20100")
                else {
                    val cache_data = new EasyOutput
                    cache_data.odata = dataList

                    fruits.oelement = fruits.oelement.updated("itemtotal", _itemTotal.toString)
                    cache_data.oelement = cache_data.oelement.updated("itemtotal", _itemTotal.toString)

                    fruits.oelement = fruits.oelement.updated("pagetotal", _pageTotal.toString)
                    cache_data.oelement = cache_data.oelement.updated("pagetotal", _pageTotal.toString)

                    cache_data.oelement = cache_data.oelement.updated("errorcode", "0")
                    cacher.cacheData(cache_name, cache_data)
                }
            }

            fruits.oelement = fruits.oelement.updated("errorcode", "0").+("duration" -> (System.currentTimeMillis() - startRunTime).toString)
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
            driver.setDB("cmstmp01")
            val conn = driver.getConnector

            val sql = "SELECT s.`id`, s.`cheadline`, s.`clongleadbody`, s.`cshortleadbody`, s.`tag`, " +
                "s.`column`, s.`pubdate` from story s ,(select id from channel_detail c where c.`chaid` in (%d) and c.`type`=1 order by c.addtime desc limit %d,%d) b ".format(_chanelId, (_pageIndex - 1) * _pageSize + 1, _pageSize) +
                "where s.id= b.id and s.publish_status='publish' order by s.pubdate desc;"

            val ps = conn.prepareStatement(sql)
            val rs = ps.executeQuery()

            while (rs.next()){
                var tmpMap = Map[String, String]()
                tmpMap = tmpMap + ("id" -> rs.getString(1))
                tmpMap = tmpMap + ("cheadline" -> rs.getString(2))
                tmpMap = tmpMap + ("clongleadbody" -> rs.getString(3))
                tmpMap = tmpMap + ("cshortleadbody" -> rs.getString(4))
                tmpMap = tmpMap + ("tag" -> rs.getString(5))
                tmpMap = tmpMap + ("column" -> rs.getString(6))
                tmpMap = tmpMap + ("pubdate" -> rs.getString(7))

                dataList = dataList :+ tmpMap
            }

            rs.close()
            ps.close()

            val tps = conn.prepareStatement("SELECT count(id) totalnum FROM channel_detail WHERE `type`=1 and chaid in (%d);".format(_chanelId))
            val trs = tps.executeQuery()

            if(trs.last()) {
                _itemTotal = trs.getInt(1)
                if(_itemTotal > 0)
                    _pageTotal = math.ceil(_itemTotal / _pageSize).toInt
            }

            trs.close()
            tps.close()

        } catch {
            case e: Exception =>
                throw e
        }

        dataList
    }
}