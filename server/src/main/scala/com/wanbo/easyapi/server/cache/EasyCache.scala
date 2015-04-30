package com.wanbo.easyapi.server.cache

/**
 * The abstract cache class.
 * Created by wanbo on 15/4/27.
 */
abstract class EasyCache {
    def get(name: String): String
    def set(name: String, data: String, expire: Int = 60): Boolean
    def del(name: String): Boolean
}
