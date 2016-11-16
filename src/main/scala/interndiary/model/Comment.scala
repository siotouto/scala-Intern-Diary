package interndiary.model

import org.joda.time.LocalDateTime

case class Comment(
  id: Long,
  entryId: Long,
  userId: Long,
  body: String,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)
