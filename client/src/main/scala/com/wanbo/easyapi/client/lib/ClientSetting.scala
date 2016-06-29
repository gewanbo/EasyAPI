package com.wanbo.easyapi.client.lib

/**
  * Created by wanbo on 16/6/29.
  */
class ClientSetting {
    var version = ""
    var host = ""
    var startTime = ""

    def toJson: String ={
        "{\"Version\":\"%s\",\"Host\":\"%s\",\"StartTime\":\"%s\"}".format(version, host, startTime)
    }
}
