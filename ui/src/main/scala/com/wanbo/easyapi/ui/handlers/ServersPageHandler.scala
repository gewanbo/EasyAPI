package com.wanbo.easyapi.ui.handlers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ServerNodeFactory}
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import com.wanbo.easyapi.ui.lib.UIUtils
import com.wanbo.easyapi.ui.pages.WebPage
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.{AbstractHandler, ContextHandler}

import scala.xml.Node

/**
 * The handler for server list page.
 * Created by wanbo on 15/8/27.
 */
class ServersPageHandler(conf: EasyConfig, contextPath: String, page: WebPage) extends ContextHandler with Logging {

    val handler = new AbstractHandler {
        override def handle(s: String, request: Request, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit = {
            httpServletResponse.setContentType("text/html; charset=utf-8")
            httpServletResponse.setStatus(HttpServletResponse.SC_OK)

            val out = httpServletResponse.getWriter

            page.title = "Servers"
            page.content = makeTable(availableServers)

            log.info("Response contents ------------ server")
            out.println(UIUtils.commonNavigationPage(page))
            log.info("Response contents ------------ server finish")

            request.setHandled(true)
        }
    }

    this.setContextPath(contextPath)
    this.setHandler(handler)

    private def availableServers: Seq[(String, Long)] = {

        var serverList = Seq[(String, Long)]()
        val zk = new ZookeeperClient(conf.zkHosts)

        val serverNode = "/easyapi/servers"

        val servers = zk.getChildren(serverNode)

        servers.foreach(s => {
            var hitNum = 0L
            val hitData = zk.get(serverNode + "/" + s)

            if (hitData != null) {
                try {
                    val hits = new String(hitData)

                    log.info("Server [%s] - hits [%s] --------".format(s, hits))

                    hitNum = hits.toLong
                } catch {
                    case e: Exception =>
                }
            }

            serverList = serverList :+(s, hitNum)
        })

        zk.close()
        serverList
    }

    private def makeTable(data: Seq[(String, Long)]): Seq[Node] = {

        val servers = data.map(x => {
            val serverNode = ServerNodeFactory.parse(x._1)
            (serverNode.host, serverNode.port)
        }).groupBy(_._1).map(x => {

            val server = x._1
            val ports = x._2.map(_._2).mkString(",")

            <tr>
                <td>{server}</td>
                <td>{ports}</td>
                <td>1.1.4</td>
                <td>Running</td>
            </tr>
        })

        val rows = data.map(r => {
            <tr>
                <td>{r._1}</td>
                <td>{r._2}</td>
            </tr>
        })

        <h2>Server Nodes</h2>
            <p>All the available servers.</p>
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Server</th>
                        <th>Ports</th>
                        <th>Version</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    {servers}
                </tbody>
            </table>

        <h2>Hit balance detail</h2>
            <p>All nodes hit balance detail.</p>
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Server</th>
                        <th>Hits</th>
                    </tr>
                </thead>
                <tbody>
                    {rows}
                </tbody>
            </table>
    }
}