package controllers

import play.api._
import play.api.mvc._

import play.api.libs.ws.WS
import play.api.libs.iteratee.{ Iteratee, Enumerator }
import play.api.libs.oauth.{ OAuthCalculator, ConsumerKey, RequestToken }

import org.apache.commons.codec.binary.Base64

object Twitter extends Controller {
  val username = ""
  val password = ""
    
  def stream(keywords: String) = WebSocket.using[String] { request =>
    
    val out: Enumerator[String] = Enumerator.pushee[String](onStart = pushee => {
      
      /**
       * Using this foreach iteratee is not ideal; since there is no
       * way to make it go into 'Done' state, which is required to make
       * the Twitter stream stop.
       * 
       * But because of a bug in the Play 2.0.1 WS API, it does not listen
       * to a Done state at all... So currently the only way to stop the 
       * Twitter stream is by killing your app...
       */
	  def twitterIteratee = Iteratee.foreach[Array[Byte]]{ ba => 
	    val msg = new String(ba, "UTF-8")
	    pushee.push(msg)
	    println(msg)
	  }
	    
	  WS.url("https://stream.twitter.com/1/statuses/filter.json").
	    withHeaders(
	      "Authorization" -> ("Basic " + new String(Base64.encodeBase64((username + ":" + password).getBytes))),
	      "Content-Type" -> "application/x-www-form-urlencoded").
	      postAndRetrieveStream("track=" + keywords)(headers => twitterIteratee)
    })
      
    val in = Iteratee.ignore[String]
    
    (in, out)
  }
  
  
}