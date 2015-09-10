package com.wanbo.easyapi.server.lib

/**
 * Error code.
 * Created by wanbo on 15/4/16.
 */
object ErrorConstant {
    private var errorList = Map[String, String]()

    errorList = errorList.+("ERROR_UNDEFINED" -> "Undefined exception.")

    errorList = errorList.+("0" -> "Successful.")
    errorList = errorList.+("12002" -> "Undefined exception.")

    errorList = errorList.+("20001" -> "The input parameters were wrong.")
    errorList = errorList.+("20010" -> "The length of UserName was wrong.")
    errorList = errorList.+("20100" -> "There is no data in results data set.")

    errorList = errorList.+("40001" -> "The database connect failed.")
    errorList = errorList.+("40002" -> "The database operation throws exception.")

    errorList = errorList.+("99999" -> "Undefined exception.")

    def getErrorMessage(code: String): String = {
        if(errorList.contains(code))
            errorList.get(code).get
        else
            errorList.get("ERROR_UNDEFINED").get
    }
}
