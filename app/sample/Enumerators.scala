package sample

import play.api.libs.concurrent.Promise
import play.api.libs.iteratee.Enumerator
import akka.util.duration.intToDurationInt

object Enumerators {
    val file = null
    
    // From a set of values
    val e1 = Enumerator("Hello! ", "these", "are", "the", "elements")
    
    // From a file
    val e2 = Enumerator.fromFile(file)
       
    // An imperative 'PushEnumerator'
    val e3 = Enumerator.imperative[String]()
    e3.push("Foo")
    e3.push("Bar")
    
    // From a callback
    val e4 = Enumerator.fromCallback { () =>
      Promise.timeout(Some("Hello!"), 1 seconds)
    }
    
    // Interleave
    val e5 = e1 interleave e3
    
    // Concatenate
    val e6 = e1 andThen e3

    // Transform chunks
    val e7 = e1.map(_.toInt)
  }
  
