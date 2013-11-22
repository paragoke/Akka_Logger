package edu.ufl.cise.actorlogger

import java.io.BufferedWriter
import java.io.FileWriter
import java.sql.Timestamp
import akka.actor.Actor

/*
 * MyLogging trait partially implements the receive method of the user actor. It uses the abstract override modifier.
 * This trait also manages the file I/O dumping the log to a file named after the actor.
 */


trait MyLogging extends Actor with Logging{
  
  import scala.concurrent.duration._
  import java.io.File
  import context._
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
   * Reset file. Comment the following code if you wish to want persistent logs over many executions.
   */
 	
  resetFile
  def resetFile ={
    
    val fw:FileWriter  = new FileWriter(file.getAbsoluteFile())
    val bw:BufferedWriter  = new BufferedWriter(fw)
    bw.write("");
	bw.close();
    
  }
  /*
   * The following block is an incomplete implementation of a scheduler. The scheduler is started at the actor start and
   * after the given interval dumps the log to the given file using the startlogging method.
   * BUG: Duplicate entries are found. Something to do with the buffered writer in the startlogging method
   */
  
  /*var schedulor:Cancellable = _
  
  override def preStart(): Unit = {
   schedulor = context.system.scheduler.schedule(30 milliseconds, 100 milliseconds){
      startlogging
   	}.asInstanceOf[Cancellable]
  }*/
  
  
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
       * Append to log
       * lamport clock,timestamp,IN(denoting received message),current actor, message,receiver in that sequence.
       * 
       */
      log = log+lmc+"\t"+timest+"\t"+"IN"+"\t"+context.self.path.name+"\t"+msg+"\t"+sender.path.name+"\n"
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
   * We need to dump the log at the termination of the actor. Also the scheduler has to be stopped if used.
   */
  override def postStop(): Unit = {
	
    //schedulor.cancel
    startlogging
 
  }
  /*
   * The follwing method uses java I/O to dummp the log to a file and reset the log.
   */
  def startlogging ={
    
    val fw:FileWriter  = new FileWriter(file.getAbsoluteFile(),true)
    val bw:BufferedWriter  = new BufferedWriter(fw)
    
	//var nlog = "**LOG***\n"+log+"\n***LOG***"
	//println(nlog)
	bw.write(log);
	bw.close();
	log = ""
    
  }
    
}

