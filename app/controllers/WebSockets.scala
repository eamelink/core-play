package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{ Iteratee, Enumerator }
import play.api.libs.concurrent.Promise
import akka.util.duration._
import akka.actor.{ ActorSystem, Actor, Props }
import play.api.libs.iteratee.Enumerator.Pushee
import java.lang.management.{ ManagementFactory, OperatingSystemMXBean }

object WebSockets extends Controller {

  def index() = Action {
    Ok(views.html.websockets.index())
  }

  // A logging websocket action
  def logging() = WebSocket.using[String] { request =>
    println("Connected")
    val in = Iteratee.foreach[String] { msg =>
      println(msg)
    }.mapDone { _ =>
      println("Disconnected")
    }

    val out = Enumerator[String]()

    (in, out)
  }

  // An echoing websocket action
  def echo() = WebSocket.using[String] { request =>

    val out = Enumerator.imperative[String]()
    val in = Iteratee.foreach[String] { msg => out.push(msg) }

    (in, out)
  }

  // A websocket action that counts
  def counter() = WebSocket.using[String] { request =>
    var i = 0;
    val out = Enumerator.fromCallback { () =>
      Promise.timeout({ i += 1; Some(i.toString) }, 1 seconds)
    }

    val in = Iteratee.ignore[String]

    (in, out)
  }

  // A websocket action that combines echoing and counting using enumerator composition
  def echoAndCounter() = WebSocket.using[String] { request =>
    var i = 0;
    val counter = Enumerator.fromCallback { () =>
      Promise.timeout({ i += 1; Some(i.toString) }, 1 seconds)
    }

    val echoer = Enumerator.imperative[String]()
    val in = Iteratee.foreach[String] { msg => echoer.push(msg) }

    (in, echoer >- counter)
  }

  case class Connect(pushee: Pushee[String])
  case class Disconnect(pushee: Pushee[String])
  case class Announce(data: String)

  val system = ActorSystem("SystemData")
  
  val myActor = system.actorOf(Props(new Actor {
    var clients: List[Pushee[String]] = Nil

    def receive = {
      case Connect(client) => clients = client :: clients
      case Disconnect(client) => clients = clients.filterNot(_ == client)
      case Announce(data) => clients.foreach(_.push(data))
    }
  }), name = "myactor")

  val statsSystem = ManagementFactory.getOperatingSystemMXBean
  
  val schedule = system.scheduler.schedule(0 seconds, 3 seconds){
    myActor ! Announce(statsSystem.getSystemLoadAverage().toString)
  }
  
  def cpuLoad() = WebSocket.using[String] { request =>
    val out = Enumerator.pushee(
      onStart = (pushee: Pushee[String]) => {
        pushee.push("Welcome!")
        myActor ! Connect(pushee)
      },
      onComplete = (pushee: Pushee[String]) => {
        myActor ! Disconnect(pushee)
      })
    val in = Iteratee.ignore[String]
    
    (in, out)
  }
}

