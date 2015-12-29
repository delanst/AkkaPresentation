package demo

import akka.actor._

/**
 * Demonstrates the sending back of a result to the actor which has sent the message.
 */

case class Variable(num1 : Int,num2 : Int)
case class Result(result : Int)

/**
 * OpActor gets a Variable message that passes it to the CalculateActor. This actor can also receive
 * a Result message back and will print the result.
 */

class OpActor extends Actor {

  val calcActor = context.actorOf(Props[CalculateActor], name = "calculator")

  override def receive: Actor.Receive = {
    case Variable(num1,num2) =>
      println(num1 + " " + num2)
      calcActor ! Variable(num1,num2)
    case Result(result) =>
      println("result is = " + result )
    case _ =>
      println("Unknown message")
  }
}

/**
 * Actor performing the actual calculation and sends the result back incapsulated in a Result message.
 * This is done by sender() ! Result(num1 + num2)
 */

class CalculateActor extends Actor {

  override def receive: Receive = {
    case Variable(num1,num2) =>
      println("Send back result")
      sender() ! Result(num1 + num2)
    case _ => println("Invalid message received")
  }
}

object SenderDemoApp extends App {

  val system = ActorSystem("SenderDemoApp")

  val opActor = system.actorOf(Props[OpActor], name = "operation")

  opActor ! Variable(5,10)

  system.registerOnTermination(system)
}



