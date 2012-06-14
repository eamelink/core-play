package controllers

import play.api._
import play.api.mvc._
import models.User

object Templates extends Controller {
  def index() = Action {
    val users = List(
      User("Erik", 27)
    )
    Ok(views.html.userlist(users))
  }
}