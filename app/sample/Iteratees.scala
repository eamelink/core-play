package sample

import play.api.libs.iteratee.Iteratee

object Iteratees {

  // Consume and concatenate input
  val i1 = Iteratee.consume[String]

  // Folding iteratee, this one sums the chunks
  val i2 = Iteratee.fold(0)((state, chunk: Int) => state + chunk)

  // Apply method to each chunk
  val i3 = Iteratee.foreach((chunk: Any) => println(chunk))

  // Ignore input...
  val i4 = Iteratee.ignore
}