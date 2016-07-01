package com.wanbo.easyapi.ui.handlers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import com.wanbo.easyapi.ui.lib.{ClientSetting, UIUtils}
import com.wanbo.easyapi.ui.pages.WebPage
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.{AbstractHandler, ContextHandler}

import scala.collection.JavaConverters._
import scala.xml.Node

/**
 * The handler for server list page.
 * Created by wanbo on 15/8/27.
 */
class ClientsPageHandler(conf: EasyConfig, contextPath: String, page: WebPage) extends ContextHandler with Logging {

    private var clientSet = Set[String]()
    private var clientSettings = Set[ClientSetting]()

    val handler = new AbstractHandler {
        override def handle(s: String, request: Request, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit = {

            var urlPath = request.getHttpURI.getPath.split("/")

            httpServletResponse.setContentType("text/html; charset=utf-8")
            httpServletResponse.setStatus(HttpServletResponse.SC_OK)

            val out = httpServletResponse.getWriter

            if(urlPath.length < 3)
                urlPath :+= ""

            urlPath.apply(2) match {
                case "data" =>
                    out.println(clientsData)
                case _ =>

                    page.title = "Clients"
                    page.content = makeTable(Seq[(String, Int)]())

                    log.info("Response contents ------------ client")
                    out.println(UIUtils.commonNavigationPage(page))
                    log.info("Response contents ------------ client finish")
            }

            request.setHandled(true)
        }
    }

    this.setContextPath(contextPath)
    this.setHandler(handler)

    private def clientsData: String = {

        val zk = new ZookeeperClient(conf.zkHosts)

        val clientNode = "/easyapi/clients"

        val clients = zk.getChildren(clientNode)

        var nodesSet = Set[String]()
        var linksList = List[(String, String, Long)]()

        clients.foreach(client => {

            val hitData = zk.get(clientNode + "/" + client)

            nodesSet += client
            clientSet += client

            if (hitData != null) {
                try {
                    val hits = new String(hitData)

                    log.info("Server [%s] - hits [%s] --------".format(client, hits))

                    val hitObj = JSON.parseObject(hits)

                    val miss = hitObj.getString("failure")

                    val missObj = JSON.parseObject(miss)

                    val servers = missObj.keySet().asScala.toList

                    servers.foreach(serverKey => {

                        nodesSet += serverKey

                        linksList :+= (client, serverKey, missObj.getLong(serverKey).toLong)
                    })

                } catch {
                    case e: Exception =>
                        log.error("Error:", e)
                }
            }

        })

        linksList.foreach(println)

        val jsonNodes = new JSONArray()
        nodesSet.foreach(x => {
            val obj = new JSONObject()
            obj.put("id", x)
            obj.put("name", x)
            jsonNodes.add(obj)
        })

        var countId = -1

        val keyMap = nodesSet.map(x => {
            countId += 1
            (x, countId)
        }).toMap

        val totalNum = linksList.map(_._3).sum.toFloat

        val jsonLinks = new JSONArray()
        linksList.foreach(x => {
            val linkObj = new JSONObject()
            linkObj.put("source", keyMap(x._1))
            linkObj.put("target", keyMap(x._2))
            linkObj.put("value", x._3 / totalNum)

            jsonLinks.add(linkObj)
        })

        val jsonObj = new JSONObject()
        jsonObj.put("nodes", jsonNodes)
        jsonObj.put("links", jsonLinks)

        // Clean up settings
        clientSettings = Set[ClientSetting]()

        clientSet.foreach(client => {
            val clientSettingRoot = "/easyapi/settings/clients/" + client
            if(zk.exists(clientSettingRoot)){
                val clientSettingBytes = zk.get(clientSettingRoot)
                if(clientSettingBytes != null){
                    val clientSettingData = new String(clientSettingBytes)
                    val settingObj = JSON.parseObject(clientSettingData)
                    val version = settingObj.getString("Version")
                    val host = settingObj.getString("Host")
                    val startTime = settingObj.getString("StartTime")
                    clientSettings = clientSettings + ClientSetting(version, host, startTime)
                }
            }
        })

        zk.close()


        jsonObj.toJSONString
    }

    private def makeTable(data: Seq[(String, Int)]): Seq[Node] = {

        val clients = clientSettings.map(x => {
            <tr>
                <td>{x.host}</td>
                <td>{x.version}</td>
                <td>{x.startTime}</td>
            </tr>
        })
        <h2>Clents list</h2>
            <p>All the running clients.</p>
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Server</th>
                        <th>Version</th>
                        <th>Start Time</th>
                    </tr>
                </thead>
                <tbody>
                    {clients}
                </tbody>
            </table>


        <h2>Clients working stream</h2>
        <p>All clients call from all available servers.</p>
        <div id="chart" class="box"></div>
    }

    case class node(id: String, name: String)
    case class link(source: Int, target: Int, value: Double)
}