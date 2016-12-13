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

  def getEither[T](field: String)(left: T): Either[T, String] =
    params.get(field).toRight(left)

  def toLongEither[T](longStr: String)(left: T): Either[T, Long] =
    (allCatch opt longStr.toLong).toRight(left)

  get("/") {
    Found(s"/user/${currentUserName}/")
  }

  get("/user/:userName/") {
    val app = createApp()

    (for {
      userName <- getEither("userName")(BadRequest()).right
      entries <- app.read(userName).right
    } yield (userName, entries)) match {
      case Right((userName, entries)) => {
        val authorized: Boolean = app.isAuthorName(userName)
        interndiary.html.read(userName, entries, authorized)
      }
      case Left(error) => error
    }
  }

  get("/my/write") {
    interndiary.html.write(currentUserName)
  }

  post("/my/write") {
    val app = createApp()

    (for {
      rawTitle <- getEither("title")(BadRequest()).right
      title <- (rawTitle.trim match {
        case "" => Left(EmptyTitleEntryError)
        case _ => Right(rawTitle)
      }).right
      body <- getEither("body")(BadRequest()).right
      entry <- app.write(title, body).right
    } yield entry) match {
      case Right(entry) => Found("/")
      case Left(error) => error
    }
  }

  get("/user/:userName/entry/:entryId/") {
    val app = createApp()

    (for {
      userName <- getEither("userName")(BadRequest()).right
      rawEntryId <- getEither("entryId")(BadRequest()).right
      entryId <- toLongEither(rawEntryId)(BadRequest()).right
      commentedEntry <- app.find(userName, entryId).right
    } yield commentedEntry) match {
      case Right((entry, comments)) => interndiary.html.find(entry, comments)
      case Left(error) => error
    }
  }

  get("/user/:userName/entry/:entryId/edit") {
    val app = createApp()

    (for {
      userName <- getEither("userName")(BadRequest()).right
      rawEntryId <- getEither("entryId")(BadRequest()).right
      entryId <- toLongEither(rawEntryId)(BadRequest()).right
      commentedEntry <- app.find(userName, entryId).right
    } yield commentedEntry) match {
      case Right((entry, comments)) => interndiary.html.edit(entry)
      case Left(error) => error
    }
  }

  post("/user/:userName/entry/:entryId/edit") {
    val app = createApp()

    (for {
      userName <- getEither("userName")(BadRequest()).right
      rawEntryId <- getEither("entryId")(BadRequest()).right
      entryId <- toLongEither(rawEntryId)(BadRequest()).right
      _ <- app.find(userName, entryId).right
      rawTitle <- getEither("title")(BadRequest()).right
      title <- (rawTitle.trim match {
        case "" => Left(EmptyTitleEntryError)
        case _ => Right(rawTitle)
      }).right
      body <- getEither("body")(BadRequest()).right
      newEntry <- app.update(userName, entryId, title, body).right
    } yield newEntry) match {
      case Right(entry) => Found("./")
      case Left(error) => error
    }
  }

  get("/user/:userName/entry/:entryId/delete") {
    val app = createApp()

    (for {
      userName <- getEither("userName")(BadRequest()).right
      rawEntryId <- getEither("entryId")(BadRequest()).right
      entryId <- toLongEither(rawEntryId)(BadRequest()).right
      commentedEntry <- app.find(userName, entryId).right
    } yield commentedEntry) match {
      case Right((entry, comments)) => interndiary.html.delete(entry)
      case Left(error) => error
    }
  }

  post("/user/:userName/entry/:entryId/delete") {
    val app = createApp()

    (for {
      userName <- getEither("userName")(BadRequest()).right
      rawEntryId <- getEither("entryId")(BadRequest()).right
      entryId <- toLongEither(rawEntryId)(BadRequest()).right
      commentedEntry <- app.delete(userName, entryId).right
    } yield commentedEntry) match {
      case Right(entry) => Found("/")
      case Left(error) => error
    }
  }
}
