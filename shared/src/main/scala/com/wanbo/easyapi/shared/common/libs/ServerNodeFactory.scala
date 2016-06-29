package com.wanbo.easyapi.shared.common.libs

/**
  * Server node factory
  * Created by wanbo on 16/4/21.
  */
object ServerNodeFactory {

    def parse(serverStr: String): ServerNode ={
        val hostAndPort = serverStr.split(":")

        if(hostAndPort.length == 2)
            ServerNode(hostAndPort(0), hostAndPort(1).toInt)
        else
            null
    }

}


case class ServerNode(host: String, port: Int) {
    override def toString: String ={
        host + ":" + port
    }
}