package interndiary.repository

import org.joda.time.LocalDateTime
import org.joda.time.LocalDateTime.Property

object MyTime {
  def create(): LocalDateTime =
    new LocalDateTime().secondOfMinute().roundFloorCopy()
}
