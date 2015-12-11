package com.wanbo.easyapi.shared.common

import com.wanbo.easyapi.shared.common.utils.Utils

/**
 * Test for MD5
 * Created by root on 15-12-11.
 */
object MD5Test {

    def main(args: Array[String]) {
        val str = "123456"

        println(Utils.MD5(str))
    }
}
