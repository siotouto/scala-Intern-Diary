package interndiary.web

import interndiary.helper.{SetupDB, WebUnitSpec}
import interndiary.repository
import interndiary.model.{Entry, User}

import scala.util.Random

class DiaryWebForTest extends DiaryWeb {
}

class DiaryWebSpec extends WebUnitSpec with SetupDB {

  describe("DiaryWeb") {
    addServlet(classOf[DiaryWebForTest], "/*")

    val testUserName: String = Random.nextInt().toString
    def testUser()(implicit
      ctx: repository.Context
    ): User =
      repository.Users.findOrCreateByName(testUserName)

    val webUserName = "siotouto"
    def webUser()(implicit
      ctx: repository.Context
    ): User =
      repository.Users.findOrCreateByName(webUserName)

    val nonExistUser = Random.nextInt().toString + "nonExist"

    def createEntry(): Entry = {
      val title: String = Random.nextInt().toString + "t"
      val body: String = Random.nextInt().toString + "b"
      post(
        "/my/write",
        params = List("title" -> title, "body" -> body)
      ) {
        val redirectAddress = header.get("Location").get
        val gettingEntryIdRegEx =
          (s"/user/${webUserName}/entry/" + """(\d+)""").r
        val entryId = redirectAddress match {
          case gettingEntryIdRegEx(entryIdString) => entryIdString.toLong
        }
        val entry =
          repository.Entries.findByUserAndEntryId(webUser, entryId).get
        entry.title shouldBe title
        entry.body shouldBe body
        entry
      }
    }

    it("postEntry method should work") {
      val entry = createEntry()
    }

    it("should redirect to the user diary's page when the top page is accessed") {
      get("/") {
        status shouldBe 302
        header.get("Location") should contain(s"/user/${webUserName}/")
      }
    }

    it("should show list page") {
      get(s"/user/${webUserName}/") {
        status shouldBe 200
      }
      get(s"/user/${nonExistUser}/") {
        status shouldBe 404
      }
    }

    it("should show write page") {
      get("/my/write") {
        status shouldBe 200
      }
    }

    it("should create an entry when entry is posted") {
      val title: String = Random.nextInt().toString + "t"
      val body: String = Random.nextInt().toString + "b"
      post(
        "/my/write",
        params = List("title" -> title, "body" -> body)
      ) {
        status shouldBe 302
        val redirectAddress = header.get("Location").get
        val gettingEntryIdRegEx =
          (s"/user/${webUserName}/entry/" + """(\d+)""").r
        val entryId = redirectAddress match {
          case gettingEntryIdRegEx(entryIdString) => entryIdString.toLong
        }
        val entry =
          repository.Entries.findByUserAndEntryId(webUser, entryId).get
        entry.title shouldBe title
        entry.body shouldBe body
      }
    }

    it("should not create an entry when empty-title entry is posted") {
      val title: String = "  "
      val body1: String = Random.nextInt().toString + "b"
      post(
        "/my/write",
        params = List("title" -> title, "body" -> body1)
      ) {
        status shouldBe 400
      }
    }

    it("should show entry page") {
      val entry = createEntry()
      get(s"/user/${webUserName}/entry/${entry.id}") {
        status shouldBe 200
      }
      get(s"/user/${nonExistUser}/entry/${entry.id}") {
        status shouldBe 404
      }
      get(s"/user/${webUserName}/entry/${Random.nextInt().toString}") {
        status shouldBe 404
      }
      get(s"/user/${webUserName}/entry/${entry.title}") {
        status shouldBe 400
      }
    }

    it("should show edit page") {
      val entry = createEntry()
      get(s"/user/${webUserName}/entry/${entry.id}/edit") {
        status shouldBe 200
      }
      get(s"/user/${nonExistUser}/entry/${entry.id}/edit") {
        status shouldBe 404
      }
      get(s"/user/${webUserName}/entry/${Random.nextInt().toString}/edit") {
        status shouldBe 404
      }
      get(s"/user/${webUserName}/entry/${entry.title}/edit") {
        status shouldBe 400
      }
    }

    it("should update an entry when entry is edited") {
      val entry = createEntry()
      val newTitle: String = Random.nextInt().toString + "nt"
      val newBody: String = Random.nextInt().toString + "nb"
      post(
        s"/user/${webUserName}/entry/${entry.id}/edit",
        params = List("title" -> newTitle, "body" -> newBody)
      ) {
        status shouldBe 302
        header.get("Location").get shouldBe s"/user/${webUserName}/entry/${entry.id}"
        val newEntry =
          repository.Entries.findByUserAndEntryId(webUser, entry.id).get
        newEntry.title shouldBe newTitle
        newEntry.body shouldBe newBody
      }
    }

    it("should not update any entry when entry is edited to empty title") {
      val entry = createEntry()
      val newTitle: String = " \t  \n  "
      val newBody: String = Random.nextInt().toString + "nb"
      post(
        s"/user/${webUserName}/entry/${entry.id}/edit",
        params = List("title" -> newTitle, "body" -> newBody)
      ) {
        status shouldBe 400
      }
    }

    it("should show delete page") {
      val entry = createEntry()
      get(s"/user/${webUserName}/entry/${entry.id}/delete") {
        status shouldBe 200
      }
      get(s"/user/${nonExistUser}/entry/${entry.id}/delete") {
        status shouldBe 404
      }
      get(s"/user/${webUserName}/entry/${Random.nextInt().toString}/delete") {
        status shouldBe 404
      }
      get(s"/user/${webUserName}/entry/${entry.title}/delete") {
        status shouldBe 400
      }
    }

    it("should delete an entry when post deleting entry") {
      val entry = createEntry()
      get(s"/user/${webUserName}/entry/${entry.id}") {
        status shouldBe 200
      }
      get(s"/user/${webUserName}/entry/${entry.id}/edit") {
        status shouldBe 200
      }
      get(s"/user/${webUserName}/entry/${entry.id}/delete") {
        status shouldBe 200
      }
      repository.Entries.findByUserAndEntryId(webUser, entry.id) shouldBe 'defined

      post(
        s"/user/${webUserName}/entry/${entry.id}/delete"
      ) {
        status shouldBe 302
        header.get("Location").get shouldBe "/"
      }

      get(s"/user/${webUserName}/entry/${entry.id}") {
        status shouldBe 404
      }
      get(s"/user/${webUserName}/entry/${entry.id}/edit") {
        status shouldBe 404
      }
      get(s"/user/${webUserName}/entry/${entry.id}/delete") {
        status shouldBe 404
      }
      repository.Entries.findByUserAndEntryId(webUser, entry.id) shouldBe 'empty
    }

    it("should be able to edit/delete entry by only author."){
      val title: String = Random.nextInt().toString + "t"
      val body: String = Random.nextInt().toString + "b"
      val Some(entry) = repository.Entries.create(testUser.id, title, body)
      get(s"/user/${testUserName}/entry/${entry.id}") {
        status shouldBe 200
      }
      get(s"/user/${testUserName}/entry/${entry.id}/edit") {
        status shouldBe 403
      }
      val newTitle: String = Random.nextInt().toString + "nt"
      val newBody: String = Random.nextInt().toString + "nb"
      post(
        s"/user/${testUserName}/entry/${entry.id}/edit",
        params = List("title" -> newTitle, "body" -> newBody)
      ) {
        status shouldBe 403
      }
      get(s"/user/${testUserName}/entry/${entry.id}/delete") {
        status shouldBe 403
      }
      post(s"/user/${testUserName}/entry/${entry.id}/delete") {
        status shouldBe 403
      }
    }
  }

}


