package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.Iteratee

object BodyParsers extends Controller {

  /**
   * An iteratee the prints each chunk to the console
   */
  def printChunks: Iteratee[Array[Byte], Unit] = Iteratee.foreach[Array[Byte]](ba => println(new String(ba, "UTF-8")))

  /**
   * A body parser 
   */
  def loggingDiscardingBodyParser = BodyParser("logging-discarding-bodyparser")(_ => printChunks.map(Right(_)))

  /**
   * Try the following request (with 'telnet localhost 9000):
   *
   * POST /bodyparsers/logging-body-parser HTTP/1.1
   * Transfer-Encoding: chunked
   *
   * 5
   * Hello
   * F
   * RingRingRingRing
   * 0
   *
   */
  def loggingBodyParser() = Action(loggingDiscardingBodyParser) { request =>
    Ok("Done!")
  }

}