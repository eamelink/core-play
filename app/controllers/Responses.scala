package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{ Enumerator, Input }

object Responses extends Controller {

  def simpleWithoutHeader() = Action {
    Ok("Hi! Play will calculate the Content-Length header for us!")
  }
  
  /*
   * Try this one out with "telnet localhost 9000" and then type
   * GET /responses/simple-feeding HTTP/1.0
   * and then two breaks. This works. Now try the same with HTTP/1.1
   */
  def simpleFeeding() = Action {
    val wordEnumerator = Enumerator("Sending data without Content-Length header doesn't work... with HTTP 1.1".split("(?=\\s)") : _*).andThen(Enumerator.eof)
    Ok.feed(wordEnumerator)
  }
  
  def chunkedResult() = Action {
    val wordEnumerator = Enumerator("Ben & Jerry's ", "New York Super Fudge ", "Chunks").andThen(Enumerator.eof)
    
    Ok.stream(wordEnumerator)
  }
  
  def imperativeChunked() = Action {
    val wordEnumerator = Enumerator.pushee[String]( onStart = pushee => {
      pushee.push("Ben & Jerry's ")
      Thread.sleep(1500)
      pushee.push("New York Super Fudge ")
      Thread.sleep(1500)
      pushee.push("Chunks")
      pushee.close()
    })

    Ok.stream(wordEnumerator)
  }
  
  def test() = Action {
    TODO
  }
}