package com.wanbo.easyapi.shared.common.utils

import java.math.BigInteger

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
}
