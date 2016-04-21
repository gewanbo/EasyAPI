package com.wanbo.easyapi.shared.common.utils

import java.io._
import java.math.BigInteger
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.{Calendar, TimeZone}

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.ServerNode

/**
 * Utility functions.
 * Created by wanbo on 15/9/11.
 */
object Utils extends Logging {

    def MD5(str: String): String ={
        val m = java.security.MessageDigest.getInstance("MD5")
        m.update(str.getBytes, 0, str.length)
        new BigInteger(1, m.digest()).toString(16)
    }

    /**
     * Format tables' name by date.
     *
     * @param days         How many days to generate.
     * @param format       Format string.
     * @param tabPrefix    Prefix of table name.
     * @return             The formatted string.
     */
    def formatTablesNameByDate(days: Int, format: String = "yyyy_MM_dd", tabPrefix: String = ""): Set[String] ={
        var tabSet = Set[String]()

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"))
        val sdf = new SimpleDateFormat(format)

        if(days >= 0) {

            for (i <- Range(0, days + 1)) {
                tabSet += tabPrefix + sdf.format(calendar.getTimeInMillis)
                calendar.add(Calendar.DATE, -1)
            }

        }

        tabSet
    }

    /**
      * Make a simple request
      *
      * @param server    The server host and port
      * @param msg       The message will be send
      * @param timeOut   Request timeout
      * @return          The response string
      */
    def simpleRequest(server: ServerNode, msg: String, timeOut: Int = 30000): String ={
        var responseMsg = ""

        try {

            val socket = new Socket(server.host, server.port)
            socket.setSoTimeout(timeOut)

            val outStream = socket.getOutputStream

            val out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outStream)))

            val inStream = new InputStreamReader(socket.getInputStream)
            val in = new BufferedReader(inStream)

            out.println(msg)
            out.flush()

            responseMsg = in.readLine()

            out.close()
            outStream.close()

            in.close()
            inStream.close()

            socket.close()
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }

        responseMsg
    }
}