package interndiary.repository

import interndiary.model.User
import scala.concurrent.ExecutionContext.Implicits.global
import org.joda.time.LocalDateTime
import com.github.tarao.slickjdbc.interpolation.SQLInterpolation._
import com.github.tarao.slickjdbc.interpolation.CompoundParameter._
import slick.jdbc.GetResult
import com.github.tototoshi.slick.MySQLJodaSupport._

object Users {
  private implicit val getUserResult =
    GetResult(r => User(r.<<, r.<<, r.<<, r.<<))

  def findByName(userName: String)(implicit ctx: Context)
      : Option[User] = {
    run(
      sql"""
        SELECT * 
        FROM user 
        WHERE name = ${userName} LIMIT 1
     """.as[User].map(_.headOption)
    )
  }

  def createByName(userName: String)(implicit ctx: Context)
      : User = {
    val id: Long = Identifier.generate()
    val user: User = User(id, userName, new LocalDateTime(), new LocalDateTime())
    run(
      sqlu"""
        INSERT INTO user
          (id, name, created_at, updated_at)
          VALUES
          (
            ${user.id}, 
            ${user.name}, 
            ${user.createdAt}, 
            ${user.updatedAt}
          )
     """)
    user
  }

  def findOrCreateByName(userName: String)(implicit ctx: Context)
      : User = {
    findByName(userName).getOrElse(createByName(userName))
  }
}
