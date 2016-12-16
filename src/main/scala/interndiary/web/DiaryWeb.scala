package interndiary.web

import interndiary.service.{Context, DiaryApp}
import org.scalatra._
import scala.util.control.Exception.allCatch
import interndiary.service.{
  FailedToEditEntryError,
  FailedToListDiaryError,
  FailedToDeleteError,
  FailedToGetCommentsError,
  UserNotFoundError,
  EntryNotFoundError,
  UnauthorizedError
}


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

  def toIntEither[T](intStr: String)(left: T): Either[T, Int] =
    (allCatch opt intStr.toInt).toRight(left)

  def toLongEither[T](longStr: String)(left: T): Either[T, Long] =
    (allCatch opt longStr.toLong).toRight(left)

  get("/") {
    Found(s"/user/${currentUserName}/")
  }

  get("/user/:userName/") {
    val app = createApp()

    (for {
      offset <- toIntEither(
        params.get("offset").getOrElse("0"))(
        BadRequest("Invalid Request.")
      ).right
      userName <- getEither("userName")(BadRequest("Invalid Request.")).right
      entries <- app.read(userName, offset).right
    } yield (userName, entries)) match {
      case Right((userName, component)) => {
        val authorized: Boolean = app.isAuthorName(userName)
        interndiary.html.read(userName, component, authorized)
      }
      case Left(UserNotFoundError) => NotFound(UserNotFoundError.toString)
      case Left(FailedToListDiaryError) => InternalServerError(FailedToListDiaryError.toString)
      case Left(error) => error
    }
  }

  get("/my/write") {
    interndiary.html.write(currentUserName)
  }

  post("/my/write") {
    val app = createApp()

    (for {
      rawTitle <- getEither("title")(BadRequest("Can't get Title.")).right
      title <- (rawTitle.trim match {
        case "" => Left(BadRequest("Empty title is invalid."))
        case _ => Right(rawTitle)
      }).right
      body <- getEither("body")(BadRequest("Can't get Body")).right
      entry <- app.write(title, body).right
    } yield entry) match {
      case Right(entry) => Found(s"/user/${currentUserName}/entry/${entry.id}")
      case Left(FailedToEditEntryError) => InternalServerError(FailedToEditEntryError.toString)
      case Left(error) => error
    }
  }

  get("/user/:userName/entry/:entryId") {
    val app = createApp()

    (for {
      userName <- getEither("userName")(BadRequest("Invalid request.")).right
      rawEntryId <- getEither("entryId")(BadRequest("Invalid request.")).right
      entryId <- toLongEither(rawEntryId)(BadRequest("Entry id must be Integer.")).right
      commentedEntry <- app.find(userName, entryId).right
    } yield (userName, commentedEntry)) match {
      case Right((userName, (entry, comments))) => {
        val authorized: Boolean = app.isAuthorName(userName)
        interndiary.html.find(entry, comments, authorized)
      }
      case Left(UserNotFoundError) => NotFound(UserNotFoundError.toString)
      case Left(EntryNotFoundError) => NotFound(EntryNotFoundError.toString)
      case Left(FailedToGetCommentsError) => InternalServerError(FailedToGetCommentsError.toString)
      case Left(error) => error
    }
  }

  get("/user/:userName/entry/:entryId/edit") {
    val app = createApp()

    (for {
      userName <- getEither("userName")(BadRequest("Invalid request.")).right
      rawEntryId <- getEither("entryId")(BadRequest("Invalid request.")).right
      entryId <- toLongEither(rawEntryId)(BadRequest("Entry id must be Integer.")).right
      commentedEntry <- app.find(userName, entryId).right
      _ <- Some(userName).filter(app.isAuthorName).toRight(UnauthorizedError).right
    } yield commentedEntry) match {
      case Right((entry, comments)) => interndiary.html.edit(entry)
      case Left(UserNotFoundError) => NotFound(UserNotFoundError.toString)
      case Left(EntryNotFoundError) => NotFound(EntryNotFoundError.toString)
      case Left(UnauthorizedError) => Forbidden(UnauthorizedError.toString)
      case Left(error) => error
    }
  }

  post("/user/:userName/entry/:entryId/edit") {
    val app = createApp()

    (for {
      userName <- getEither("userName")(BadRequest("Invalid request.")).right
      rawEntryId <- getEither("entryId")(BadRequest("Invalid request.")).right
      entryId <- toLongEither(rawEntryId)(BadRequest("Entry id must be Integer.")).right
      _ <- app.find(userName, entryId).right
      rawTitle <- getEither("title")(BadRequest("Invalid request.")).right
      title <- (rawTitle.trim match {
        case "" => Left(BadRequest("Empty title is invalid."))
        case _ => Right(rawTitle)
      }).right
      body <- getEither("body")(BadRequest()).right
      newEntry <- app.update(userName, entryId, title, body).right
    } yield newEntry) match {
      case Right(entry) => Found(s"/user/${currentUserName}/entry/${entry.id}")
      case Left(UserNotFoundError) => NotFound(UserNotFoundError.toString)
      case Left(EntryNotFoundError) => NotFound(EntryNotFoundError.toString)
      case Left(UnauthorizedError) => Forbidden(UnauthorizedError.toString)
      case Left(FailedToEditEntryError) => InternalServerError(FailedToEditEntryError.toString)
      case Left(error) => error
    }
  }

  get("/user/:userName/entry/:entryId/delete") {
    val app = createApp()

    (for {
      userName <- getEither("userName")(BadRequest("Invalid request.")).right
      rawEntryId <- getEither("entryId")(BadRequest("Invalid request.")).right
      entryId <- toLongEither(rawEntryId)(BadRequest("Entry id must be Integer.")).right
      commentedEntry <- app.find(userName, entryId).right
      _ <- Some(userName).filter(app.isAuthorName).toRight(UnauthorizedError).right
    } yield commentedEntry) match {
      case Right((entry, comments)) => interndiary.html.delete(entry)
      case Left(UserNotFoundError) => NotFound(UserNotFoundError.toString)
      case Left(EntryNotFoundError) => NotFound(EntryNotFoundError.toString)
      case Left(UnauthorizedError) => Forbidden(UnauthorizedError.toString)
      case Left(error) => error
    }
  }

  post("/user/:userName/entry/:entryId/delete") {
    val app = createApp()

    (for {
      userName <- getEither("userName")(BadRequest("Invalid request.")).right
      rawEntryId <- getEither("entryId")(BadRequest("Invalid request.")).right
      entryId <- toLongEither(rawEntryId)(BadRequest("Entry id must be Integer.")).right
      commentedEntry <- app.delete(userName, entryId).right
    } yield commentedEntry) match {
      case Right(entry) => Found("/")
      case Left(UserNotFoundError) => NotFound(UserNotFoundError.toString)
      case Left(EntryNotFoundError) => Found("/")
      case Left(UnauthorizedError) => Forbidden(UnauthorizedError.toString)
      case Left(error) => error
    }
  }
}
