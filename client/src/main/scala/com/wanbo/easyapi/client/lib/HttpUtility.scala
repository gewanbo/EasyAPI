package com.wanbo.easyapi.client.lib

/**
 * Http utility
 * Created by root on 15-12-11.
 */
object HttpUtility {

    val jsonHeader = "HTTP/1.1 200 OK\nContent-Type: application/json\n\n"
    val textHeader = "HTTP/1.1 200 OK\nContent-Type: text/plain\n\n"

    def post(message: String): String ={
        var responseMsg = ""

        responseMsg
    }

    /**
     * Extract http request body
     * @param msgData    The full request message.
     * @return           The part of body in full request message.
     */
    def parseBody(msgData: String): String ={
        var msgBody = ""

        var firstLine = true
        var hasBody = false
        var mark = false
        msgData.split("\r").foreach(x => {
            val body = x.trim

            if (mark)
                msgBody += body
            else {

                if (firstLine) {
                    if (body.contains("GET") || body.contains("POST") || body.contains("HTTP"))
                        hasBody = true
                }

                if (hasBody) {
                    if (body.isEmpty)
                        mark = true
                } else {
                    msgBody += body
                }

            }

            firstLine = false
        })

        msgBody
    }
}
