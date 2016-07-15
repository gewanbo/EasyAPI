package com.wanbo.easyapi.server.actors

import java.io._
import java.net.Socket
import java.util.Properties

import akka.actor.{Actor, ActorRef, Props}
import com.wanbo.easyapi.server.database.{MongoDriver, MysqlDriver}
import com.wanbo.easyapi.server.lib.{ErrorConstant, SeedCounter, WorkCounter}
import com.wanbo.easyapi.server.messages._
import com.wanbo.easyapi.shared.common.libs.EasyConfig
import org.slf4j.{LoggerFactory, MDC}

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

            // Load custom constant of error message.
            loadErrorMessage()

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

                // Initialize MongoDB database resources.
                val mongoSettings = conf.driverSettings.filter(x => x._2.get("type").get == "mongo").toList.map(_._2)
                MongoDriver.initializeDataSource(mongoSettings)

                // Start up work counter
                val workCounter = new WorkCounter(conf)
                workCounter.start()

                val seedCounter = new SeedCounter(conf)
                seedCounter.start()

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

            message match {
                case "workcount" =>
                    var dataStr = ""
                    var split = ""
                    WorkCounter.getSummary.foreach(x => {
                        dataStr += split + "%s=%d".format(x._1, x._2)
                        split = "|"
                    })
                    out.println(dataStr)
                case "seedcount" =>
                    var dataStr = ""
                    var split = ""
                    SeedCounter.getSummary.foreach(x => {
                        dataStr += split + "%s=%d".format(x._1, x._2)
                        split = "|"
                    })
                    out.println(dataStr)
                case "resetworkcount" =>
                    WorkCounter.resetSummary()
                    out.println("done")
                case _ =>
                    println("Manager command is :" + message)
                    // Response message
                    out.println("OK")
            }

            out.close()
            in.close()
            client.close()
        } catch {
            case e: Exception =>
                log.error("Manage process exception :", e)
        }

        message
    }

    private def loadErrorMessage(){
        try {

            val f = new File("../conf/errormsg.properties")
            if(f.exists()){
                val confProps = new Properties()
                confProps.load(new FileInputStream(f))

                if(confProps.size() > 0) {
                    val keys = confProps.keys()
                    while (keys.hasMoreElements){
                        val key = keys.nextElement().toString
                        ErrorConstant.setErrorMessage(key, confProps.getProperty(key, ""))
                    }
                }
            }

        } catch {
            case e: Exception =>
                log.info("Throws exception when load custom error message:", e)
        }
    }
}
