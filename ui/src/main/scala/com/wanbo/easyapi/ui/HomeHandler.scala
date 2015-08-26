package com.wanbo.easyapi.ui

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.wanbo.easyapi.shared.common.libs.EasyConfig
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import com.wanbo.easyapi.ui.lib.UIUtils
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.slf4j.LoggerFactory

import scala.xml.Node

/**
 * Home handler
 * Created by wanbo on 15/8/21.
 */
class HomeHandler(conf: EasyConfig) extends AbstractHandler {

    private val log = LoggerFactory.getLogger(classOf[HomeHandler])

    override def handle(s: String, request: Request, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit = {
        httpServletResponse.setContentType("text/html; charset=utf-8")
        httpServletResponse.setStatus(HttpServletResponse.SC_OK)

        val out = httpServletResponse.getWriter

        log.info("Response contents ------------")
        out.println(UIUtils.commonNavigationPage("Servers", makeTable(availableServers)))
        log.info("Response contents ------------ finish")

        request.setHandled(true)
    }

    private def availableServers: Seq[(String, Int)] ={
        var serverList = Seq[(String, Int)]()
        val zk = new ZookeeperClient(conf.zkHosts)

        val serverNode = "/easyapi/servers"

        val servers = zk.getChildren(serverNode)

        servers.map(s => {
            var hitNum = 0
            val hits = zk.get(serverNode + "/" + s).mkString

            try {

                log.info("Server [%s] - hits [%s] --------".format(s, hits))

                hitNum = hits.toInt
            } catch {
                case e: Exception =>
            }

            serverList = serverList :+ (s, hitNum)
        })

        zk.close()
        serverList
    }

    private def makeTable(data: Seq[(String, Int)]): Seq[Node] = {

        val rows = data.map(r => {
            <tr>
                <td>{r._1}</td>
                <td>{r._2}</td>
            </tr>
        })

        <h2>Server List</h2>
        <p>All the active servers.</p>
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
