/**
 * This is the first DOS assignment modified to demonstrate the usage of our logging capabilities.
 * The logged files are generated on the classpath to keep them compatible.The files are named after
 * their respective actor names. Comments have been added to where the looging capabilites are added.
 */

package edu.ufl.cise.actorlogger

import scala.Array.canBuildFrom
import akka.actor.Actor
import akka.actor.Props
import akka.routing.RoundRobinRouter
 
case class finalsolution(result:Array[Long])
case class range(start:Long,end:Long,size:Long)

object Supervisor {
    def main(args: Array[String]) {
        val system = akka.actor.ActorSystem()
   /*
   * Every actor object has to be initialized using the MyLogging trait.
   */
        val Supervisor = system.actorOf(Props(new Supervisor(args) with MyLogging),"Supervisor")
    }
}

/*
 * A user class has to extend the logging trait.
 */
class Supervisor(val args:Array[String]) extends Actor with Logging{  
 
  val numOfWorkers = 200
  val end = 1000000.toLong
  var jump = 100.toLong
  val packet = ((end.toDouble) / jump).ceil.toInt
  var size1 = 24.toLong
  var noofresult = 0 
  
  /*
   * Every actor object has to be initialized using the MyLogging trait.
   */
  
   val Worker = context.actorOf(Props(new Worker with MyLogging).withRouter
      (RoundRobinRouter(nrOfInstances = numOfWorkers)),"worker") 
  
   val startTime = System.currentTimeMillis()
   
   for(a<- 1 until packet){                                   
    val rng = getRange(a,jump,size1)
    val start = rng(0)
    val end = rng(1)
    val size = rng(2)
    send(Worker,range(start,end,size))                                 
   }
  
  
  /*
   * send is a wrapper method around the ! method of the actor.
   */
  send(Worker,range(1+((packet-1)*jump),end,size1))      
   

  def getRange(actor_no:Int,jump_size:Long,size:Long):List[Long] ={        
                                                                        
    List( 1+(actor_no-1)*jump_size, actor_no*jump_size, size)   
  
  }
  
  
 def receive ={                                                        
   
   case finalsolution(result) =>
     
     for(a <- result)
      println(a)

     noofresult = noofresult + 1
   
     
     if(noofresult == packet)  {
       context.system.shutdown()
       println("Time:"+(startTime-System.currentTimeMillis()))
     }
      
 }


}


/*
 * A user class has to extend the logging trait.
 */


class Worker extends Actor with Logging{


  var diff:Long = 0                            
  var sqroot:Double = 0

  
  
  def sumnsq(n:Long):Long = {
  
    (n*(n+1)*(2*n+1))/6                           
  
  }
  

  def receive ={
  
    case range(start1,end1,size1)=>{ 

      var solution = Array[Long]()
     
      var start = start1
      var end = end1
      var size = size1
      

       while(start<=end)   {                                
        
        diff = sumnsq(start+size-1) - sumnsq(start-1)     
        sqroot = math.sqrt(diff)                                         
        if(sqroot.isValidInt){                             
          solution = solution :+ start                     
        }
        start = start + 1
     }
      


   /*
   * send is a wrapper method around the ! method of the actor.
   */
      send(sender,finalsolution(solution))

      }
    
   } 
  
    
}

