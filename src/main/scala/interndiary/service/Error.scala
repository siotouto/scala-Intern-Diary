package interndiary.service

final case object FailedToCreateEntryError extends Error {
  override def toString(): String =
    "Fail to create entry. Sorry."
}

final case object FailedToListDiaryError extends Error {
  override def toString(): String =
    "Fail to list diary. Sorry."
}

final case object FailedToDeleteError extends Error {
  override def toString(): String =
    "Fail to delete entry. Sorry."
}

final case object FailedToAddCommentError extends Error {
  override def toString(): String =
    "Fail to add your comment. Sorry."
}

final case object UserNotFoundError extends Error {
  override def toString(): String =
    "Can not find a target user."
}

final case object EntryNotFoundError extends Error {
  override def toString(): String =
    "Can not find a target entry."
}

final case object UnauthorizedToDeleteError extends Error {
  override def toString(): String =
    "Only author can delete and you can't."
}

