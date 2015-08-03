package com.wanbo.easyapi.server.cache

import org.slf4j.LoggerFactory
import tachyon.TachyonURI
import tachyon.client.{WriteType, ReadType, TachyonFS}
import tachyon.conf.TachyonConf

import scala.io.Source

/**
 * Tachyon cache class.
 * Created by wanbo on 15/8/3.
 */
class CacheTachyon(host: String, port: Int) extends EasyCache {

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

            val cacheFile = getFileURI(name)

            if(cacheClient.exist(cacheFile)){
                val tmp = cacheClient.getFile(cacheFile)
                if(tmp != null) {
                    val is = tmp.getInStream(ReadType.CACHE)
                    val bytes = Source.fromInputStream(is)
                    data = bytes.mkString
                    is.close()
                    ttl = -1L
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

    override def set(name: String, data: String, expire: Int = 60): Boolean = {
        var ret = false

        try {

            val cacheFile = getFileURI(name)

            val fs = cacheClient.getFile(cacheFile)
            val os = fs.getOutStream(WriteType.MUST_CACHE)

            os.write(data.toCharArray.map(_.toByte))

            os.flush()
            os.close()

            // TODO: Set ttl of cache

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
}
