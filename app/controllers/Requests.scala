package controllers

import play.api._
import play.api.mvc._

object Requests extends Controller {

  // Default body parser
  def actionOne() = Action { request =>
    Ok("Hello world!")
  }
  
  // Explicit body parser
  def actionTwo() = Action(parse.json) { request =>
    // We now have request.body of type JsValue
    val json = request.body
    Ok 
  }
  
  // Custom body parser of type BodyParser[Unit]
  def actionThree() = Action(BodyParsers.loggingDiscardingBodyParser) { request =>
    Ok
  }
}