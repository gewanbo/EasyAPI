package com.wanbo.easyapi.ui.lib

import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.ServletContextHandler

import scala.collection.mutable.ArrayBuffer

/**
 * An http server.
 * Created by wanbo on 15/8/21.
 */
class HttpServer(conf: EasyConfig) {

    private var _server: Server = null
    private val _port: Int = conf.serverUIPort

    private var _handlers: ArrayBuffer[ServletContextHandler] = ArrayBuffer[ServletContextHandler]()

    def start(): Unit ={
        if(_server != null)
            throw new Exception("Server is already started.")
        else {
            doStart()
        }
    }

    /**
     * Actually start the HTTP server.
     */
    private def doStart(): Unit ={

        // The server
        _server = new Server()

        // The HTTP connector
        val http = new ServerConnector(_server)
        http.setHost(conf.serverHost)
        http.setPort(_port)
        http.setIdleTimeout(30000)

        // Set the connector
        _server.addConnector(http)

        // Set handlers
        if(_handlers.size > 0) {
            val collection = new ContextHandlerCollection
            collection.setHandlers(_handlers.toArray)

            _server.setHandler(collection)
        }

        // Start the server
        _server.start()
        _server.join()
    }

    def attachHandler(handler: ServletContextHandler): Unit ={
        _handlers += handler
    }

    def stop(): Unit ={
        if(_server == null)
            throw new Exception("Server is already stopped.")
        else {
            _server.stop()
            _server = null
        }
    }
}
