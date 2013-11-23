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
   */
  val file:File = new File(context.self.path.name)
  /*
   * Create new file if not present.
   */
  if (!file.exists()){
      file.createNewFile();
      }
  /*
   * Reset the file by performing a dummy write
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


/*
 * MyLogging trait partially implements the receive method of the user actor. It acts as a wrapper around the user defined
 * receive to log info. 
 * It uses the abstract override modifier.
 * NOTE: This class has to extended by the user class object.
 */


trait MyLogging extends Actor with Logging{
  
  import scala.concurrent.duration._
  import java.io.File
  import context._
  
  /*
   * Partial implementation of the actor receive method. The logging info is recorded, the lamport clock is seperated from the msg and then the
   * message is forwarded to actor using the super method.
   */
  
  abstract override def receive = {
    
    
  	case (msg:Any,lmcs:Int) =>{
  	  /*
  	   * Set the lamport clock.
  	   */
  	  if(lmc<lmcs+1){
  	    lmc = lmcs + 1 
  	  }
  	  /*
  	   * Generate timestamp.
  	   */
  	  var timest = new Timestamp(System.currentTimeMillis())
      timest.setNanos((now%1000000000).toInt)
      /*
       * Write out to the buffered writer.
       * lamport clock,timestamp,IN(denoting received message),current actor, message,receiver in that sequence.
       * 
       */
      bw.write(log+lmc+"\t"+timest+"\t"+"IN"+"\t"+context.self.path.name+"\t"+msg+"\t"+sender.path.name+"\n")
      bw.flush()
      /*
       * increment lamport clock.
       */
      lmc = lmc + 1
      /*
       * forward the message to the actor using super.In the implementation using stackable traits , the super of a trait is the 
       * class that is extending it. In this case it is the object that we create above using the 'with' clause.  
       */
      
      super.receive(msg)
    }
  
  }
  
 
  /*
   * We need to close the buffered writer.
   */
  override def postStop(): Unit = {
	
    bw.close()
 
  }
    
}


