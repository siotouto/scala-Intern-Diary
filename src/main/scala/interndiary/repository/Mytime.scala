package interndiary.repository

import org.joda.time.LocalDateTime

object MyTime {
  def apply(): LocalDateTime = floorSecond()
  def floorSecond(): LocalDateTime =
    new LocalDateTime().secondOfMinute().roundFloorCopy()
}
