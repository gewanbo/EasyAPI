package com.wanbo.easyapi.server.cache

import com.wanbo.easyapi.server.lib.{ObjectSerialization, EasyOutput}
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory

/**
 * The manager of all type caches.
 * Created by wanbo on 15/4/27.
 */
class CacheManager(conf: EasyConfig, cacheType: String = "", expire: Int = 60) {

    private val _systemCacheTypes = Array("redis", "tachyon")

    private var _cacheType = "redis"

    private var easyCache: EasyCache = _

    private val log = LoggerFactory.getLogger(classOf[CacheManager])

    var ttl: Long = _

    init()

    def init() {

        try {

            _cacheType = conf.cache_type

            // Set the cache type by Seeder custom.
            if(cacheType != "" && cacheType != _cacheType && _systemCacheTypes.contains(cacheType))
                _cacheType = cacheType

            _cacheType match {
                case "redis" =>

                    val redis_hosts = conf.getConfigure("cache.redis.hosts")
                    val redis_ports = conf.getConfigure("cache.redis.ports")
                    easyCache = new CacheRedis(redis_hosts, redis_ports.toInt, expire)

                case "tachyon" =>

                    val tachyon_hosts = conf.getConfigure("cache.tachyon.hosts")
                    val tachyon_ports = conf.getConfigure("cache.tachyon.ports")
                    easyCache = new CacheTachyon(tachyon_hosts, tachyon_ports.toInt, expire)

                case _ =>
                // Didn't match the type of cache.
            }

        } catch {
            case e: Exception =>
                log.error("Throws exception when initialize cache manager:", e)
        }
    }

    /**
     * Cache data
     * @return EasyOutput The cache exists, or write the cache data successful.
     *         null   The cache doesn't exist.
     */
    def cacheData(name: String, data: EasyOutput = new EasyOutput, inExpire: Int = -1): EasyOutput ={

        var output = new EasyOutput

        try {
            val cache_name = cacheName(name)

            if (easyCache == null)
                throw new Exception("There is no cache can use.")

            if(data == null) {
                // Delete cache
                easyCache.del(cache_name)
            } else if (data.odata != null || data.oelement.size > 2) {
                // Set cache
                if(inExpire > 0)
                    easyCache.set(cache_name, ObjectSerialization.objectEncode(data), inExpire)
                else
                    easyCache.set(cache_name, ObjectSerialization.objectEncode(data))
                output = data
            } else {
                // Get cache
                val getData = easyCache.get(cache_name)

                if(getData == null)
                    output = null
                else {
                    output = ObjectSerialization.objectDecode(getData).asInstanceOf[EasyOutput]
                    ttl = easyCache.ttl
                }
            }

        } catch {
            case e: Exception =>
                log.error("Cache data error:", e)
        }

        output
    }

    def close(): Unit ={
        try {

            if(easyCache != null)
                easyCache.close()

        } catch {
            case e: Exception =>
                log.error("Throws exception when initialize cache manager:", e)
        }
    }

    private def cacheName(name: String): String ={
        name.filter(_.isDigit).substring(0, 5) + new String(Hex.encodeHex(java.security.MessageDigest.getInstance("MD5").digest(name.getBytes("UTF-8"))))
    }
}
