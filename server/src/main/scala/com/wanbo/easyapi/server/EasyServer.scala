package com.wanbo.easyapi.server

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{ServerSocket, Socket}
import java.util.concurrent.Executors

/**
 * The server of EasyAPI
 * Created by wanbo on 15/3/30.
 */
object EasyServer {
    def main(args: Array[String]) {
        val socket = new ServerSocket(8800)

        val execService = Executors.newFixedThreadPool(10)

        var client: Socket = socket.accept()
        while( client != null ) {

            // In message
            val in = new BufferedReader(new InputStreamReader(client.getInputStream))

            // Out message
            val out = new PrintWriter(client.getOutputStream, true)

            execService.submit(new Worker(in, out))

            // Accept next message
            client = socket.accept()
        }

        client.close()
        println("Done.")
    }
}