package com.wanbo.easyapi.ui.handlers

import java.io._
import java.net.Socket
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.wanbo.easyapi.shared.common.Logging
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import com.wanbo.easyapi.shared.common.utils.ZookeeperClient
import com.wanbo.easyapi.ui.lib.UIUtils
import com.wanbo.easyapi.ui.pages.WebPage
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.{AbstractHandler, ContextHandler}

import scala.xml.Node

/**
 * The handler for server list page.
 * Created by wanbo on 15/10/23.
 */
class SeedersPageHandler(conf: EasyConfig, contextPath: String, page: WebPage) extends ContextHandler with Logging {

    val handler = new AbstractHandler {
        override def handle(s: String, request: Request, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit = {
            httpServletResponse.setContentType("text/html; charset=utf-8")
            httpServletResponse.setStatus(HttpServletResponse.SC_OK)

            val out = httpServletResponse.getWriter

            var serverList = List[(String, Long)]()
            val servers = availableServers

            servers.foreach(s => {
                val info = getSummary(s)
                if(info != "") {
                    try {
                        info.split("\\|").map(_.split("=")).foreach(i => {
                            if(i.length > 1) {
                                val tk = i(0)
                                val tv = i(1).toLong

                                serverList = (tk, tv) +: serverList
                            }
                        })
                    } catch {
                        case e: Exception =>
                            log.error("Throws exception when parse server information.", e)
                    }
                }
            })

            val summaryData = serverList.groupBy(_._1).map(x=>{
                val tData = x._2.map(_._2)

                (x._1, tData.sum / tData.size)
            })

            page.title = "Seeders"
            page.content = makeTable(summaryData.toSeq.sortBy(_._1))

            log.info("Response contents ------------ seeder")
            out.println(UIUtils.commonNavigationPage(page))
            log.info("Response contents ------------ seeder finish")

            request.setHandled(true)
        }
    }

    this.setContextPath(contextPath)
    this.setHandler(handler)

    private def availableServers: Seq[String] = {
        var serverList = Seq[String]()
        val zk = new ZookeeperClient(conf.zkHosts)

        val serverNode = "/easyapi/servers"

        val servers = zk.getChildren(serverNode)

        serverList = servers.map(_.split(":")(0))

        zk.close()
        serverList.distinct
    }

    private def getSummary(host: String): String ={
        var info = ""

        try {
            val socket = new Socket(host, 8860)

            val outStream = socket.getOutputStream

            val out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outStream)))

            val inStream = new InputStreamReader(socket.getInputStream)
            val in = new BufferedReader(inStream)

            out.println("seedcount")
            out.flush()

            val msg = in.readLine()

            info = msg

            println(msg)

            out.close()
            outStream.close()

            in.close()
            inStream.close()

            socket.close()
        } catch {
            case e: Exception =>
                log.error("Error:", e)
        }

        info
    }

    private def makeTable(data: Seq[(String, Long)]): Seq[Node] = {

        val rows = data.map(r => {
            <tr>
                <td>{r._1}</td>
                <td>{r._2} ms</td>
            </tr>
        })

        <h2>Transaction performance metrics</h2>
            <p>All the available servers.</p>
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>TransactionType</th>
                        <th>AverageTime(ms)</th>
                    </tr>
                </thead>
                <tbody>
                    {rows}
                </tbody>
            </table>
    }
}