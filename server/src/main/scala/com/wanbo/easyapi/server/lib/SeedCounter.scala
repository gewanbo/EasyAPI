package com.wanbo.easyapi.server.lib

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig

/**
 * Seed counter
 * Created by wanbo on 2015/10/23.
 */
class SeedCounter(conf: EasyConfig) extends Runnable with Logging {

    def start(): Unit ={
        log.info("Starting up the seed counter ...")

        val countThread = new Thread(new SeedCounter(conf))

        countThread.start()

        log.info("The seed counter was running ...")
    }

    override def run(): Unit = {
        var timeMark = System.currentTimeMillis()

        while (true){

            try {

                if (System.currentTimeMillis() - timeMark > 30000) {

                    val summaryData = SeedCounter.pull().map(x => (x._1, x._3 - x._2)).groupBy(_._1).map(x => {
                        val data = x._2.map(_._2)
                        val mean = data.sum / data.size
                        (x._1, mean)
                    })

                    timeMark = System.currentTimeMillis()
                    // write counter to ZK
                    SeedCounter.updateSummary(summaryData)

                    log.info("Current summary ------------------- :" + SeedCounter.getSummary)
                    SeedCounter.getSummary.foreach(println)

                } else {
                    // Waiting for 10 seconds.
                    Thread.sleep(10000)
                }

            } catch {
                case e: Exception =>
                    log.error("Work counting throws exception:", e)
            }

        }
    }
}

object SeedCounter {
    private var seedQueue = List[(String, Long, Long)]()

    private var summary = Map[String, Long]()

    def push(info: (String, Long, Long)): Unit ={
        seedQueue.synchronized{
            seedQueue = info +: seedQueue
        }
    }

    def pull(): List[(String, Long, Long)] ={
        seedQueue.synchronized{
            //println(seedQueue)
            seedQueue = seedQueue.filter(_._2 > (System.currentTimeMillis() - 60000))
            //println(seedQueue)
        }
        seedQueue
    }

    def getSummary: Map[String, Long] ={
        summary
    }

    def updateSummary(list: Map[String, Long]): Unit ={
        summary.synchronized{
            summary = list
        }
    }
}