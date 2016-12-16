package interndiary.web

final case object EmptyTitleEntryError extends Error {
  override def toString(): String =
    "Entry title must be non-Empty."
}

