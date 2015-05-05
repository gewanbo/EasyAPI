package com.wanbo.easyapi.server.cache

import com.redis.RedisClient
import org.slf4j.LoggerFactory

/**
 * Redis cache class.
 * Created by wanbo on 15/4/27.
 */
class CacheRedis(host: String, port: Int) extends EasyCache {

    private val redis = new RedisClient(host, port)

    private val log = LoggerFactory.getLogger(classOf[CacheRedis])

    /**
     * Get cache data
     * @param name The name of cache
     * @return String  The cache data.
     *         null    The cache doesn't exists.
     */
    override def get(name: String): String = {
        var data = ""

        try {

            if(redis.exists(name)){
                val tmp = redis.get(name)
                if(tmp != null) {
                    data = tmp.get
                    ttl = redis.ttl(name).get
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

            ret = redis.set(name, data)
            redis.expire(name, expire)

        } catch {
            case e: Exception =>
                log.error("", e)
        }

        ret
    }

    override def del(name: String): Boolean = {
        var ret = false

        try {

            redis.del(name)

            ret = true
        } catch {
            case e: Exception =>
                log.error("", e)
        }

        ret
    }
}
