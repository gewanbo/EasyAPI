package com.wanbo.easyapi.client.lib

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig

import scala.collection.mutable

/**
 * Count the works every server do.
 * Created by wanbo on 2015/11/16.
 */
class WorkCounter(conf: EasyConfig) extends Runnable with Logging {

    def start(): Unit ={
        log.info("Starting up the work counter ...")

        val countThread = new Thread(new WorkCounter(conf))

        countThread.start()

        log.info("The work counter was running ...")
    }

    override def run(): Unit = {
        var timeMark = System.currentTimeMillis()

        while (true){

            try {

                if (WorkCounter.missQueue.size > 1000 || System.currentTimeMillis() - timeMark > 30000) {
                    var countList = List[(String, Long)]()
                    while (WorkCounter.missQueue.size > 0) {
                        val worker = WorkCounter.pull()
                        if(worker != "")
                            countList :+= (worker, 1L)
                    }
                    timeMark = System.currentTimeMillis()
                    // write counter to ZK
                    log.info("Current batch ------------------- :" + countList)

                    WorkCounter.updateSummary(countList)

                    log.info("Current summary ------------------- :" + WorkCounter.getSummary)
                    //WorkCounter.getSummary.foreach(println)

                    WorkCounterSync.sync(conf)
                } else {
                    // Waiting for 3 seconds.
                    Thread.sleep(3000)
                }

            } catch {
                case e: Exception =>
                    log.error("Work counting throws exception:", e)
            }

        }
    }
}


object WorkCounter {
    private val missQueue = mutable.Queue[String]()

    private var summary = Map[String, Long]()

    def push(worker: String): Unit ={
        missQueue.synchronized{
            missQueue += worker
        }
    }

    def pull(): String ={
        var ret = ""
        missQueue.synchronized{
            if(missQueue.size > 0)
                ret = missQueue.dequeue()
        }
        ret
    }

    def getSummary: Map[String, Long] ={
        summary
    }

    def updateSummary(list: List[(String, Long)]): Unit ={
        summary.synchronized{

            // Reset all record last time.
            summary = Map[String, Long]()

            list.foreach(item => {
//                var num = 0L
//                if(summary.contains(item._1)) {
//                    num = summary.getOrElse(item._1, 0)
//                }
//                summary = summary.updated(item._1, item._2 + num)
                summary = summary.updated(item._1, item._2)
            })
        }
    }
}
