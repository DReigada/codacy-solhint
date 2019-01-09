package codacy.solhint

import java.nio.file.{Path, Paths}

import better.files.File
import com.codacy.plugins.api.results.Result.{FileError, Issue}
import com.codacy.plugins.api.{Options, Source}
import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import com.codacy.tools.scala.seed.utils.CommandRunner
import com.codacy.tools.scala.seed.utils.FileHelper._
import play.api.libs.json._

import scala.util.Try
import scala.util.matching.Regex

object Solhint extends Tool {

  val configFile = Set(".solhint.json")

  override def apply(
    source: Source.Directory,
    configuration: Option[List[Pattern.Definition]],
    files: Option[Set[Source.File]],
    options: Map[Options.Key, Options.Value]
  )(implicit specification: Tool.Specification): Try[List[Result]] = {

    val configFilePath = getConfigFile(source, configuration)
    Try {
      val path = Paths.get(source.path)
      val filesToLint: Seq[String] = files.fold(Seq(source.path)) { paths =>
        paths.map(_.toString)(collection.breakOut)
      }

      val command = List("solhint", "-f", "unix", "--config", configFilePath.toString) ++ filesToLint

      CommandRunner.exec(command, Option(path.toFile)) match {
        case Right(resultFromTool) =>
          val output = resultFromTool.stdout ++ resultFromTool.stderr
          parseToolResult(output)
        case Left(failure) =>
          throw failure
      }
    }
  }

  def checkForExistingConfigFile(source: Source.Directory): Option[Path] = {
    findConfigurationFile(Paths.get(source.path), configFile)
  }

  def getConfigFile(source: Source.Directory, configuration: Option[List[Pattern.Definition]]): Path = {
    configuration.map { config =>
      val patterns = config.map { pattern =>
        val parameter = pattern.parameters
          .flatMap(_.headOption.map { param =>
            val parameterValue: JsValue = param.value
            parameterValue
          })
          .getOrElse(JsNull)

        (pattern.patternId.value, parameter)
      }
      File
        .newTemporaryFile(".solhint", ".json")
        .write(Json.prettyPrint(Json.toJson(JsObject(Seq(("rules", JsObject(patterns)))))))
        .path
    }.orElse {
      checkForExistingConfigFile(source)
    }.getOrElse {
      File
        .newTemporaryFile(".solhint", ".json")
        .write(Json.prettyPrint(Json.toJson(JsObject(Seq(("extends", JsString("default")))))))
        .path
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
