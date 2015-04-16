package com.wanbo.easyapi.server.database

import com.wanbo.easyapi.server.lib.EasyConfig

/**
 * Database driver
 * Created by wanbo on 15/4/16.
 */
trait Driver {
    def setConfiguration(conf: EasyConfig)
}
