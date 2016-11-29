package interndiary.repository

import interndiary.model.Comment
import com.github.tarao.slickjdbc.interpolation.SQLInterpolation._
import com.github.tarao.slickjdbc.interpolation.CompoundParameter._
//import slick.jdbc.GetResult
import com.github.tototoshi.slick.MySQLJodaSupport._

import scala.util.control.Exception.allCatch

object Comments {
  /*
  private implicit val getCommentResult =
    GetResult(r => Comment(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))
   */
  def create(entryId: Long, commenterId: Long, body: String)
    (implicit ctx: Context)
      : Option[Comment] = {
    val id = Identifier.generate()
    val comment =
      Comment(
        id,
        entryId,
        commenterId,
        body,
        MyTime(),
        MyTime()
      )
    for {
      _ <- allCatch opt run(
        sqlu"""
          INSERT INTO comment
            (id, entry_id, commenter_id, body, created_at, updated_at)
            VALUES
            (
              ${comment.id},
              ${comment.entryId},
              ${comment.commenterId},
              ${comment.body},
              ${comment.createdAt},
              ${comment.updatedAt}
            )
       """)
    } yield comment
  }
}
