package com.wanbo.easyapi.server.database

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.{Document, MongoClient, MongoClientSettings, MongoCollection, MongoDatabase, ServerAddress}

import scala.collection.JavaConverters._

/**
 * MongoDB driver.
 * Created by wanbo on 16/7/5.
 */
case class MongoDriver() extends DbDriver with IDriver with Logging {

    private var _coll: MongoCollection[Document] = null

    override def setConfiguration(conf: EasyConfig): Unit = {
        this._conf = conf
    }

    def getCollection(dbName: String, collection: String): MongoCollection[Document] ={

        val writable = true

        try {

            var sourceList = MongoDriver.dataSourceList.filter(x => x._1._1 == dbName && x._1._2)

            // If didn't find readable data source, can read from writable data source.
            if(sourceList.isEmpty && !writable){
                sourceList = MongoDriver.dataSourceList.filter(x => x._1._1 == dbName)
            }

            if(sourceList.nonEmpty) {
                _coll = util.Random.shuffle(sourceList).head._2.getCollection(collection)
            } else {
                throw new Exception("Didn't find the available database source.")
            }
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }

        _coll
    }

    def close(): Unit ={
        try{
            if(MongoDriver._client != null)
                MongoDriver._client.close()
        } catch {
            case e: Exception =>
        }
    }
}

object MongoDriver extends Logging {
    private var _client: MongoClient = null

    private var dataSourceList: List[((String, Boolean), MongoDatabase)] = List[((String, Boolean), MongoDatabase)]()

    def initializeDataSource(settings: List[Map[String, String]]): Unit ={

        if (settings.nonEmpty) {


            try {
                val db_host = settings.head.getOrElse("host", "")
                val db_port = settings.head.getOrElse("port", "0").toString
                val db_name = settings.head.getOrElse("dbname", "")

                if(db_host.isEmpty || db_name.isEmpty)
                    throw new Exception("The MongoDB driver initialization cannot finish, Please check configuration.")

                val serverAddress = ServerAddress(db_host, db_port.toInt)
                val clusterSetting = ClusterSettings.builder().hosts(List(serverAddress).asJava).build()
                val clientSettings = MongoClientSettings.builder().clusterSettings(clusterSetting).build()

                _client = MongoClient(clientSettings)
                val db = _client.getDatabase(db_name)

                dataSourceList = dataSourceList :+((db_name, true), db)
            } catch {
                case e: Exception =>
                    log.error("Throws exception when initialize MongoDb driver.", e)
            }
        }

    }
}
