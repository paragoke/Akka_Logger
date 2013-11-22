package edu.ufl.cise.actorlogger

import akka.actor.Actor
import akka.actor.Props
import akka.event.LoggingReceive
import akka.actor.PoisonPill

case class messageA(msg:String)
case class messageAACK(msg:String)

class PeerA extends Actor with Logging{
  
  val peerB = context.actorOf(Props(new PeerB with MyLogging),"PeerB")
  var counter = 0
  
  send(peerB,messageA("Hi"))
  send(peerB,messageA("Hi"))
  send(peerB,messageA("Hi"))
  send(peerB,messageA("Hi"))
  send(peerB,messageA("Hi"))
  send(peerB,messageA("Hi"))
  send(peerB,messageA("Hi"))
  send(peerB,messageA("Hi"))
  send(peerB,messageA("Hi"))
  send(peerB,messageA("Hi"))
  
 // def receive = LoggingReceive{
  def receive = {
    
    case messageAACK(msg)=>{    
      //println(context.self.path.name+" received ACK with message "+"\""+msg+"\"")
      counter = counter + 1
      if(counter==10){
        context.system.shutdown
      }
    }
    
  }

}