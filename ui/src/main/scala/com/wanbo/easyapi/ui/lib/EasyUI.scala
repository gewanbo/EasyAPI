package com.wanbo.easyapi.ui.lib

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import com.wanbo.easyapi.ui.RedirectHandler
import com.wanbo.easyapi.ui.handlers.{SeedersPageHandler, ClientsPageHandler, ServersPageHandler}
import com.wanbo.easyapi.ui.pages._
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.servlet.ServletContextHandler

import scala.collection.mutable.ArrayBuffer

/**
 * EasyUI
 * Created by wanbo on 15/8/27.
 */
class EasyUI(conf: EasyConfig) extends Logging {

    private val _server = new HttpServer(conf)

    private var _pages = ArrayBuffer[WebPage]()

    private def initialize(): Unit = {

        // Global static
        val resourceHandler = new ResourceHandler
        resourceHandler.setDirectoriesListed(false)
        resourceHandler.setResourceBase("../webapp/static")

        val staticContext = new ServletContextHandler()
        staticContext.setContextPath("/static")
        staticContext.setHandler(resourceHandler)

        _server.attachHandler(staticContext)

        //_server.attachHandler(new RedirectHandler("/", "/servers"))

        val serversPage = new ServersPage

        serversPage.attachTab(new PageTab("Servers", "/servers"))
        serversPage.attachTab(new PageTab("Seeders", "/seeders"))
        serversPage.attachTab(new PageTab("Clients", "/clients"))

        serversPage.attachHandler(new ServersPageHandler(conf, "/servers", serversPage))

        _pages += serversPage

        val seedersPage = new SeedersPage

        seedersPage.attachTab(new PageTab("Servers", "/servers"))
        seedersPage.attachTab(new PageTab("Seeders", "/seeders"))
        seedersPage.attachTab(new PageTab("Clients", "/clients"))

        seedersPage.attachHandler(new SeedersPageHandler(conf, "/seeders", seedersPage))

        _pages += seedersPage

        val clientsPage = new ClientsPage

        clientsPage.attachTab(new PageTab("Servers", "/servers"))
        clientsPage.attachTab(new PageTab("Seeders", "/seeders"))
        clientsPage.attachTab(new PageTab("Clients", "/clients"))

        clientsPage.attachHandler(new ClientsPageHandler(conf, "/clients", clientsPage))

        _pages += clientsPage

        _pages.foreach(p => {
            p._handlers.foreach(h => {
                _server.attachHandler(h)
            })
        })

    }

    initialize()

    def start(): Unit ={
        _server.start()
    }

    def stop(): Unit ={
        _server.stop()
    }
}
