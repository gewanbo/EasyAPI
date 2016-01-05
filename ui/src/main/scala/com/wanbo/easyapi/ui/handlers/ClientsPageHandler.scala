package com.wanbo.easyapi.ui.handlers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.alibaba.fastjson.JSON
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import com.wanbo.easyapi.ui.lib.UIUtils
import com.wanbo.easyapi.ui.pages.WebPage
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.{AbstractHandler, ContextHandler}


import scala.xml.Node
import scala.collection.JavaConverters._

/**
 * The handler for server list page.
 * Created by wanbo on 15/8/27.
 */
class ClientsPageHandler(conf: EasyConfig, contextPath: String, page: WebPage) extends ContextHandler with Logging {

    val handler = new AbstractHandler {
        override def handle(s: String, request: Request, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit = {

            var urlPath = request.getHttpURI.getPath.split("/")


            httpServletResponse.setContentType("text/html; charset=utf-8")
            httpServletResponse.setStatus(HttpServletResponse.SC_OK)

            val out = httpServletResponse.getWriter

            page.title = "Clients"
            page.content = makeTable(availableServers)

            if(urlPath.size < 3)
                urlPath :+= ""

            urlPath.apply(2) match {
                case "data" =>
                    out.println("{\n\t\"nodes\": [\n\t\t{\n\t\t\t\"id\": \"a\",\n\t\t\t\"name\": \"A\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"b\",\n\t\t\t\"name\": \"B\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"c\",\n\t\t\t\"name\": \"C\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"d\",\n\t\t\t\"name\": \"D\"\n\t\t}\n\t],\n\t\"links\": [\n\t\t{\n\t\t\t\"source\": 0,\n\t\t\t\"target\": 1,\n\t\t\t\"value\": 0.3\n\t\t},\n\t\t{\n\t\t\t\"source\": 0,\n\t\t\t\"target\": 2,\n\t\t\t\"value\": 0.5\n\t\t},\n\t\t{\n\t\t\t\"source\": 3,\n\t\t\t\"target\": 2,\n\t\t\t\"value\": 0.5\n\t\t},\n\t\t{\n\t\t\t\"source\": 3,\n\t\t\t\"target\": 1,\n\t\t\t\"value\": 0.2\n\t\t}\n\t]\n}")
                case _ =>
                    log.info("Response contents ------------ client")
                    out.println(UIUtils.commonNavigationPage(page))
                    log.info("Response contents ------------ client finish")
            }

            request.setHandled(true)
        }
    }

    this.setContextPath(contextPath)
    this.setHandler(handler)

    private def availableServers: Seq[(String, Int)] = {
        var clientList = Seq[(String, Int)]()
        val zk = new ZookeeperClient(conf.zkHosts)

        val clientNode = "/easyapi/clients"

        val clients = zk.getChildren(clientNode)

        var nodesSet = Set[String]()
        var linksList = List[(String, String, Long)]()

        clients.map(s => {
            var hitNum = 0
            val hitData = zk.get(clientNode + "/" + s)

            nodesSet += s

            if (hitData != null) {
                try {
                    val hits = new String(hitData)

                    log.info("Server [%s] - hits [%s] --------".format(s, hits))

                    val hitObj = JSON.parseObject(hits)

                    val miss = hitObj.getString("miss")

                    val missObj = JSON.parseObject(miss)

                    val servers = missObj.keySet().asScala.toList

                    servers.foreach(serverKey => {

                        nodesSet += serverKey

                        linksList :+= (s, serverKey, missObj.getLong(serverKey).toLong)
                    })

                    log.info("miss string-----:" + miss)

                } catch {
                    case e: Exception =>
                        log.error("Error:", e)
                }
            }

            clientList = clientList :+(s, hitNum)
        })

        zk.close()
        clientList
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
        <div id="chart" class="box"></div>

    }

    case class node(id: String, name: String)
    case class link(source: Int, target: Int, value: Double)
}