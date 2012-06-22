package controllers;

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{ Iteratee, Enumerator }
import play.api.libs.concurrent.Promise
import akka.util.duration._
import akka.util._
import akka.actor.{ ActorSystem, Actor }
import play.api.libs.iteratee.Enumerator._
import akka.actor.Props
import akka.actor.Scheduler
import java.util.concurrent.atomic.AtomicInteger

object CpuInfoWebSocket extends Controller {

  val system = ActorSystem("MySystem")
  val cpuLoadActor = system.actorOf(Props[CpuInfoDistributionActor])
  val pusheeCounter = new AtomicInteger
  system.scheduler.schedule(0 seconds, 3 seconds, cpuLoadActor, DistributeMessage)

  /**
   * Websocket that retrieves a pushee and send it to
   * a Actor
   */
  def cpuLoadSender() = WebSocket.using[String] { request =>
    val in = Iteratee.ignore[String]
    val count = pusheeCounter.incrementAndGet()
    val cpuInfoEnumerator = Enumerator.pushee[String](
      onStart = pushee => cpuLoadActor ! AddMessage(count, pushee),
      onComplete = cpuLoadActor ! RemoveMessage(count),
      onError = (a, b) => println("error " + a + " " + b))
    (in, cpuInfoEnumerator)
  }
}

/**
 * Actor messages
 */
sealed trait CpuMessages
case class AddMessage(id: Int, pushee: Pushee[String]) extends CpuMessages
case class DistributeMessage() extends CpuMessages
case class RemoveMessage(id: Int) extends CpuMessages

/**
 * Distribution actor
 */
class CpuInfoDistributionActor extends Actor {
  var pushees: Map[Int, Pushee[String]] = Map.empty
  var counter: Int = 0

  def receive = {
    case AddMessage(id, pushee) => {
      println("Added pushee with id " + id)
      pushees = pushees + (id -> pushee)
    }
    case DistributeMessage => {
      if (!pushees.isEmpty) {
        println("Distribute cpu average: " + counter)
        counter += 1
      }
      pushees.foreach {
        case (id, p) => {
          println("Send to pushee: " + id)
          p.push(counter.toString)
        }
      }
    }
    case RemoveMessage(id) => {
      println("Remove pushee with id " + id)
      pushees = pushees - id

    }

  }

}
