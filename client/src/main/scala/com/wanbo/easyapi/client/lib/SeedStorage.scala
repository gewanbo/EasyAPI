package com.wanbo.easyapi.client.lib

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig

/**
 * Seeder storage.
 * Created by root on 15-12-11.
 */
class SeedStorage(conf: EasyConfig) extends Runnable with Logging {

    def start(): Unit ={
        log.info("Starting up the seeder storage cleaner ...")

        val seederStorage = new Thread(new SeedStorage(conf))

        seederStorage.start()

        log.info("The seeder storage cleaner was running ...")
    }

    override def run(): Unit = {
        var timeMark = System.currentTimeMillis()

        while (true){

            try {

                if (SeedStorage.dataStore.size > 300 || System.currentTimeMillis() - timeMark > 30000) {

                    val timeoutData = SeedStorage.dataStore.filter(System.currentTimeMillis() - _._2.time > 30000)

                    if(timeoutData.size > 0){
                        SeedStorage.synchronized{
                            SeedStorage.dataStore = SeedStorage.dataStore.--(timeoutData.map(_._1))
                        }
                    }

                    log.info("Seed storage current size: " + SeedStorage.dataStore.size)
                    
                    timeMark = System.currentTimeMillis()

                } else {
                    // Waiting for 3 seconds.
                    Thread.sleep(3000)
                }

            } catch {
                case e: Exception =>
                    log.error("Storage cleaning throws exception:", e)
            }

        }
    }
}

object SeedStorage {

    private var dataStore = Map[String, SeedData]()

    def pull(key:String): SeedData = dataStore.getOrElse(key, SeedData("", "", 0))

    def push(data: SeedData): Unit ={
        if(data.key.nonEmpty)
            dataStore = dataStore.updated(data.key, data)
    }

    case class SeedData(key: String, data: String, time: Long = System.currentTimeMillis())
}