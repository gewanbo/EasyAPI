package com.wanbo.easyapi.ui.lib

import scala.xml.Node

/**
 * Utility functions for UI page contents.
 * Created by wanbo on 15/8/26.
 */
private[easyapi] object UIUtils {

    def commonHeaderNodes: Seq[Node] = {
            <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
            <link rel="stylesheet" href="/static/css/bootstrap.min.css" type="text/css"/>
            <script src="/static/jquery-1.11.3.min.js"></script>
    }

    def commonNavigationPage(title: String): Seq[Node] ={
        <html>
            <head>
                {commonHeaderNodes}
                <title>{title}</title>
            </head>
            <body>
                <nav class="navbar navbar-default navbar-static-top">
                    <div class="container">
                        <div class="navbar-header">
                            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                                <span class="sr-only">Toggle navigation</span>
                                <span class="icon-bar"></span>
                                <span class="icon-bar"></span>
                                <span class="icon-bar"></span>
                            </button>
                            <a class="navbar-brand" href="#">EasyAPI</a>
                        </div>
                        <div id="navbar" class="navbar-collapse collapse">
                            <ul class="nav navbar-nav">
                                <li class="active"><a href="#">Servers</a></li>
                                <li><a href="#">Clients</a></li>
                            </ul>
                            <ul class="nav navbar-nav navbar-right">
                                <li><a href="#">Help</a></li>
                            </ul>
                        </div>
                    </div>
                </nav>

            </body>
        </html>
    }
}
