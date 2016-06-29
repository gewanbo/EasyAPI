package com.wanbo.easyapi.client.lib

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ServerNode}

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

                if (WorkCounter.dataQueue.size > 1000 || System.currentTimeMillis() - timeMark > 30000) {
                    var batchList = List[(ServerNode, MetricsItem)]()
                    while (WorkCounter.dataQueue.nonEmpty) {
                        val item = WorkCounter.pull()
                        batchList :+= item
                    }

                    if(batchList.nonEmpty) {
                        val batchSummary = batchList.groupBy(_._1).map{
                            case (serverNode: ServerNode, list: List[(ServerNode, MetricsItem)]) =>
                                val status = list.map(_._2.status)
                                val success = status.count(_ == MetricsStatus.success)
                                val failure = status.count(_ == MetricsStatus.failure)
                                (serverNode, success, failure)
                        }.toList

                        WorkCounter.updateSummary(batchSummary)

                        log.info("Current summary ------------------- :" + WorkCounter.getSummary)
                        //WorkCounter.getSummary.foreach(println)

                        // Sync data to ZK
                        WorkCounterSync.sync(conf)

                    } else {
                        log.info("Current batch list is empty!")
                    }

                    timeMark = System.currentTimeMillis()

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
    private val dataQueue = mutable.Queue[(ServerNode, MetricsItem)]()

    private var summary = Map[String, (Long, Long)]()

    def push(serverNode: ServerNode, metricsItem: MetricsItem): Unit ={
        dataQueue.synchronized{
            dataQueue.enqueue((serverNode, metricsItem))
        }
    }

    def pull(): (ServerNode, MetricsItem) ={
        var ret: (ServerNode, MetricsItem) = null
        dataQueue.synchronized{
            if(dataQueue.nonEmpty)
                ret = dataQueue.dequeue()
        }
        ret
    }

    def getSummary: Map[String, (Long, Long)] ={
        summary
    }

    def updateSummary(list: List[(ServerNode, Int, Int)]): Unit ={
        summary.synchronized{

            // Reset all record last time.
            summary = AvailableServer.serverList.map(x => (x._1, (0L, 0L)))

            list.foreach(item => {
                summary = summary.updated(item._1.toString, (item._2.toLong, item._3.toLong))
            })
        }
    }
}
