package interndiary.service

import interndiary.helper._

import scala.util.Random
import org.joda.time.{DateTime, DateTimeUtils}

class DiaryAppSpec extends UnitSpec with SetupDB {
  private def createApp(): DiaryApp = new DiaryApp(Random.nextInt().toString)

  def mockTimeMillisFixed[T](millis: Long)(block: => T): T = {
    DateTimeUtils.setCurrentMillisFixed(millis)
    try {
      block
    } finally {
      DateTimeUtils.setCurrentMillisSystem()
    }
  }

  def millisPerHour: Long = 3600000L
  describe("DiaryApp") {
    it("should be able to write and read") {
      val app: DiaryApp = createApp()
      val nowMillis: Long = DateTime.now().getMillis()
      val testTime: Seq[Long] = (0L until 10L).map(nowMillis + millisPerHour * _)
      val Right(entry0) = mockTimeMillisFixed(testTime(0))(
        app.write("Test title", "This is unit test.")
      )
      entry0.userId shouldBe app.currentUser.id
      entry0.title shouldBe "Test title"
      entry0.body shouldBe "This is unit test."

      val Right(entries0) = app.read(app.currentUser.name)
      entries0.length shouldBe 1
      entries0.head.title shouldBe "Test title"
      entries0.head.body shouldBe "This is unit test."

      val Right(entry1) = mockTimeMillisFixed(testTime(1))(
        app.write("2nd entry", "I won't see bugs anymore.")
      )
      entry1.title shouldBe "2nd entry"
      entry1.body shouldBe "I won't see bugs anymore."

      val Right(entries1) = app.read(app.currentUser.name)
      entries1.length shouldBe 2
      entries1.head.title shouldBe "2nd entry"
      entries1(1).body shouldBe "This is unit test."
      entries1 shouldBe Seq(entry1, entry0)
    }

    it("should be able to delete") {
      val app: DiaryApp = createApp()
      val nowMillis: Long = DateTime.now().getMillis()
      val testTime: Seq[Long] = (0L until 10L).map(nowMillis + millisPerHour * _)
      val Right(entry0) = mockTimeMillisFixed(testTime(0)){
        app.write("Test title", "This is unit test.")
      }
      val Right(entry1) = mockTimeMillisFixed(testTime(1)){
        app.write("2nd entry", "I won't see bugs anymore.")
      }
      val Right(entry2) = mockTimeMillisFixed(testTime(2)){
        app.write("3rd entry", "I will delete all bugs.")
      }
      val Right(entry3) = mockTimeMillisFixed(testTime(3)){
        app.write("4th entry", "tired.")
      }
      print(app.read(app.currentUser.name))

      val Right(entries0) = app.read(app.currentUser.name)
      entries0.length shouldBe 4
      entries0(0) shouldBe entry3
      entries0(1) shouldBe entry2
      entries0(2) shouldBe entry1

      app.delete(entry1.id)
      val Right(entries1) = app.read(app.currentUser.name)
      entries1.length shouldBe 3
      entries1(0) shouldBe entry3
      entries1(2) shouldBe entry0

      app.delete(entry3.id)
      val Right(entries2) = app.read(app.currentUser.name)
      entries2.length shouldBe 2
      entries2(0) shouldBe entry2
      entries2(1) shouldBe entry0
    }

    it("should work when write diaries to DB any order") {
      val app: DiaryApp = createApp()
      val nowMillis: Long = DateTime.now().getMillis()
      val testTime: Seq[Long] = (0L until 10L).map(nowMillis + millisPerHour * _)
      val Right(entry3) = mockTimeMillisFixed(testTime(3)){
        app.write("4th entry", "tired.")
      }
      val Right(entry1) = mockTimeMillisFixed(testTime(1)){
        app.write("2nd entry", "I won't see bugs anymore.")
      }
      val Right(entry0) = mockTimeMillisFixed(testTime(0)){
        app.write("3rd entry", "I will delete all bugs.")
      }
      val Right(entry2) = mockTimeMillisFixed(testTime(2)){
        app.write("Test title", "This is unit test.")
      }

      val Right(entries0) = app.read(app.currentUser.name)
      entries0.length shouldBe 4
      entries0(0) shouldBe entry3
      entries0(1) shouldBe entry2
      entries0(2) shouldBe entry1

      app.delete(entry1.id)
      val Right(entries1) = app.read(app.currentUser.name)
      entries1.length shouldBe 3
      entries1(0) shouldBe entry3
      entries1(2) shouldBe entry0

      app.delete(entry3.id)
      val Right(entries2) = app.read(app.currentUser.name)
      entries2.length shouldBe 2
      entries2(0) shouldBe entry2
      entries2(1) shouldBe entry0
    }
    // comment has been not supported yet.
  }
}
