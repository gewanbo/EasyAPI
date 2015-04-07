package com.wanbo.easyapi.server.actors

import java.io.{FileInputStream, PrintWriter, InputStreamReader, BufferedReader}
import java.net.Socket
import java.util.Properties

import akka.actor.{ActorRef, Props, Actor}
import com.wanbo.easyapi.server.messages._

/**
 * Manager
 * Created by wanbo on 15/4/3.
 */
class Manager(workTracker: ActorRef) extends Actor {

    protected val _conf: Properties = new Properties()

    val watcherController = context.actorOf(Props(new WatcherController(_conf, self)), name = "watcher_controller")

    override def receive: Receive = {
        case StartUp =>
            workTracker ! StartUp

            val configFile = System.getProperty("easy.conf", "config.properties")
            _conf.load(new FileInputStream(configFile))

            watcherController ! ListenerStart(_conf)

        case ListenerRunning =>
            workTracker ! ListenerRunning

        case ListenerFailed =>
            workTracker ! ListenerFailed
            self ! ShutDown("Listener starting failed.")

        case ManagerCommand(client) =>
            val cmd = parseCommand(client)
            if(cmd == "shutdown")
                self ! ShutDown("Command shut down.")

        case ShutDown(msg) =>
            workTracker ! ShutDown(msg)
            watcherController ! WatcherStop(_conf)
            context.stop(self)
    }

    def parseCommand(client: Socket): String = {

        var message = ""

        try {

            // In message
            val in = new BufferedReader(new InputStreamReader(client.getInputStream))

            // Out message
            val out = new PrintWriter(client.getOutputStream, true)

            message = in.readLine()

            println("Manager command is :" + message)

            // Response message
            out.println("OK")

            out.close()
            in.close()
            client.close()
        } catch {
            case e: Exception =>
                println("Manage process exception :", e)
        }

        message
    }
}
