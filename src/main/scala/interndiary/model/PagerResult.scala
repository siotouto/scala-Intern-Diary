package interndiary.model

case class PagerResult(
  entries: Seq[Entry],
  preOffset: Option[Int],
  succOffset: Option[Int]
)
