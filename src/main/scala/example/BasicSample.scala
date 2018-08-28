package example

import java.util

import better.files.File
import com.vladsch.flexmark.ast.{Emphasis, Node, StrongEmphasis}
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables._
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.MutableDataSet

import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import com.codacy.plugins.api.results.{Parameter, Pattern, Result, Tool}


object BasicSample {

  def main(args: Array[String]): Unit = {
    val options = new MutableDataSet
    // uncomment to set optional extensions
    options.set(Parser.EXTENSIONS, util.Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));
    // uncomment to convert soft-breaks to hard breaks
    //options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
    val parser = Parser.builder(options).build
    val renderer = HtmlRenderer.builder(options).build
    // You can re-use parser and renderer instances

    val file = File("/Users/dreigada/myWorkspace/codacy-solhint/test.md").contentAsString
    val document = parser.parse(file)

    def filterType[A: ClassTag](node: Node): List[A] =
      node.getChildIterator.asScala.toList.collect {
        case a: A => a
      }

    def tableRowToPattern(tableRow: TableRow): (String, String, String) = {
      tableRow.getChildIterator.asScala.toList match {
        case List(rule: TableCell, error: TableCell, options: TableCell) =>
          val ruleName = filterType[StrongEmphasis](rule).head.getText.toString

          val t = filterType[Emphasis](options).head.getText.toString

          Pattern.Specification(ruleName, Result.Level.Err, , None)

          (ruleName, error.getText.toString, t)
      }
    }

    val scalaa: List[(String, String, String)] =
      document.getChildIterator.asScala.toList
        .collect { case table: TableBlock => table }
        .flatMap(filterType[TableBody])
        .flatMap(filterType[TableRow])
        .map(tableRowToPattern)

    val html = renderer.render(document) // "<p>This is <em>Sparta</em></p>\n"
    System.out.println(html)
  }
}
