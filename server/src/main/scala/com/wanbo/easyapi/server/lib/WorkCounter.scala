package com.wanbo.easyapi.server.lib

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig

import scala.collection.mutable

/**
 * Count the works every server do.
 * Created by wanbo on 15/9/15.
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

                if (WorkCounter.workQueue.size > 300 || System.currentTimeMillis() - timeMark > 30000) {
                    var countList = List[(String, Long)]()
                    while (WorkCounter.workQueue.size > 0) {
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
    private val workQueue = mutable.Queue[String]()

    private var summary = Map[String, Long]()

    def push(worker: String): Unit ={
        workQueue.synchronized{
            workQueue += worker
        }
    }

    def pull(): String ={
        var ret = ""
        workQueue.synchronized{
            if(workQueue.size > 0)
                ret = workQueue.dequeue()
        }
        ret
    }

    def getSummary: Map[String, Long] ={
        summary
    }

    def updateSummary(list: List[(String, Long)]): Unit ={
        summary.synchronized{
            list.foreach(item => {
                var num = 0L
                if(summary.contains(item._1)) {
                    num = summary.getOrElse(item._1, 0)
                }
                summary = summary.updated(item._1, item._2 + num)
            })
        }
    }
}
