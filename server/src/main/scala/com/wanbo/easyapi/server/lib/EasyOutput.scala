package com.wanbo.easyapi.server.lib

/**
 * Output
 * Created by wanbo on 15/4/27.
 */
class EasyOutput extends Serializable {
    var oelement: Map[String, String] = Map(("errorcode", "-1"), ("errormsg", ""))
    var odata: List[List[(String, Any)]] = _
}
