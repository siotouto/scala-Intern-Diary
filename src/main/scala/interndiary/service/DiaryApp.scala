package interndiary.service

import interndiary.model.{Entry, User, Comment, PagerResult}
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

  def read(userName: String, offset: Int, pageSize: Int = 5)(implicit
    ctx: Context
  ): Either[Error, PagerResult] = for {
    user <- {
      repository.Users.findByName(userName)
        .toRight(UserNotFoundError).right
    }
    entries <- {
      repository.Entries.listByUser(user, offset, pageSize + 1)
        .toRight(FailedToListDiaryError).right
    }
  } yield PagerResult(
    entries.take(pageSize),
    Some(offset - pageSize)
      .filter(_ => offset > 0)
      .map(_.max(0)),
    Some(offset + pageSize)
      .filter(_ => pageSize < entries.size)
  )

  def find(userName: String, entryId: Long)(implicit
    ctx: Context
  ): Either[Error, (Entry, Seq[Comment])] = for {
    entry <- findModule(userName, entryId).right
    comments <- repository.Comments.listByEntry(entryId).toRight(FailedToGetCommentsError).right
  } yield (entry, comments)

  def update(userName: String, entryId: Long, title: String, body: String)(implicit
      ctx: Context
  ): Either[Error, Entry] = for {
    entry <- findModule(userName, entryId).right
    _ <- Some(entry.userId).filter(isAuthorId).toRight(UnauthorizedError).right
    newEntry <- repository.Entries.update(entry, title, body).toRight(FailedToEditEntryError).right
  } yield entry

  def delete(userName :String, entryId: Long)(implicit
    ctx: Context
  ): Either[Error, Entry] = for {
    entry <- findModule(userName, entryId).right
    _ <- Some(entry.userId).filter(isAuthorId).toRight(UnauthorizedError).right
    newEntry <- repository.Entries.delete(entry).toRight(FailedToDeleteError).right
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
