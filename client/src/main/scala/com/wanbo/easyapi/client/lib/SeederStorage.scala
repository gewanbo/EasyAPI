package com.wanbo.easyapi.client.lib

/**
 * Seeder storage.
 * Created by root on 15-12-11.
 */
object SeederStorage {

    private var dataStore = Map[String, SeedData]()

    def pull(key:String): SeedData = dataStore.getOrElse(key, SeedData("", "", 0))

    def push(data: SeedData): Unit ={
        if(data.key.nonEmpty)
            dataStore = dataStore.updated(data.key, data)
    }

    case class SeedData(key: String, data: String, time: Long = System.currentTimeMillis())
}