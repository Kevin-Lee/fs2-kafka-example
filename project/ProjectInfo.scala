import wartremover.WartRemover.autoImport.{Wart, Warts}

object ProjectInfo {
  final case class ProjectName(projectName: String) extends AnyVal

  val ProjectVersion: String = "1.2.0"

  def commonWarts(scalaBinaryVersion: String): Seq[wartremover.Wart] = scalaBinaryVersion match {
    case "2.10" =>
      Seq.empty
    case _ =>
      Warts.all
  }

}
