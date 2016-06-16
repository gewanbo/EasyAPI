package com.wanbo.easyapi.ui.handlers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.alibaba.fastjson.JSON
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.{EasyConfig, ServerNodeFactory}
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import com.wanbo.easyapi.ui.lib.{ServerSetting, UIUtils}
import com.wanbo.easyapi.ui.pages.WebPage
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.{AbstractHandler, ContextHandler}

import scala.xml.Node

/**
 * The handler for server list page.
 * Created by wanbo on 15/8/27.
 */
class ServersPageHandler(conf: EasyConfig, contextPath: String, page: WebPage) extends ContextHandler with Logging {

    private var serverPortHits = Seq[(String, Long)]()
    private var serverSet = Set[String]()
    private var serverSettings = Set[ServerSetting]()

    val handler = new AbstractHandler {
        override def handle(s: String, request: Request, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit = {
            httpServletResponse.setContentType("text/html; charset=utf-8")
            httpServletResponse.setStatus(HttpServletResponse.SC_OK)

            val out = httpServletResponse.getWriter

            collectData()

            page.title = "Servers"
            page.content = makeTable()

            log.info("Response contents ------------ server")
            out.println(UIUtils.commonNavigationPage(page))
            log.info("Response contents ------------ server finish")

            request.setHandled(true)
        }
    }

    this.setContextPath(contextPath)
    this.setHandler(handler)

    private def collectData(): Unit = {

        val zk = new ZookeeperClient(conf.zkHosts)

        val serverNode = "/easyapi/servers"

        val servers = zk.getChildren(serverNode)

        serverPortHits = servers.map(serverWithPort => {
            var hitNum = 0L
            val hitData = zk.get(serverNode + "/" + serverWithPort)

            if (hitData != null) {
                try {
                    val hits = new String(hitData)

                    log.info("Server [%s] - hits [%s] --------".format(serverWithPort, hits))

                    hitNum = hits.toLong
                } catch {
                    case e: Exception =>
                }
            }

            (serverWithPort, hitNum)
        })

        serverSet = servers.map(server => {
            val serverNode = ServerNodeFactory.parse(server)
            serverNode.host
        }).toSet

        serverSet.foreach(server => {
            val serverSettingRoot = "/easyapi/settings/servers/" + server
            if(zk.exists(serverSettingRoot)){
                val serverSettingBytes = zk.get(serverSettingRoot)
                if(serverSettingBytes != null){
                    val serverSettingData = new String(serverSettingBytes)
                    val settingObj = JSON.parseObject(serverSettingData)
                    val version = settingObj.getString("Version")
                    val host = settingObj.getString("Host")
                    val startTime = settingObj.getString("StartTime")
                    serverSettings = serverSettings + ServerSetting(version, host, startTime)
                }
            }
        })

        zk.close()
    }

    private def makeTable(): Seq[Node] = {

        val servers = serverPortHits.map(x => {
            val serverNode = ServerNodeFactory.parse(x._1)
            (serverNode.host, serverNode.port)
        }).groupBy(_._1).map(x => {

            val server = x._1
            val ports = x._2.map(_._2).mkString(",")

            var version = "0.0"
            var startTime = "0"

            val setting = serverSettings.find(_.host == server)
            setting.foreach(s => {
                version = s.version
                startTime = s.startTime
            })

            <tr>
                <td>{server}</td>
                <td>{ports}</td>
                <td>{version}</td>
                <td>{startTime}</td>
                <td>0</td>
            </tr>
        })

        val rows = serverPortHits.map(r => {
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
                        <th>Start Time</th>
                        <th>Requests Per Second</th>
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