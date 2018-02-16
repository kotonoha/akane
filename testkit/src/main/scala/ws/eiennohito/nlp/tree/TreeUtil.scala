package ws.eiennohito.nlp.tree

import org.apache.commons.io.IOUtils
import org.scalatest.Suite
import ws.kotonoha.akane.analyzers.juman.{JumanConfig, JumanSequence, JumanSubprocess}
import ws.kotonoha.akane.analyzers.knp.{TableConverter, TableWrapper}
import ws.kotonoha.akane.analyzers.knp.raw.OldAngUglyKnpTable
import ws.kotonoha.akane.io.Charsets
import ws.kotonoha.akane.parser.KnpTabFormatParser

/**
  * @author eiennohito
  * @since 2014-11-07
  */
object TreeUtil {
  import ws.kotonoha.akane.resources.FSPaths._

  import scala.collection.JavaConverters._

  val parser = new KnpTabFormatParser
  def tabTreeFromResource(path: String)(implicit cl: ClassLoader) = {
    val res = cl.getResource(path)
    if (res == null) {
      throw new TreeException("could not find a tree with name: " + path)
    }
    val o = for (in <- res.openStream().res) yield {
      parser.parse(IOUtils.readLines(in, Charsets.utf8).asScala)
    }
    o.obj
  }
}

class TreeException(msg: String) extends RuntimeException(msg)

trait KnpTrees { self: Suite =>
  def resTree(res: String): TableWrapper = {
    val ugly: OldAngUglyKnpTable = resTreeOld(res)
    val clean = TableConverter.fromOld(ugly)
    TableWrapper.wrap(clean)
  }

  def resTreeOld(res: String): OldAngUglyKnpTable = {
    TreeUtil.tabTreeFromResource(res)(self.getClass.getClassLoader)
  }

  def resJuman(res: String): JumanSequence =
    JumanUtil.jumanTableFromResource(res)(self.getClass.getClassLoader)
}

object JumanUtil {
  import ws.kotonoha.akane.resources.FSPaths._

  val reader = JumanSubprocess.reader(new JumanConfig("", "utf-8", Nil))

  def jumanTableFromResource(path: String)(implicit cl: ClassLoader): JumanSequence = {
    val res = cl.getResource(path)
    val o = for (in <- res.openStream().res) yield {
      reader.readFrom(in)
    }

    o.obj.get
  }
}
