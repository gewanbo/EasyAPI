package com.wanbo.easyapi.server.lib

/**
 * The configuration of EasyApi
 * Created by wanbo on 15/4/8.
 */
class EasyConfig() {
    var serverHost: String = _
    var serverPort: Int = _

    var workersPort: List[Int] = _
    var workersMaxThreads: Int = _
}
