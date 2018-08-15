package codacy.solhint

import java.nio.file.Paths

import com.codacy.plugins.api.results.Result.{FileError, Issue}
import com.codacy.plugins.api.{Options, Source}
import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import com.codacy.tools.scala.seed.utils.CommandRunner

import scala.util.Try
import scala.util.matching.Regex

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
        paths.map(_.toString)(collection.breakOut)
      }

      val command = List("solhint", "-f","unix") ++ filesToLint

      CommandRunner.exec(command, Option(path.toFile)) match {
        case Right(resultFromTool) =>
          val output = resultFromTool.stdout ++ resultFromTool.stderr
          parseToolResult(output)
        case Left(failure) =>
          throw failure
      }
    }
  }

  private def parseToolResult(resultFromTool: List[String]): List[Result] = {
    val outputFormat: Regex = """(.+):(.+):(.+): (.+) \[(.+)\/(.+)\]""".r
    resultFromTool.collect {
      case outputFormat(fileName, line, _, description, _, pattern) =>
        Issue(Source.File(fileName), Result.Message(description), Pattern.Id(pattern), Source.Line(line.toInt))
    }
  }

}
