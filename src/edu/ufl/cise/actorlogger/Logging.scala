package edu.ufl.cise.actorlogger

import java.sql.Timestamp
import akka.actor.Actor
import akka.actor.ActorRef

/*
 * Logging trait , extended by the user class as well as the MyLogging trait. It forms the base trait upon which the user class 
 * and the MyLogging trait are stacked.
 */

trait Logging extends Actor{
  /*
   * The logs are currently stored in a string which is reset after every I/O activity
   */ 
  var log = ""
  /*
   *  A lamport clock implemented for message ordering.
   */  
  var lmc = 0 
 
  /*
   * Now function returning the current system time in nano seconds.
   */
  def now = System.nanoTime()
  /*
   * The send method form a wrappera around the akka tell or ! method managing the logging and the lamport
   * clock implementation. It encodes the clock in the message similar to a tcp/ip header.The user has to use
   * this wrapper function instead of the ! operator.
   */
  def send(receiver:ActorRef,msg:Any) ={

    var timest = new Timestamp(System.currentTimeMillis())
  	timest.setNanos((now%1000000000).toInt)
  	/*
  	 * Append info to log.
  	 * lamport clock,timestamp,OUT(denoting sent message),current actor, message,receiver in that sequence.
  	 */
    log = log+lmc+"\t"+timest+"\t"+"OUT"+"\t"+context.self.path.name+"\t"+msg+"\t"+receiver.path.name+"\n"
    
    /*
     *  Send a tuple of the user message and the clock. This can be changed to include any information needed.
     */  
    receiver ! (msg,lmc)
    /*
     * Increment the lamport clock.
     */
    lmc = lmc + 1
    
  }
  
}
