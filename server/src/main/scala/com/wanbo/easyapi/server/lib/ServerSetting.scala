package com.wanbo.easyapi.server.lib

/**
  * Created by wanbo on 16/6/15.
  */
class ServerSetting {
    var version = ""
    var host = ""

    def toJson: String ={
        "{\"version\":\"%s\",\"host\":\"%s\"}".format(version, host)
    }
}
