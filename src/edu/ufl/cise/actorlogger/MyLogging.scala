package edu.ufl.cise.actorlogger

import java.io.BufferedWriter
import java.io.FileWriter
import java.sql.Timestamp
import akka.actor.Actor

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
   * #moved file io to logging
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

