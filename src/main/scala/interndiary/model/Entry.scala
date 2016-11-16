package interndiary.model

import org.joda.time.LocalDateTime

case class Entry(
  id: Long,
  userId: Long,
  title: String,
  body: String,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)
