package com.wanbo.easyapi.server

import scala.collection.mutable

/**
 * Counter
 * Created by wanbo on 16/1/11.
 */
object Counter {
    private val queue = mutable.Queue[Int]()

    def add(): Unit ={
        queue.synchronized{
            queue += 1
        }
    }

    def getSize: Int ={
        queue.size
    }

    def log(zk: String, msg: String): Unit ={
        println("ID - [%s]: --- msg -- :%s".format(zk, msg))
    }
}
