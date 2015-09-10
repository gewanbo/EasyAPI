package com.wanbo.easyapi.server.actors

import java.io.{FileInputStream, PrintWriter, InputStreamReader, BufferedReader}
import java.net.Socket
import java.util.Properties

import akka.actor.{ActorRef, Props, Actor}
import com.wanbo.easyapi.server.database.MysqlDriver
import com.wanbo.easyapi.server.messages._
import com.wanbo.easyapi.shared.common.libs.EasyConfig
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

            // Load configuration
            conf.parseServerConf(confProps)

            // Verify configuration.
            val confVerification = conf.verifyConf()

            if(confVerification) {

                log.info("Initialize MySQL database configuration.")
                // Initialize Mysql database resources.
                val mysqlSettings = conf.driverSettings.filter(x => x._2.get("type").get == "mysql").toList.map(_._2)
                MysqlDriver.initializeDataSource(mysqlSettings)


                watcherController ! ListenerStart
            } else {
                log.error("Load configure file failed. Please check it.")
                context.stop(self)
            }

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
