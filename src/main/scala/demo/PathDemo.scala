package demo

import akka.actor.{Actor, ActorSystem, Props}
import scala.concurrent.duration._

/**
 * Demonstration of how the paths of actors are constructed. Each actor prints the following
 * - The path of the actor that received the message
 * - The message itself
 *
 * After this information is printed out following actors send a new message to the next actor
 * - A1Actor sends a A2Message to A2Actor
 * - A2Actor sends a A4Message to A4Actor
 * - A4Actor sends a A3Message to A3Actor
 */

case class A1Message(message: String)
case class A2Message(message: String)
case class A3Message(message: String)
case class A4Message(message: String)
case class VoidMessage(message: String)

/**
 * Four actor have been defined in which each actor prints it's path and corresponding message. Then each actor
 * can sent a message to another actor. Here is how the messages are sent between the actors.
 * - A1Actor sends A2Message to A2Actor
 * - A2Actor sends A4Message to A4Actor
 * - A4Actor sends A3Message to A3Actor
 *
 * The A3Actor does not communicate with other actors.
 */

class A1Actor extends Actor {
  val a2Actor = context.actorOf(Props[A2Actor],"A2")

  override def receive: Receive = {
    case A1Message(message) =>
      println("A1Message received on " + self.path)
      println(message)
      directToA2(message)
    case _ => println("Received unexpected message for A1Actor")
  }

  def directToA2(message: String) = {
    a2Actor ! A2Message(message)
  }
}

class A2Actor extends Actor {
  val a4Actor = context.actorOf(Props[A4Actor],"A4")

  override def receive: Actor.Receive = {
    case A2Message(message) =>
      println("A2Message received on " + self.path)
      println(message)
      directToA4(message)
    case _ => println("Received unexpected message for A2Actor")
  }

  def directToA4(message: String): Unit = {
    a4Actor ! A4Message(message)
  }

}

class A3Actor extends Actor {
  override def receive: Actor.Receive = {
    case A3Message(message) =>
      println("A3Message received on " + self.path)
      println(message)
    case _ => println("Received unexpected message for A3Actor")
  }
}

class A4Actor extends Actor {
  val a3Actor = context.actorOf(Props[A3Actor],"A3")

  override def receive: Actor.Receive = {
    case A4Message(message) =>
      println("A4Message received on " + self.path)
      println(message)
      direcToA3(message)
    case _ => println("Received unexpected message for A4Actor")
  }

  def direcToA3(message : String): Unit = {
    a3Actor ! A3Message(message)
  }
}

/**
 * Instantiates the ScheduleInstructor and sent a tick message to the ScheduleInstructor actor.
 */
object PathDemoApp extends App {

  val system = ActorSystem("PathDemo")

  val scheduleAct = system.actorOf(Props[ScheduleInstructor],"Schedule")

  scheduleAct ! "tick"

}

/**
 * The schedule instructor is an actor that sends a message to an A1Actor instance every second. The message is
 * sent by the context.system.scheduler.schedule to itself with the message "tick".
 * If the counter cnt % 2 == 0 then a A1Message is sent to the A1Actor otherwise a VoidMessage is sent.
 *
 */
class ScheduleInstructor extends Actor {
  val a1Actor = context.actorOf(Props[A1Actor],"A1")

  import context.dispatcher
  val tick = context.system.scheduler.schedule(500 millis,1000 millis,self,"tick")

  var cnt = 0

  override def postStop() = tick.cancel()

  override def receive: Actor.Receive = {
    case "tick" =>
      if(cnt % 2 == 0)
        a1Actor ! A1Message("This is a valid message")
      else
        a1Actor ! VoidMessage("Void message")
      cnt=cnt + 1
    case _ => println("Some really went wrong")
  }
}