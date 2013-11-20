package edu.ufl.cise.actorlogger

import akka.actor.Actor
import akka.event.LoggingReceive

class PeerB extends Actor with Logging{
   
 //  def receive = LoggingReceive{
  def receive = {
    
    case messageA(msg)=> {
      //println(context.self.path.name+" received message "+"\""+msg+"\"")
      send(sender,messageAACK("Got it..."))   
    }
    
  }
}