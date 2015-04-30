package com.wanbo.easyapi.server.lib

/**
 * The abstract class of seeders
 * Created by GWB on 2015/4/8.
 */
abstract class Seeder {

    var name: String = _

    protected var fruits: EasyOutput = new EasyOutput()

    protected def onDBHandle(): Any
}
