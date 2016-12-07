package interndiary.web

import interndiary.service.{Context, DiaryApp}
import org.scalatra._
import scala.util.control.Exception.allCatch

class DiaryWeb extends DiaryWebStack {


  Context.setup("db.default")
  implicit val ctx = Context.createContext()
  //user is only siotouto, yet.
  def currentUserName(): String =
    "siotouto"

  def createApp(): DiaryApp =
    new DiaryApp(currentUserName())

  get("/") {
    Found("/diary")
  }

  get("/diary") {
    Found("/diary/user/my")
  }

  get("/diary/user/:user") {
    val app = createApp()
    val mine: Boolean = false
    if(Some(currentUserName) == params.get("user"))
      Found("/diary/user/my")
    else
      (for {
        rUserName <- params.get("user").toRight(BadRequest()).right
        rEntries <- app.read(rUserName).right
      } yield (rUserName, rEntries)) match {
        case Right((userName, entries)) => interndiary.html.read(userName, entries, mine)
        case Left(error) => error
      }
  }

  get("/diary/user/my") {
    val app = createApp()
    val mine: Boolean = true
      (for {
        rEntries <- app.read(currentUserName).right
      } yield rEntries) match {
      case Right(entries) => interndiary.html.read(currentUserName, entries, mine)
      case Left(error) => error
    }
  }

  get("/diary/write") {
    Found("/diary/user/my/write")
  }

  get("/diary/user/my/write") {
    interndiary.html.write(currentUserName)
  }

  post("/diary/user/my/write") {
    val app = createApp()
    (for {
      rawTitle <- params.get("title").toRight(BadRequest()).right
      title <- (rawTitle.trim match {
        case "" => Left(EmptyTitleEntryError)
        case _ => Right(rawTitle)
      }).right
      body <- params.get("body").toRight(BadRequest()).right
      rEntry <- app.write(title, body).right
    } yield rEntry) match {
      case Right(entry) => Found("/diary/user/my")
      case Left(error) => error
    }
  }

  get("/diary/entry/:id") {
    val app = createApp()
      (for {
        rawId <- params.get("id").toRight(BadRequest()).right
        id <- (allCatch either rawId.toLong).left.map(_ => BadRequest()).right
        rCommentedEntry <- app.find(id).right
      } yield rCommentedEntry) match {
      case Right((entry, comments)) => interndiary.html.find(entry, comments)
      case Left(error) => error
    }
  }

  get("/diary/entry/:id/edit") {
    val app = createApp()
      (for {
        //
        rawId <- params.get("id").toRight(BadRequest()).right
        id <- (allCatch either rawId.toLong).left.map(_ => BadRequest()).right
        rEntry <- app.find(id).right
      } yield rEntry) match {
      case Right((entry, comments)) => interndiary.html.edit(entry)
      case Left(error) => error
    }
  }

  post("/diary/entry/:id/edit") {
    val app = createApp()
      (for {
        //
        rawId <- params.get("id").toRight(BadRequest()).right
        id <- (allCatch either rawId.toLong).left.map(_ => BadRequest()).right
        rawTitle <- params.get("title").toRight(BadRequest()).right
        title <- (rawTitle.trim match {
          case "" => Left(EmptyTitleEntryError)
          case _ => Right(rawTitle)
        }).right
        body <- params.get("body").toRight(BadRequest()).right
        rEntry <- app.update(id, title, body).right
      } yield rEntry) match {
      case Right(entry) => Found("/diary")
      case Left(error) => error
    }
  }

  get("/diary/entry/:id/delete") {
    val app = createApp()
      (for {
        rawId <- params.get("id").toRight(BadRequest()).right
        id <- (allCatch either rawId.toLong).left.map(_ => BadRequest()).right
        rEntry <- app.find(id).right
      } yield rEntry) match {
      case Right((entry, comments)) => interndiary.html.delete(entry)
      case Left(error) => error
    }
  }

  post("/diary/entry/:id/delete") {
    val app = createApp()
      (for {
        rawId <- params.get("id").toRight(BadRequest()).right
        id <- (allCatch either rawId.toLong).left.map(_ => BadRequest()).right
        rEntry <- app.delete(id).right
      } yield rEntry) match {
      case Right(entry) => Found("/diary")
      case Left(error) => error
    }
  }
}
