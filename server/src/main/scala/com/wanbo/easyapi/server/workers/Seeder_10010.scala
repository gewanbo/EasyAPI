package com.wanbo.easyapi.server.workers

import com.wanbo.easyapi.server.cache.CacheManager
import com.wanbo.easyapi.server.database.MysqlDriver
import com.wanbo.easyapi.server.lib.{EasyException, EasyOutput, ISeeder, Seeder}
import org.slf4j.LoggerFactory


/**
 * Get the column author by unique id.
 * Created by wanbo on 2015/10/10.
 */
final class Seeder_10010 extends Seeder with ISeeder {

    name = "10010"

    driver = new MysqlDriver

    private var _id = ""

    private val log = LoggerFactory.getLogger(classOf[Seeder_10010])

    override def onHandle(seed: Map[String, Any]): EasyOutput = {

        var dataList = List[Map[String, Any]]()

        try {
            val id = seed.getOrElse("id", "").toString

            if (id != null && !id.forall(_.isDigit))
                throw new EasyException("20001")

            if(id != null)
                _id = id

            // Cache
            val cache_name = this.getClass.getSimpleName + _id

            val cacher = new CacheManager(conf = _conf, expire = 600)

            val cacheData = cacher.cacheData(cache_name)

            if (cacheData != null && cacheData.oelement.get("errorcode").get == "0" && !isUpdateCache) {
                dataList = cacheData.odata
                fruits.oelement = fruits.oelement + ("fromcache" -> "true") + ("ttl" -> cacher.ttl.toString)
            } else {

                dataList = onDBHandle()

                if (dataList.size < 1)
                    throw new EasyException("20100")
                else {
                    val cache_data = new EasyOutput
                    cache_data.odata = dataList

                    cache_data.oelement = cache_data.oelement.updated("errorcode", "0")
                    cacher.cacheData(cache_name, cache_data)
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
            val conn = driver.getConnector("cmstmp01")

            val sql = "SELECT c.id, c.headline, c.author_name_cn, c.author_name_en, c.description, p.`type` pic_type, p.piclink pic_link FROM `column` c left join `picture` p on convert(left(c.pic,9) using utf8)=p.id where c.`id` = '%s'".format(_id)

            val ps = conn.prepareStatement(sql)
            val rs = ps.executeQuery()

            val metaData = ps.getMetaData
            val columnCount = metaData.getColumnCount
            while (rs.next()) {
                var tmpMap = Map[String, String]()

                for(i <- Range(1, columnCount + 1)) {
                    tmpMap = tmpMap + (metaData.getColumnLabel(i) -> rs.getString(i))
                }

                dataList = dataList :+ tmpMap
            }

            rs.close()
            ps.close()
            conn.close()

        } catch {
            case e: Exception =>
                throw e
        }

        dataList
    }

}