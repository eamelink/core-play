package sample

import play.api._
import play.api.mvc._
import play.api.http.{ Writeable, ContentTypeOf }
import play.api.libs.iteratee.Enumerator

object Results extends Controller {
  // Creating a result with status code 200 and no body
  Status(200)

  // Shortcut for the above
  Ok

  // Adding a non-chunked body
  Ok("Hello DUSE!")

  // The above is similar to 
  Ok("Hello DUSE!")(Writeable.wString(Codec.utf_8), ContentTypeOf.contentTypeOf_String(Codec.utf_8))

  // Adding a chunked body
  val bodyChunks = Enumerator[String]("Hello ", "DUSE", "!").andThen(Enumerator.eof)
  Ok.stream(bodyChunks)
}