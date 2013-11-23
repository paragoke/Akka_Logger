package edu.ufl.cise.actorlogger

import java.sql.Timestamp
import akka.actor.Actor
import akka.actor.ActorRef
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.File

/*
 * Logging trait , extended by the user class as well as the MyLogging trait. It forms the base trait upon which the user class 
 * and the MyLogging trait are stacked.
 * NOTE: This trait has to be mixed in with the actor class by the class with extends actor.
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
   * Create a log file named after the actor name.
   * #moved from mylogging
   */
  val file:File = new File(context.self.path.name)
  /*
   * Create new file if not present.
   * #moved from mylogging
   */
  if (!file.exists()){
      file.createNewFile();
      }
  /*
   * Reset the file by performing a dummy write
   * #moved from mylogging
   */
  resetFile
  def resetFile ={
    
    val fw:FileWriter  = new FileWriter(file.getAbsoluteFile())
    val bw:BufferedWriter  = new BufferedWriter(fw)
    bw.write("");
	bw.close();
    
  }
  /*
   * Create a buffered writer for the log file
   * #moved from mylogging
   */
  val fw:FileWriter  = new FileWriter(file.getAbsoluteFile(),true)
  val bw:BufferedWriter  = new BufferedWriter(fw)
 
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
  	 * Write out the log info to the buffered writer
  	 * lamport clock,timestamp,OUT(denoting sent message),current actor, message,receiver in that sequence.
  	 * #modified to write out to the buffered writer directly
  	 * #increased performance 300%
  	 */
    bw.write(log+lmc+"\t"+timest+"\t"+"OUT"+"\t"+context.self.path.name+"\t"+msg+"\t"+receiver.path.name+"\n")
    bw.flush()
    
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
