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
        val Supervisor = system.actorOf(Props(new Supervisor(args) with MyLogging),"Supervisor")
    }
}


class Supervisor(val args:Array[String]) extends Actor with Logging{
 
  val numOfWorkers = 200
  val end = args(0).toLong
  var jump = 100.toLong
  val packet = ((end.toDouble) / jump).ceil.toInt
  var size1 = args(1).toLong
  var noofresult = 0 
  
  
   val Worker = context.actorOf(Props(new Worker with MyLogging).withRouter
      (RoundRobinRouter(nrOfInstances = numOfWorkers)),"worker") 
  
   val startTime = System.currentTimeMillis()
   
   for(a<- 1 until packet){                                        //Send workers the range they need 
    val rng = getRange(a,jump,size1)
    val start = rng(0)
    val end = rng(1)
    val size = rng(2)
    send(Worker,range(start,end,size))                                 // to calculate
   }
  
  
  
  send(Worker,range(1+((packet-1)*jump),end,size1))      // The last worker is an exception
   

  def getRange(actor_no:Int,jump_size:Long,size:Long):List[Long] ={        // Calculates range for a worker
                                                                        // Using its index in the array
    List( 1+(actor_no-1)*jump_size, actor_no*jump_size, size)   
  
  }
  
  
 def receive ={                                                        //Supervisor receive method
   
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





class Worker extends Actor with Logging{


  var diff:Long = 0                            //Intialise variables needed
  var sqroot:Double = 0

  
  
  def sumnsq(n:Long):Long = {
  
    (n*(n+1)*(2*n+1))/6                              //Calculates the sum of first n square multiplied by six
  
  }
  

  def receive ={
  
    case range(start1,end1,size1)=>{ 

      var solution = Array[Long]()
     
      var start = start1
      var end = end1
      var size = size1
      

       while(start<=end)   {                                 //Loops through the range to find a sequence
        
        diff = sumnsq(start+size-1) - sumnsq(start-1)      // It used the mathematical formula
        sqroot = math.sqrt(diff)                                         
        if(sqroot.isValidInt){                                           //The square root is checked to be a valid 
          solution = solution :+ start                                        // integer and if so is sent to the supervisor
        }
        start = start + 1
     }
      


      send(sender,finalsolution(solution))

      }
    
   } 
  
    
}

