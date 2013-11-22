/**
 * The following file traits Logging and MyLogging implementing the logging facilities for
 * an akka actor.
 */

package edu.ufl.cise.actorlogger

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import java.sql.Timestamp
import java.util.Date
import akka.actor.ActorRef
import java.io.BufferedWriter
import java.io.FileWriter
import akka.actor.Cancellable
import akka.actor.Scheduler
import scala.concurrent.ExecutionContext
import akka.actor.Cancellable

object application extends App{
  
  val actorSystem = ActorSystem("TheSystem")
  val peerA = actorSystem.actorOf(Props(new PeerA with MyLogging), "PeerA")

}





