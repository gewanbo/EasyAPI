package com.wanbo.easyapi.server.database

import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCollection, Document, ServerAddress}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
 * MongoDB driver.
 * Created by wanbo on 16/7/5.
 */
case class MongoDriver() extends DbDriver with IDriver {
    private var _client: MongoClient = null
    private var _coll: MongoCollection[Document] = null

    private var _settings: MongoClientSettings = null

    private val log = LoggerFactory.getLogger(classOf[MongoDriver])

    override def setConfiguration(conf: EasyConfig): Unit = {
        this._conf = conf

        val settings = conf.driverSettings.filter(x => x._2.get("type").get == "mongo").toList.map(_._2)

        if(settings.nonEmpty) {
            val serverAddress = ServerAddress(settings.head.getOrElse("host", ""), settings.head.getOrElse("port", "0").toInt)
            val clusterSetting = ClusterSettings.builder().hosts(List(serverAddress).asJava).build()
            _settings = MongoClientSettings.builder().clusterSettings(clusterSetting).build()
        }

    }

    def getCollection(dbName: String, collection: String): MongoCollection[Document] ={

        try {
            if(_client == null) {
                if (_settings == null)
                    throw new Exception("The configuration for MongoDB driver was not found!")
                else
                    _client = MongoClient(_settings)
            }

            val db = _client.getDatabase(dbName)
            _coll = db.getCollection(collection)

        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }

        _coll
    }

    def close(): Unit ={
        try{
            if(_client != null)
                _client.close()
        } catch {
            case e: Exception =>
        }
    }
}
