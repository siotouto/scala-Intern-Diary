package interndiary.service

final case object FailedToEditEntryError extends Error {
  override def toString(): String =
    "Failed to edit entry. Sorry."
}

final case object FailedToListDiaryError extends Error {
  override def toString(): String =
    "Failed to list diary. Sorry."
}

final case object FailedToDeleteError extends Error {
  override def toString(): String =
    "Failed to delete entry. Sorry."
}

final case object FailedToAddCommentError extends Error {
  override def toString(): String =
    "Failed to add your comment. Sorry."
}

final case object FailedToGetCommentsError extends Error {
  override def toString(): String =
    "Failed to get entry's comments. Sorry."
}

final case object UserNotFoundError extends Error {
  override def toString(): String =
    "Can not find a target user."
}

final case object EntryNotFoundError extends Error {
  override def toString(): String =
    "Can not find a target entry."
}

final case object UnauthorizedError extends Error {
  override def toString(): String =
    "Only author can edit/delete and you can't."
}

