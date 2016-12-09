package interndiary.repository

import interndiary.model.Entry
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.tarao.slickjdbc.interpolation.SQLInterpolation._
import com.github.tarao.slickjdbc.interpolation.CompoundParameter._
import slick.jdbc.GetResult
import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.LocalDateTime

import scala.util.control.Exception.allCatch

object Entries {
  private implicit val getEntryResult =
    GetResult(r => Entry(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  def findById(entryId: Long)(implicit
    ctx: Context
  ): Option[Entry] = run(sql"""
        SELECT * 
        FROM entry 
        WHERE id = ${entryId} 
        LIMIT 1
    """.as[Entry].map(_.headOption)
  )

  def listByUserId(userId: Long)(implicit
    ctx: Context
  ): Option[Seq[Entry]] = allCatch opt run(sql"""
        SELECT * 
        FROM entry 
        WHERE user_id = ${userId}
        ORDER BY created_at DESC
    """.as[Entry]
  )

  def create(userId: Long, title: String, body: String)(implicit
    ctx: Context
  ): Option[Entry] = {
    val id = Identifier.generate()
    val entry: Entry = Entry(id, userId, title, body, MyTime(), MyTime())
    (allCatch opt run(sqlu"""
          INSERT INTO entry
            (id, user_id, title, body, created_at, updated_at)
            VALUES
            (
              ${entry.id}, 
              ${entry.userId}, 
              ${entry.title}, 
              ${entry.body}, 
              ${entry.createdAt}, 
              ${entry.updatedAt}
            )
      """)).map(_ => entry)
  }

  def updateEntry(entryId: Long, title: String, body: String)(implicit
    ctx: Context
  ): Option[Entry] = {
    val updateTime: LocalDateTime = MyTime()
    allCatch opt {
      run(sqlu"""
          UPDATE entry
          SET 
            title = ${title},
            body = ${body},
            updated_at = ${updateTime}
          WHERE
            id = ${entryId}
      """)
      findById(entryId).get
    }
  }

  def deleteById(entryId: Long)(implicit
    ctx: Context
  ): Option[Unit] = allCatch opt {
    run(sqlu"""
      DELETE 
      FROM entry
      WHERE id = ${entryId}
    """
    )
    ()
  }
}
