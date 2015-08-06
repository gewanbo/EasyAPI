package com.wanbo.easyapi.server.cache

import org.slf4j.LoggerFactory
import tachyon.TachyonURI
import tachyon.client.{TachyonFile, WriteType, ReadType, TachyonFS}
import tachyon.conf.TachyonConf

import scala.io.Source

/**
 * Tachyon cache class.
 * Created by wanbo on 15/8/3.
 */
class CacheTachyon(host: String, port: Int, expire: Int = 60) extends EasyCache {

    private var cacheClient: TachyonFS = null

    private val cacheRootPath = "/easyapi"

    private val log = LoggerFactory.getLogger(classOf[CacheTachyon])

    init()

    private def init(): Unit ={
        val tachyonConf = new TachyonConf()
        cacheClient = TachyonFS.get(new TachyonURI("tachyon://%s:%d".format(host, port)), tachyonConf)
    }

    /**
     * Get cache data
     * @param name The name of cache
     * @return String  The cache data.
     *         null    The cache doesn't exists.
     */
    override def get(name: String): String = {
        var data = ""

        try {

            val filePath = getFileURI(name)

            if(cacheClient.exist(filePath)){
                val cacheFile = cacheClient.getFile(filePath)

                if(cacheFile != null) {

                    if(isExpired(cacheFile)){
                        log.info("The cache named " + name + " was expired, need to update!!!")
                    }

                    val is = cacheFile.getInStream(ReadType.NO_CACHE)
                    val bytes = Source.fromInputStream(is)
                    data = bytes.mkString
                    is.close()
                } else
                    data = null
            } else {
                data = null
            }

        } catch {
            case e: Exception =>
                log.error("", e)
        }
        data
    }

    override def set(name: String, data: String): Boolean = {
        var ret = false

        try {

            val cacheFile = getFileURI(name)

            // Delete first
            if(cacheClient.exist(cacheFile)){
                cacheClient.delete(cacheFile, false)
            }

            val fId = cacheClient.createFile(cacheFile)
            val fs = cacheClient.getFile(fId)
            val os = fs.getOutStream(WriteType.MUST_CACHE)

            os.write(data.toCharArray.map(_.toByte))

            os.flush()
            os.close()

            ret = true

        } catch {
            case e: Exception =>
                log.error("", e)
        }

        ret
    }

    override def del(name: String): Boolean = {
        var ret = false

        try {

            val cacheFile = getFileURI(name)

            if(cacheClient.exist(cacheFile))
                cacheClient.delete(cacheFile, false)

            ret = true
        } catch {
            case e: Exception =>
                log.error("", e)
        }

        ret
    }

    private def getFileURI(name: String): TachyonURI ={
        val fileURI: TachyonURI = new TachyonURI(cacheRootPath + "/" + name)
        fileURI
    }

    private def isExpired(file: TachyonFile): Boolean ={
        var ret = false
        val createTime = file.getCreationTimeMs
        val currentTime = System.currentTimeMillis()
        val subTime = (currentTime - createTime) / 1000

        ttl = expire - subTime
        if(ttl < 0)
            ret = true
        ret
    }
}
