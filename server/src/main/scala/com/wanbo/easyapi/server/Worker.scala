package com.wanbo.easyapi.server

import java.io.{BufferedReader, PrintWriter}

/**
 * Worker
 * Created by wanbo on 15/3/30.
 */
class Worker(in: BufferedReader, out: PrintWriter) extends Runnable {

    override def run(): Unit = {
        val line = in.readLine()

        println(line)

        out.println(line)
        out.close()
        in.close()
    }
}
