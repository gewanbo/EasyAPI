package com.wanbo.easyapi.server.database

import java.sql.Connection

import com.wanbo.easyapi.server.database.mysql._
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 * Mysql driver
 * Created by wanbo on 15/4/16.
 */
case class MysqlDriver() extends Driver {

//    private var db_host: String = _
//    private var db_port: String = _
//    private var db_username: String = _
//    private var db_password: String = _

//    private var _dataSource: DataSource = null
//    private var _conn:Connection = null

    override def setConfiguration(conf: EasyConfig): Unit = {

    }

    def getConnector(dbName: String = "test", writable: Boolean = false): Connection ={
        var conn: Connection = null

        try {

            val sourceList = MysqlDriver.dataSourceList.filter(x => x._1._1 == dbName && x._1._2 == writable).toList

            if(sourceList.size > 0) {
                conn = util.Random.shuffle(sourceList).apply(0)._2.getConnection
            } else {
                throw new Exception("Didn't find the available database source.")
            }

        } catch {
            case e: Exception =>
                throw e
        }

        conn
    }

    protected def close(): Unit ={
        try{
//            if(_conn != null)
//                _conn.close()
        } catch {
            case e: Exception =>
        }
    }
}

object MysqlDriver {

    private var dataSourceList: List[((String, Boolean), DataSource)] = List[((String, Boolean), DataSource)]()

    /**
     * Initialize all available data source.
     *
     * Called by manager when it start up.
     */
    def initializeDataSource(settings: List[Map[String, String]]): Unit ={

        if (settings.size > 0) {
            settings.foreach(x => {
                val db_host = x.getOrElse("host", "")
                val db_port = x.getOrElse("port", "")
                val db_username = x.getOrElse("uname", "")
                val db_password = x.getOrElse("upswd", "")
                val db_name = x.getOrElse("dbname", "")
                val db_writable = if (x.get("writable").get.toLowerCase == "true") true else false

                val ctx = new AnnotationConfigApplicationContext(classOf[BeanConfig])

                val ds: DataSource = ctx.getBean(classOf[DataSource])
                ds.setUrl("jdbc:mysql://%s:%s/%s?characterEncoding=utf-8".format(db_host, db_port, db_name))
                ds.setUsername(db_username)
                ds.setPassword(db_password)

                MysqlDriver.dataSourceList = MysqlDriver.dataSourceList :+ ((db_name, db_writable), ds)
            })
        }

    }

}