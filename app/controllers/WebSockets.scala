package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{ Iteratee, Enumerator }
import play.api.libs.concurrent.Promise
import akka.util.duration._

import akka.actor.{ ActorSystem, Actor }

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
}

