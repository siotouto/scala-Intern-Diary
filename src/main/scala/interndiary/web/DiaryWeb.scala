package interndiary.web

import interndiary.service.{Context, DiaryApp}
import org.scalatra._
import scala.util.control.Exception.allCatch
//import scala.util.Either.RightProjection

class DiaryWeb extends DiaryWebStack {


  Context.setup("db.default")
  implicit val ctx = Context.createContext()
  //user is only siotouto, yet.
  def currentUserName(): String =
    "siotouto"

  def createApp(): DiaryApp =
    new DiaryApp(currentUserName())

  def tryToGetEither(field: String): Either[Any, String] =
    params.get(field).toRight(BadRequest())

  def requestToLongEither(longStr: String): Either[Any, Long] =
    (allCatch opt longStr.toLong).toRight(BadRequest())

  get("/") {
    Found("/diary")
  }

  get("/diary") {
    Found("/diary/my")
  }

  get("/diary/user/:user") {
    val app = createApp()
    if(Right(currentUserName) == tryToGetEither("user"))
      Found("/diary/my")
    else{
      val mine: Boolean = false
      (for {
        rUserName <- tryToGetEither("user").right
        rEntries <- app.read(rUserName).right
      } yield (rUserName, rEntries)) match {
        case Right((userName, entries)) => interndiary.html.read(userName, entries, mine)
        case Left(error) => error
      }
    }
  }

  get("/diary/my") {
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
    Found("/diary/my/write")
  }

  get("/diary/my/write") {
    interndiary.html.write(currentUserName)
  }

  post("/diary/my/write") {
    val app = createApp()
    (for {
      rawTitle <- tryToGetEither("title").right
      title <- (rawTitle.trim match {
        case "" => Left(EmptyTitleEntryError)
        case _ => Right(rawTitle)
      }).right
      body <- tryToGetEither("body").right
      rEntry <- app.write(title, body).right
    } yield rEntry) match {
      case Right(entry) => Found("/diary/my")
      case Left(error) => error
    }
  }

  get("/diary/entry/:id") {
    val app = createApp()
      (for {
        rawId <- tryToGetEither("id").right
        id <- requestToLongEither(rawId).right
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
        rawId <- tryToGetEither("id").right
        id <- requestToLongEither(rawId).right
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
        rawId <- tryToGetEither("id").right
        id <- requestToLongEither(rawId).right
        rawTitle <- tryToGetEither("title").right
        title <- (rawTitle.trim match {
          case "" => Left(EmptyTitleEntryError)
          case _ => Right(rawTitle)
        }).right
        body <- tryToGetEither("body").right
        rEntry <- app.update(id, title, body).right
      } yield rEntry) match {
      case Right(entry) => Found("/diary")
      case Left(error) => error
    }
  }

  get("/diary/entry/:id/delete") {
    val app = createApp()
      (for {
        rawId <- tryToGetEither("id").right
        id <- requestToLongEither(rawId).right
        rEntry <- app.find(id).right
      } yield rEntry) match {
      case Right((entry, comments)) => interndiary.html.delete(entry)
      case Left(error) => error
    }
  }

  post("/diary/entry/:id/delete") {
    val app = createApp()
      (for {
        rawId <- tryToGetEither("id").right
        id <- requestToLongEither(rawId).right
        rEntry <- app.delete(id).right
      } yield rEntry) match {
      case Right(entry) => Found("/diary")
      case Left(error) => error
    }
  }
}
