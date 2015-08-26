package com.wanbo.easyapi.ui

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import com.wanbo.easyapi.ui.lib.UIUtils
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

/**
 * Home handler
 * Created by wanbo on 15/8/21.
 */
class HomeHandler(greeting: String = "Hello World!") extends AbstractHandler {

    override def handle(s: String, request: Request, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit = {
        httpServletResponse.setContentType("text/html; charset=utf-8")
        httpServletResponse.setStatus(HttpServletResponse.SC_OK)

        val out = httpServletResponse.getWriter
        out.println(UIUtils.commonNavigationPage("Home"))

        request.setHandled(true)
    }
}
