package com.wanbo.easyapi.shared.common.utils

import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.{TimeZone, Calendar}

/**
 * Utility functions.
 * Created by wanbo on 15/9/11.
 */
object Utils {

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
    def formatTablesNameByDate(days: Int, format: String = "Y_m_d", tabPrefix: String = ""): Set[String] ={
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
}