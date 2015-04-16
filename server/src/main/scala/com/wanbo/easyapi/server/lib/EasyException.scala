package com.wanbo.easyapi.server.lib

/**
 * The exception class for api
 * Created by wanbo on 15/4/16.
 */
class EasyException(code: String, message: String) extends Exception(message: String) {

    private var _code: String = _

    def this(code: String) = {
        this(code, ErrorConstant.getErrorMessage(code))
        _code = code
    }

    def getCode: String = _code
}
