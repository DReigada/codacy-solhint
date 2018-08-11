package codacy.solhint

import java.nio.file.{Path, Paths}

import com.codacy.plugins.api.results.Result.Issue
import com.codacy.plugins.api.{Options, Source}
import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import com.codacy.tools.scala.seed.utils.CommandRunner
import play.api.libs.json._

import scala.util.Try

case class WarnResult(patternId: String, file: String, message: String, line: String = "1")

object WarnResult {
  implicit val warnResultFmt = Json.format[WarnResult]
}

object Solhint extends Tool {

  val configFile = ".solhint.json"

  override def apply(
    source: Source.Directory,
    configuration: Option[List[Pattern.Definition]],
    files: Option[Set[Source.File]],
    options: Map[Options.Key, Options.Value]
  )(implicit specification: Tool.Specification): Try[List[Result]] = {
    Try {
      val path = Paths.get(source.path)
      val filesToLint: Seq[String] = files.fold(Seq(source.path)) { paths =>
        paths.map(_.toString).toSeq
      }

      val command = List("solhint", "-f unix ") ++ filesToLint

      CommandRunner.exec(command) match {
        case Right(resultFromTool) =>
          val output = resultFromTool.stdout ++ resultFromTool.stderr
          parseToolResult(output, path, checkPattern(configuration))
        case Left(failure) =>
          throw failure
      }
    }
  }

  private def parseToolResult(resultFromTool: List[String], path: Path, wasRequested: String => Boolean): List[Result] = ???

  private def checkPattern(conf: Option[List[Pattern.Definition]])(patternId: String): Boolean = {
    conf.forall(_.exists(_.patternId.value.toLowerCase == patternId.toLowerCase))
  }

}
