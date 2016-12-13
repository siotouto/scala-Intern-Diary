package interndiary.service

import interndiary.model.{Entry, User, Comment}
import interndiary.repository

class DiaryApp(currentUserName: String) {
  def currentUser()(implicit
    ctx: Context
  ): User =
    repository.Users.findOrCreateByName(currentUserName)

  def isAuthorName(userName: String): Boolean = 
    (userName == currentUserName)

  def isAuthorId(userId: Long)(implicit
    ctx: Context
  ): Boolean =
    (userId == currentUser.id)

  private def findModule(userName: String, entryId: Long)(implicit
    ctx: Context
  ): Either[Error, Entry] = for {
    user <- repository.Users.findByName(userName).toRight(UserNotFoundError).right
    entry <- repository.Entries.findByUserAndEntryId(user, entryId).toRight(EntryNotFoundError).right
  } yield entry

  def write(title: String, body: String)(implicit
    ctx: Context
  ): Either[Error, Entry] =
    repository.Entries.create(currentUser.id, title, body)
      .toRight(FailedToEditEntryError)

  def read(userName: String)(implicit
    ctx: Context
  ): Either[Error, Seq[Entry]] = for {
    user <- {
      repository.Users.findByName(userName)
        .toRight(UserNotFoundError).right
    }
    entries <- {
      repository.Entries.listByUser(user)
        .toRight(FailedToListDiaryError).right
    }
  } yield entries

  def find(userName: String, entryId: Long)(implicit
    ctx: Context
  ): Either[Error, Tuple2[Entry, Seq[Comment]]] = for {
    entry <- findModule(userName, entryId).right
    comments <- repository.Comments.listByEntry(entryId).toRight(FailedToGetCommentsError).right
  } yield (entry, comments)

  def update(userName: String, entryId: Long, title: String, body: String)(implicit
      ctx: Context
  ): Either[Error, Entry] = for {
    entry <- findModule(userName, entryId).right
    _ <- {
      if(isAuthorId(entry.userId))
        repository.Entries.updateEntry(entry, title, body)
          .toRight(FailedToEditEntryError).right
      else
        Left(UnauthorizedError).right
    }
  } yield entry

  def delete(userName :String, entryId: Long)(implicit
    ctx: Context
  ): Either[Error, Entry] = for {
    entry <- findModule(userName, entryId).right
    _ <- {
      if(isAuthorId(entry.userId))
        repository.Entries.delete(entry)
          .toRight(FailedToDeleteError).right
      else
        Left(UnauthorizedError).right
    }
  } yield entry

  /*
  def comment(entryId: Long, body: String)(implicit
    ctx: Context
  ): Either[Error, Entry] = for {
    entry <- {
      repository.Entries.findById(entryId)
        .toRight(EntryNotFoundError).right
    }
    _ <- {
      repository.Comments.create(entry.id, currentUser.id, body)
        .toRight(FailedToAddCommentError).right
    }
  } yield entry
   */
}
