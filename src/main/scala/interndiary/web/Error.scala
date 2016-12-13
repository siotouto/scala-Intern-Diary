package interndiary.web

final case object EmptyTitleEntryError extends Error {
  override def toString(): String =
    "Entry title must be non-Empty."
}

final case object UnauthorizedError extends Error {
  override def toString(): String =
    """
It's seems to be unauthorized.
Confirm your ID and URI.
"""

}

