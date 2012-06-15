package controllers

import play.api.mvc.{WebSocket, Controller}
import play.api.libs.iteratee.{Iteratee, Enumerator}
import akka.actor.{Props, ActorSystem, Actor}
import scala.collection.mutable
import play.api.libs.concurrent.Promise
import akka.util.duration._
import java.util
import management.ManagementFactory

/**
 *
 * Challenge #2: Clients subscribe to different topics (weather news and system load info) through websockets and receive updates every X seconds
 *
 * @author: Edi Weissmann
 */
object TopicSubscriberWebSockets extends Controller {

  /**
   * Actor messages
   */
  case class SubscribeTopic(topic:Topic, client:util.UUID)
  case class UnsubscribeTopic(topic:Topic, client:util.UUID)

  val subscribers = mutable.Map[util.UUID, mutable.Set[Topic]]()

  /**
   * Actor that manages clients and their subscriptions
   */
  class TopicActor extends Actor {
    def receive = {
      case SubscribeTopic(topic, client) => {
        subscribers.getOrElseUpdate(client, mutable.Set[Topic]()).add(topic)
      }
      case UnsubscribeTopic(topic, client) => {
        subscribers.getOrElseUpdate(client, mutable.Set[Topic]()).remove(topic)
      }
    }
  }

  /**
   * Topics - System load and Weather
   */
  trait Topic
  object SystemLoad extends Topic {
    val statsSystem = ManagementFactory.getOperatingSystemMXBean
    override def toString = "System load [%s]".format(statsSystem.getSystemLoadAverage)
  }
  object WeatherForecast extends Topic {
    override def toString = "Weather [sunny all day]"
  }
  object Topic {
    def apply(name:String):Option[Topic] = name match {
      case "weather" => Some(WeatherForecast)
      case "system" => Some(SystemLoad)
      case _ => None
    }
  }

  /**
   * Akka actor bootstrap
   */
  val system = ActorSystem("CpuAndOrSystemLoad")
  val topicActor = system.actorOf(Props[TopicActor], name = "topicActor")


  def topicSubscribers() = WebSocket.using[String] { request =>

    val client = util.UUID.randomUUID()

    /**
     * Prints topic updates to subscribed clients
     */
    val topicOut = Enumerator.fromCallback { () =>
      Promise.timeout({
        Some(subscribers.getOrElse(client, Set()).mkString(" "))
      }, 3 seconds)
    }

    /**
     * Prints usage options
     */
    val usageOut = Enumerator("Commands are: 'subscribe' and 'unsubscribe'.\nTopics are: 'weather' and 'system'\nType your command: subscribe weather")

    /**
     * Processes incoming commands
     */
    val in = Iteratee.foreach[String] { msg => {
      val Command = """(\w+) (\w+)""".r
      msg match {
        case Command(action, topicName) =>
          Topic(topicName) match {
            case Some(topic) =>
              action match {
                case "subscribe" => topicActor ! SubscribeTopic(topic, client)
                case "unsubscribe" => topicActor ! UnsubscribeTopic(topic, client)
                case _ => // unknown action
              }
            case _ => // unknown topic
          }
        case _ => // unknown, noop
      }
    }}


    (in, topicOut >- usageOut)
  }
}
