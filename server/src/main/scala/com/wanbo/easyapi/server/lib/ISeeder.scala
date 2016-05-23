package com.wanbo.easyapi.server.lib

import com.wanbo.easyapi.server.database.IDriver
import com.wanbo.easyapi.shared.common.libs.EasyConfig

/**
 * The trait of workers
 * Created by GWB on 2015/4/8.
 */
trait ISeeder {
    var _conf: EasyConfig = _

    var driver: IDriver = _

    var manager: SeederManager = _

    var isUpdateCache = false

    def onHandle(seed: Map[String, Any]): EasyOutput
}
