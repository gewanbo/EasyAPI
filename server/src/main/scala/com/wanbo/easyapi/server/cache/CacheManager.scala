package com.wanbo.easyapi.server.cache

import com.wanbo.easyapi.server.lib.{ObjectSerialization, EasyConfig, EasyOutput}
import org.slf4j.LoggerFactory

/**
 * The manager of all type caches.
 * Created by wanbo on 15/4/27.
 */
class CacheManager(cacheType: String = "redis", expire: Int = 60) {

    private var _cacheType = "redis"

    private var cacher: EasyCache = _

    private val log = LoggerFactory.getLogger(classOf[CacheManager])

    init()

    def init() {

        val eConf = new EasyConfig

        cacheType match {
            case "redis" =>
                _cacheType = cacheType

                val hosts = eConf.getConfigure("cache.redis.hosts")
                val ports = eConf.getConfigure("cache.redis.ports")
                cacher = new CacheRedis(hosts, ports.toInt)

            case _ =>
                // Didn't match the type of cache.
        }
    }

    /**
     * Cache data
     * @return EasyOutput The cache exists, or write the cache data successful.
     *         null   The cache doesn't exist.
     */
    def cacheData(name: String, data: EasyOutput = new EasyOutput): EasyOutput ={

        var output = new EasyOutput

        try {
            val cache_name = cacheName(name)

            if (cacher == null)
                throw new Exception("There is no cache can use.")

            if(data == null) {
                // Delete cache
                cacher.del(cache_name)
            } else if (data.odata != null || data.oelement.size > 2) {
                // Set cache
                cacher.set(cache_name, ObjectSerialization.objectEncode(data))
                output = data
            } else {
                // Get cache
                val getData = cacher.get(cache_name)

                println("The get cache is---------:" + getData)

                if(getData == null)
                    output = null
                else
                    output = ObjectSerialization.objectDecode(getData).asInstanceOf[EasyOutput]
            }

        } catch {
            case e: Exception =>
                log.error("Cache data error:", e)
        }

        output
    }

    private def cacheName(name: String): String ={
        name.substring(5, 10) + java.security.MessageDigest.getInstance("MD5").digest(name.getBytes("UTF-8"))
    }
}
