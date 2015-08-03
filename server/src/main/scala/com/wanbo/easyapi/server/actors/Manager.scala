package com.wanbo.easyapi.server.actors

import java.io.{FileInputStream, PrintWriter, InputStreamReader, BufferedReader}
import java.net.Socket
import java.util.Properties

import akka.actor.{ActorRef, Props, Actor}
import com.wanbo.easyapi.server.lib.EasyConfig
import com.wanbo.easyapi.server.messages._
import org.slf4j.{MDC, LoggerFactory}

/**
 * Manager
 * Created by wanbo on 15/4/3.
 */
class Manager(workTracker: ActorRef) extends Actor {

    protected val conf: EasyConfig = new EasyConfig

    private val log = LoggerFactory.getLogger(classOf[Manager])

    val watcherController = context.actorOf(Props(new WatcherController(conf, self)), name = "watcher_controller")

    MDC.put("destination", "system")

    override def receive: Receive = {
        case StartUp =>
            workTracker ! StartUp

            val confProps = new Properties()
            val configFile = System.getProperty("easy.conf", "config.properties")
            confProps.load(new FileInputStream(configFile))

            conf.serverId = confProps.getProperty("server.id", "0")
            conf.serverHost = confProps.getProperty("server.host", "localhost")
            conf.serverPort = confProps.getProperty("server.port", "8800").toInt

            val workers_port = confProps.getProperty("server.worker.port", "8801")

            conf.workersPort = workers_port.split(";").toList.map(_.toInt)

            conf.workersMaxThreads = confProps.getProperty("server.worker.max_threads", "10").toInt

            conf.zkEnable = confProps.getProperty("zookeeper.enable", "true").toBoolean
            conf.zkHosts = confProps.getProperty("zookeeper.hosts", "localhost:2181")

            conf.cache_type = confProps.getProperty("cache.type", "redis")

            conf.driver_mysql = conf.driver_mysql.+("mysql.db.host" -> confProps.getProperty("mysql.db.host", "localhost"))
            conf.driver_mysql = conf.driver_mysql.+("mysql.db.port" -> confProps.getProperty("mysql.db.port", "3306"))
            conf.driver_mysql = conf.driver_mysql.+("mysql.db.username" -> confProps.getProperty("mysql.db.username", "root"))
            conf.driver_mysql = conf.driver_mysql.+("mysql.db.password" -> confProps.getProperty("mysql.db.password", ""))

            watcherController ! ListenerStart

        case ListenerRunning(null, workers) =>
            workTracker ! ListenerRunning(conf, workers)

        case ListenerFailed =>
            workTracker ! ListenerFailed
            self ! ShutDown("Listener starting failed.")

        case ManagerCommand(client) =>
            val cmd = parseCommand(client)
            if(cmd == "shutdown")
                self ! ShutDown("Command shut down.")
            if(cmd == "clean cache")
                println("Clean cache.")

        case ShutDown(msg) =>
            workTracker ! ShutDown(msg)
            watcherController ! WatcherStop
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
                log.error("Manage process exception :", e)
        }

        message
    }
}
