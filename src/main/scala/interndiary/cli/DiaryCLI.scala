package interndiary.cli

import interndiary.service.{Context, DiaryApp}
import interndiary.model.Entry
//import interndiary.model.{User, Entry, Comment}

import scala.sys.process
import scala.util.control.Exception.allCatch

object DiaryCLI {
  def main(args: Array[String]): Unit = {
    val exitStatus = run(args)
    sys.exit(exitStatus)
  }

  def createApp(userName: String): DiaryApp = new DiaryApp(userName)

  def run(args: Array[String]): Int = {
    sys.env.get("USER") match {
      case Some(userName) =>
        try {
          Context.setup("db.default")
          implicit val ctx = Context.createContext()
          val app = createApp(userName)
          args.toList match {
            case "write" :: title :: body :: _ =>
              write(app, title, body)
            case "read" :: "all" :: rest =>
              val author = rest.headOption.getOrElse(userName)
              read(app, readTitleOnly = false, author)
            case "read" :: "title" :: rest =>
              val author = rest.headOption.getOrElse(userName)
              read(app, readTitleOnly = true, author)
            case "delete" :: entryIdStr :: _ =>
              delete(app, entryIdStr)
            case "comment" :: entryIdStr :: body :: _ =>
              comment(app, entryIdStr, body)
            case _ =>
              help()
          }
        } finally Context.destroy()
      case None => 
        process.stderr.println("USER environment must be set.")
        1
    }
  }

  def write(app: DiaryApp, title: String, body: String)
    (implicit ctx: Context): Int = {
    app.write(title, body) match {
      case Right(entry) =>
        println("Wrote " + prettifyEntry(entry))
        0
      case Left(error) =>
        process.stderr.println(error)
        1
    }
  }

  def read(app: DiaryApp, readTitleOnly: Boolean = false, userName: String)
    (implicit ctx: Context): Int = {
    app.read(userName) match {
      case Right(entries) =>
        println(
          s"---${userName}'s diary---" +
            (
              if(readTitleOnly) "titles only---"
              else ""
            )
        )
        entries.foreach(entry => println(prettifyEntry(entry, readTitleOnly)))
        0
      case Left(error) =>
        process.stderr.println(error)
        1
    }
  }

  def delete(app: DiaryApp, entryIdStr: String)
    (implicit ctx: Context): Int = {
    allCatch opt entryIdStr.toLong match{
      case Some(entryId) =>
        app.delete(entryId) match {
          case Right(entry) =>
            println("Delete " + prettifyEntry(entry))
            0
          case Left(error) =>
            process.stderr.println(error)
            1
        }
      case None =>
        process.stderr.println("entry ID must be Integer")
        1
    }
  }

  def comment(app: DiaryApp, entryIdStr: String, body: String)
    (implicit ctx: Context): Int = {
    allCatch opt entryIdStr.toLong match{
      case Some(entryId) =>
        app.comment(entryId, body) match {
          case Right(entry) =>
            println("Wrote " + prettifyEntry(entry))
            0
          case Left(error) =>
            process.stderr.println(error)
            1
        }
      case None =>
        process.stderr.println("entry ID must be Integer")
        1
    }
  }

  def help(): Int = {
    process.stderr.println(
      """
        | usage:
        |   run write [entry title] [entry body]
        |   run read {title, all} (user name)
              (if (user name) is blank, read yours)
        |   run delete [entry Id]
        |   run comment [entry Id] [comment body]
      """.stripMargin)
    1
  }

  private[this] def prettifyEntry(entry: Entry, readTitleOnly: Boolean = false)
      : String = {
    f"${entry.id}%5d: ${entry.title}%s" +
    (
      if (readTitleOnly) ""
      else s", ${entry.body}"
    )
  }
}
