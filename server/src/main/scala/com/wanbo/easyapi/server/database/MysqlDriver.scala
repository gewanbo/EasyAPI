package com.wanbo.easyapi.server.database

import java.sql.Connection

import com.wanbo.easyapi.server.database.mysql._
import com.wanbo.easyapi.server.lib.EasyConfig
import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 * Mysql driver
 * Created by wanbo on 15/4/16.
 */
case class MysqlDriver() extends Driver {

    private var db_host: String = _
    private var db_port: String = _
    private var db_username: String = _
    private var db_password: String = _

    private var _dataSource: DataSource = null
    private var _conn:Connection = null

    override def setConfiguration(conf: EasyConfig): Unit = {
        db_host = conf.driver_mysql.get("mysql.db.host").get
        db_port = conf.driver_mysql.get("mysql.db.port").get
        db_username = conf.driver_mysql.get("mysql.db.username").get
        db_password = conf.driver_mysql.get("mysql.db.password").get
    }

    def setDB(dbName: String) {
        val ctx = new AnnotationConfigApplicationContext(classOf[BeanConfig])

        val ds: DataSource = ctx.getBean(classOf[DataSource])
        ds.setUrl("jdbc:mysql://%s:%s/%s?characterEncoding=utf-8".format(db_host, db_port, dbName))
        ds.setUsername(db_username)
        ds.setPassword(db_password)

        _dataSource = ds

    }

    def getConnector: Connection ={
        connect()
        _conn
    }

    /**
     * Get database connection
     */
    protected def connect() {
        try{

            if(_dataSource == null)
                throw new Exception("DataSource is empty!")

            _conn = _dataSource.getConnection

        } catch {
            case e: Exception =>
                e.printStackTrace()
        }
    }

    protected def close(): Unit ={
        try{
            if(_conn != null)
                _conn.close()
        } catch {
            case e: Exception =>
        }
    }
}
