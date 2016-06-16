package com.wanbo.easyapi.server.lib

/**
  * Created by wanbo on 16/6/15.
  */
class ServerSetting {
    var version = ""
    var host = ""
    var startTime = ""

    def toJson: String ={
        "{\"Version\":\"%s\",\"Host\":\"%s\",\"StartTime\":\"%s\"}".format(version, host, startTime)
    }
}
