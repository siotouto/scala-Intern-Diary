package interndiary.service

import interndiary.helper._

import scala.util.Random

class DiaryAppSpec extends UnitSpec with SetupDB {
  private def createApp(): DiaryApp = new DiaryApp(Random.nextInt().toString)

  describe("DiaryApp") {
    it("should able to write and read") {
      val app = createApp()
      app.write("Test title","This is unit test.").fold(
        { _ => fail() },
        { entry =>
          entry.userId shouldBe app.currentUser.id
          entry.title shouldBe "Test title"
          entry.body shouldBe "This is unit test."
        }
      )
      app.read(app.currentUser.name).fold(
        { _ => fail() },
        { entries =>
          entries.length shouldBe 1
          entries.head.title shouldBe "Test title"
          entries.head.body shouldBe "This is unit test."
        }
      )
      app.write("2nd entry","I won't see bugs anymore.").fold(
        { _ => fail() },
        { entry =>
          entry.title shouldBe "2nd entry"
          entry.body shouldBe "I won't see bugs anymore."
        }
      )
      app.read(app.currentUser.name).fold(
        { _ => fail() },
        { entries =>
          entries.length shouldBe 2
          entries.head.title shouldBe "2nd entry"
          entries(1).body shouldBe "This is unit test."
        }
      )
    }

    it("should able to write and read and delete") {
      val app = createApp()
      val entry0 = app.write("Test title","This is unit test.").right.get
      val entry1 = app.write("2nd entry","I won't see bugs anymore.").right.get
      val entry2 = app.write("3rd entry","I will delete all bugs.").right.get
      val entry3 = app.write("4th entry","tired.").right.get
      app.read(app.currentUser.name).fold(
        { _ => fail() },
        { entries =>
          entries.length shouldBe 4
          entries.head.title shouldBe "4th entry"
          entries(2).body shouldBe "I won't see bugs anymore."
          entries(1) shouldBe entry2
        }
      )
      app.delete(entry1.id)
      app.read(app.currentUser.name).fold(
        { _ => fail() },
        { entries =>
          entries.length shouldBe 3
          entries.head.title shouldBe "4th entry"
          entries(2).body shouldBe "This is unit test."
        }
      )
      app.delete(entry3.id)
      app.read(app.currentUser.name).fold(
        { _ => fail() },
        { entries =>
          entries.length shouldBe 2
          entries.head.title shouldBe "3rd entry"
          entries(1).body shouldBe "This is unit test."
          entries(1) shouldBe entry0
        }
      )
    }
  }
}
