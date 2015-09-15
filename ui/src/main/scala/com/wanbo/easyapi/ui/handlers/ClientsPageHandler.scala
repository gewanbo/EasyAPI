package com.wanbo.easyapi.ui.handlers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import com.wanbo.easyapi.ui.lib.UIUtils
import com.wanbo.easyapi.ui.pages.WebPage
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.{AbstractHandler, ContextHandler}

import scala.xml.Node

/**
 * The handler for server list page.
 * Created by wanbo on 15/8/27.
 */
class ClientsPageHandler(conf: EasyConfig, contextPath: String, page: WebPage) extends ContextHandler with Logging {

    val handler = new AbstractHandler {
        override def handle(s: String, request: Request, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit = {
            httpServletResponse.setContentType("text/html; charset=utf-8")
            httpServletResponse.setStatus(HttpServletResponse.SC_OK)

            val out = httpServletResponse.getWriter

            page.title = "Clients"
            page.content = makeTable(availableServers)

            log.info("Response contents ------------ client")
            out.println(UIUtils.commonNavigationPage(page))
            log.info("Response contents ------------ client finish")

            request.setHandled(true)
        }
    }

    this.setContextPath(contextPath)
    this.setHandler(handler)

    private def availableServers: Seq[(String, Int)] ={
        //        var serverList = Seq[(String, Int)]()
        //        val zk = new ZookeeperClient(conf.zkHosts)
        //
        //        val serverNode = "/easyapi/servers"
        //
        //        val servers = zk.getChildren(serverNode)
        //
        //        servers.map(s => {
        //            var hitNum = 0
        //            val hitData = zk.get(serverNode + "/" + s)
        //
        //            if(hitData != null) {
        //                try {
        //                    val hits = new String(hitData)
        //
        //                    log.info("Server [%s] - hits [%s] --------".format(s, hits))
        //
        //                    hitNum = hits.toInt
        //                } catch {
        //                    case e: Exception =>
        //                }
        //            }
        //
        //            serverList = serverList :+ (s, hitNum)
        //        })

        //        zk.close()
        //        serverList
        Seq[(String, Int)](("ser1", 8284), ("sr23", 3838))
    }

    private def makeTable(data: Seq[(String, Int)]): Seq[Node] = {

        val rows = data.map(r => {
            <tr>
                <td>{r._1}</td>
                <td>{r._2}</td>
            </tr>
        })

        <h2>Server List</h2>
            <p>All the available servers.</p>
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Client</th>
                        <th>Hits</th>
                    </tr>
                </thead>
                <tbody>
                    {rows}
                </tbody>
            </table>
    }
}