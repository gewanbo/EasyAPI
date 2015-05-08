package com.wanbo.easyapi.server.workers

import com.wanbo.easyapi.server.cache.CacheManager
import com.wanbo.easyapi.server.database.HBaseDriver
import com.wanbo.easyapi.server.lib.{EasyOutput, EasyException, ISeeder, Seeder}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Scan, HTable}
import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.slf4j.LoggerFactory

/**
 * Number 61001 seeder
 * Created by wanbo on 15/4/17.
 */
final class Seeder_61001 extends Seeder with ISeeder {

    name = "61001"

    driver = new HBaseDriver

    private var _uuId = ""
    private var _cookieId = ""

    private val log = LoggerFactory.getLogger(classOf[Seeder_61001])

    override def onHandle(seed: Map[String, Any]): EasyOutput = {

        var dataList = List[Map[String, Any]]()

        try {

            val uuId = seed.getOrElse("uuid", "").toString
            val cookieId = seed.getOrElse("cookieid", "").toString

            if(uuId == "" && cookieId == "")
                throw new EasyException("20001")

            _uuId = uuId
            _cookieId = cookieId

            // Cache
            val cache_name = this.getClass.getSimpleName + _uuId + _cookieId

            val cacher = new CacheManager(expire = 600)
            val cacheData = cacher.cacheData(cache_name)

            if (cacheData != null && cacheData.oelement.get("errorcode").get == "0" && !isUpdateCache) {
                dataList = cacheData.odata
                fruits.oelement = fruits.oelement + ("fromcache" -> "true") + ("ttl" -> cacher.ttl.toString)
            } else {

                val data = onDBHandle()

                if (data.size < 1)
                    throw new EasyException("20100")
                else {
                    val cache_data = new EasyOutput
                    cache_data.odata = List[Map[String, Any]]()

                    val sortData = data.sortBy(x => x._3)(Ordering.Double.reverse)
                    sortData.slice(0, 30).foreach(x => {
                        var obj = Map[String, Any]()
                        obj = obj + ("storyid" -> x._1.reverse.padTo(9, 0).reverse.mkString)
                        obj = obj + ("cheadline" -> x._2)
                        dataList = dataList :+ obj

                        cache_data.odata = cache_data.odata :+ obj
                    })
                    cache_data.oelement = cache_data.oelement.updated("errorcode", "0")
                    cacher.cacheData(cache_name, cache_data)
                }
            }

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

    override protected def onDBHandle(): List[(String, String, Double)] = {
        var dataList = List[(String, String, Double)]()

        try {

            val conf = HBaseConfiguration.create
            conf.set("hbase.zookeeper.quorum", "BJtest223")

            val table = new HTable(conf, Bytes.toBytes("user_recommend_stories"))

            val scan = new Scan()
            scan.addFamily(Bytes.toBytes("c"))
            scan.addColumn(Bytes.toBytes("c"), Bytes.toBytes("storyid"))
            scan.addColumn(Bytes.toBytes("c"), Bytes.toBytes("cheadline"))
            scan.addColumn(Bytes.toBytes("c"), Bytes.toBytes("rating"))

            log.info("Query ---------- uuid:" + _uuId)
            log.info("Query ---------- cookieid:" + _cookieId)


            if (_uuId != null && _uuId != "") {
                scan.setStartRow(Bytes.toBytes(_uuId))
                scan.setFilter(new PrefixFilter(Bytes.toBytes(_uuId)))
                scan.setStopRow(Bytes.toBytes(_uuId + 'Z'))
            } else if(_cookieId != null && _cookieId != "") {
                scan.setStartRow(Bytes.toBytes(_cookieId))
                scan.setFilter(new PrefixFilter(Bytes.toBytes(_cookieId)))
                scan.setStopRow(Bytes.toBytes(_cookieId + 'Z'))
            }

            log.info("Start row ------:" + new String(scan.getStartRow))
            log.info("Stop row ------:" + new String(scan.getStopRow))

            val retScanner = table.getScanner(scan)

            var result = retScanner.next()
            while (result != null) {

                val rowKey = new String(result.getRow)
                val fields = rowKey.split("#")
                val id = fields(0)

                log.info("Row key ------:" + rowKey)

                if((_uuId != "" && _uuId == id) || (_cookieId != "" && _cookieId == id)) {

                    val s = result.getValue(Bytes.toBytes("c"), Bytes.toBytes("storyid"))
                    val c = result.getValue(Bytes.toBytes("c"), Bytes.toBytes("cheadline"))
                    val r = result.getValue(Bytes.toBytes("c"), Bytes.toBytes("rating"))

                    if (s != null && c != null && r != null) {
                        var rating: Double = 0
                        try {
                            rating = new String(r).toDouble
                        } catch {
                            case e: Exception =>
                            // Ignore
                        }
                        dataList = dataList :+(new String(s), new String(c), rating)
                    }
                }

                result = retScanner.next()
            }

            table.close()
        } catch {
            case e: Exception =>
                throw e
        }

        dataList
    }
}
