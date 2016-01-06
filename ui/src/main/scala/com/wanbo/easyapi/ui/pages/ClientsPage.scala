package com.wanbo.easyapi.ui.pages

/**
 * Clients page
 * Created by wanbo on 15/8/27.
 */
class ClientsPage extends WebPage {

    val styles =
        """
            body {padding: 10px;min-width: 600px;max-width: 1200px;margin: auto;}
            #chart {
                height: 500px;
                font: 13px sans-serif;
            }
            .node rect {
                fill-opacity: .9;
                shape-rendering: crispEdges;
                stroke-width: 0;
            }
            .node text {
                text-shadow: 0 1px 0 #fff;
            }
            .link {
                fill: none;
                stroke: #000;
                stroke-opacity: .2;
            }
        """

    htmlHeaders = {
        <style>{styles}</style>
    }

    htmlFooters = {
        <script src="/static/js/d3.v3.min.js"></script>
        <script src="/static/js/sankey.js"></script>
        <script src="/static/js/d3.chart.min.js"></script>
        <script src="/static/js/d3.chart.sankey.min.js"></script>
        <script src="/static/js/clients.js"></script>
    }
}