package com.wanbo.easyapi.server

import com.wanbo.easyapi.server.lib.MessageQ
import com.wanbo.easyapi.server.messages.CacheUpdate

import scala.actors.threadpool.Executors

/**
 * Message Queue Test
 * Created by wanbo on 16/1/11.
 */
object MQTest {
    def main(args: Array[String]) {
        val qee1 = "qee1-"
        val qee2 = "qee2-"

        val threadPool = Executors.newFixedThreadPool(10)

        println("In queue:")

        for(i <- Range(1, 10)) {

            threadPool.submit(new Runnable {
                override def run(): Unit = {
                    for(k <- 0 until 100) {
                        println("push:" + MessageQ.push(qee1, CacheUpdate(qee1 + k.toString)))
                        println("push:" + MessageQ.push(qee2, CacheUpdate(qee1 + k.toString)))
//                        val msg = MessageQ.pull(qee1)
//                        println("pull:" + msg)
//                        if (msg != null) {
//                            Counter.add()
//                        }
                    }
                }
            })

        }

        Thread.sleep(5000)
        println("Q size:" + MessageQ.getSize(qee1))
        println("Q size:" + MessageQ.getSize(qee2))

        Thread.sleep(2000)
        println("Q size:" + MessageQ.getSize(qee1))
        println("Q size:" + MessageQ.getSize(qee2))

        println("Out queue:")



        for(i <- Range(1, 10)) {

            threadPool.submit(new Runnable {
                override def run(): Unit = {
                    while(MessageQ.getSize(qee1) > 0) {
                        val msg = MessageQ.pull(qee1)
                        if (msg != null) {
                            //println(msg)
                            Counter.add()
                        }
                    }
                }
            })

        }

        Thread.sleep(3000)

        println("Out num:" + Counter.getSize)

        Thread.sleep(3000)

        println("Out num:" + Counter.getSize)

        println("Q size:" + MessageQ.getSize(qee1))
        println("Q size:" + MessageQ.getSize(qee2))

        threadPool.shutdown()

        println("Done!")
    }
}
