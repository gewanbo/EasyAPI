package com.wanbo.easyapi.server.database

import com.wanbo.easyapi.shared.common.Logging

import scala.collection.mutable

/**
  * Created by wanbo on 16/6/12.
  */
object HealthChecker extends Logging {

    private val hbaseStatus = mutable.Queue[Int]()

    val GREEN = 0
    val YELLOW = 1
    val RED = 2

    def push(status: Int): Unit ={
        hbaseStatus.synchronized{
            hbaseStatus.enqueue(status)
            if(hbaseStatus.size > 10)
                hbaseStatus.dequeue()
        }

        hbaseStatus.foreach(x => log.info(x.toString))
    }

    def status = {
        log.info("----------:" + hbaseStatus.sum)

        hbaseStatus.sum
    }
}
