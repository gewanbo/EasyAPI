package com.wanbo.easyapi.ui

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}

/**
 * Redirect handler
 * Created by wanbo on 15/8/26.
 */
class RedirectHandler(contextPath: String, redirectUrl: String) extends ServletContextHandler {

    this.setContextPath(contextPath)

    val servlet = new HttpServlet {
        override protected def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
            doRedirect(req, resp)
        }

        override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
            doRedirect(req, resp)
        }

        private def doRedirect(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
            resp.sendRedirect(redirectUrl)
        }
    }

    val holder = new ServletHolder(servlet)

    this.addServlet(holder, this.getContextPath)
}