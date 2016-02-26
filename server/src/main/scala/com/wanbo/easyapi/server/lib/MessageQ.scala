package com.wanbo.easyapi.server.lib

import com.wanbo.easyapi.server.messages.MsgQee
import com.wanbo.easyapi.shared.common.Logging

import scala.collection.mutable

/**
 * Message Queue
 * Created by wanbo on 2016/1/11.
 */
class MessageQ extends Serializable with Logging {

    var maxSize = 10000
    private val dataQueue = mutable.Queue[MsgQee]()

    /**
     * Push a message to queue
     */
    private def push(msgData: MsgQee): Boolean = {
        var ret = false

        try{

            dataQueue.synchronized {

                if(!dataQueue.contains(msgData)) {
                    if (dataQueue.size < maxSize) {
                        dataQueue += msgData
                        ret = true
                    } else {
                        throw new Exception("The queue was full.")
                    }
                } else {
                    log.info("The message was exists.")
                }

            }

        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }
        ret
    }

    /**
     * Pull a message from queue
     */
    private def pull(): MsgQee = {
        var retMsg: MsgQee = null

        dataQueue.synchronized{
            if(dataQueue.size > 0)
                retMsg = dataQueue.dequeue()
        }

        retMsg
    }

    private def getSize = dataQueue.size
}

object MessageQ extends Serializable with Logging {

    private val maxGroup = 10

    private var qeeGroup = Map[String, MessageQ]()

    def push(groupName: String, msgData: MsgQee): Boolean ={
        var ret = false

        try {
            qeeGroup.synchronized {
                if (!qeeGroup.contains(groupName)) {
                    if (qeeGroup.keys.size < maxGroup) {
                        qeeGroup += groupName -> new MessageQ
                    } else
                        throw new Exception("The group has reached the upper limit.")
                }
            }
            ret = qeeGroup(groupName).push(msgData)
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }

        ret
    }

    def pull(groupName: String): MsgQee = {
        if(qeeGroup.contains(groupName)){
            qeeGroup(groupName).pull()
        } else
            null
    }

    def getSize(queueName: String): Int ={
        if(qeeGroup.contains(queueName)){
            qeeGroup(queueName).getSize
        } else
            -1
    }
}