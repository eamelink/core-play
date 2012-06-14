package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Promise

object ManualIteratee extends Controller {
	
  /**
   * Count action, just to demo the 'Counter' iteratee and the 'feed' method below
   */
  def count() = Action {
    val eventuallyResult = feed(counter[String], Seq("a", "b", "c", "d"))
    
    Async {
      eventuallyResult.map(number => Ok(number.toString))
    }
  }
  
  /**
   * Counter iteratee, that counts the elements that it consumes.
   * 
   * It starts in the "Cont" state
   */
  def counter[E]: Iteratee[E, Int] = {
    def step(s: Int)(i: Input[E]): Iteratee[E, Int] = i match {
      case Input.EOF => Done(s, Input.EOF)
      case Input.Empty => Cont[E, Int](i => step(s)(i))
      case Input.El(e) => Cont[E, Int](i => step(s + 1)(i))
    }

    Cont[E, Int](i => step(0)(i))
  }
  
  /**
   * Method to manually feed chunks from a sequence to the iterator.
   * 
   * It's pretty cumbersome to do this manually, so you should use an Enumerator for this :)
   */
  def feed[E, A](iteratee: Iteratee[E, Int], values: Seq[E]): Promise[Int] = {
    iteratee.fold(
      done = (result, remainingInput) => Promise.pure(result),
      cont = (consumeMethod) => {
        val newIteratee = consumeMethod(values.headOption.map(Input.El(_)).getOrElse(Input.EOF))
        feed(newIteratee, if(values.isEmpty) Nil else values.tail)
      },
      error = (error, remainingInput) => Promise.pure(-1))
  }
  
  
}
