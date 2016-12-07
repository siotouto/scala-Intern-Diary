package interndiary.service

import interndiary.model.{Entry, User}
import interndiary.repository

class DiaryApp(currentUserName: String) {
  def currentUser()(implicit
    ctx: Context
  ): User =
    repository.Users.findOrCreateByName(currentUserName)

  def write(title: String, body: String)(implicit
    ctx: Context
  ): Either[Error, Entry] =
    repository.Entries.create(currentUser.id, title, body)
      .toRight(FailedToCreateEntryError)


  def read(user: String)(implicit
    ctx: Context
  ): Either[Error, Seq[Entry]] = for {
    user <- {
      repository.Users.findByName(user)
        .toRight(UserNotFoundError).right
    }
    entries <- {
      repository.Entries.listByUserId(user.id)
        .toRight(FailedToListDiaryError).right
    }
  } yield entries

  def delete(entryId: Long)(implicit
    ctx: Context
  ): Either[Error, Entry] = for {
    entry <- {
      repository.Entries.findById(entryId)
        .toRight(EntryNotFoundError).right
    }
    _ <- {
      if(entry.userId != currentUser.id)
        Left(UnauthorizedToDeleteError).right
      else
        repository.Entries.deleteById(entry.id)
          .toRight(FailedToDeleteError).right
    }
  } yield entry

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
}
