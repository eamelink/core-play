package sample

import anorm.SqlParser.flatten
import anorm.SqlParser.str
import anorm.SQL
import anorm.sqlToSimple
import anorm.toParameterValue
import play.api.Play.current
import play.api.db.DB
import play.api.mvc.Controller

object AnormSample {

  case class SpokenLanguages(country: String, languages: Seq[String])

  def spokenLanguages(countryCode: String): Option[SpokenLanguages] = DB.withConnection { implicit conn =>
    val languages: List[(String, String)] = SQL(
      """
      select c.name, l.language from Country c 
      join CountryLanguage l on l.CountryCode = c.Code 
      where c.code = {code};
    """)
      .on("code" -> countryCode)
      .as(str("name") ~ str("language") map (flatten) *)

    languages.headOption.map { f =>
      SpokenLanguages(f._1, languages.map(_._2))
    }
  }
}

