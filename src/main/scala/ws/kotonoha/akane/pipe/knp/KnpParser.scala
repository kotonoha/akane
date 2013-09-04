package ws.kotonoha.akane.pipe.knp

import ws.kotonoha.akane.pipe.knp.lisp._
import com.typesafe.scalalogging.slf4j.Logging
import ws.kotonoha.akane.pipe.knp.lisp.KAtom
import scala.Some

/**
 * @author eiennohito
 * @since 2013-09-04
 */
object KnpParser extends Logging {

  val intRe = "\\d+".r.unanchored

  def parseFeatures(elements: List[KElement]): List[String] = {
    elements.flatMap {
      case KAtom(e) => e :: Nil
      case x => logger.warn("unexpected element in feature " + x); Nil
    }
  }

  def parseSurface(elements: List[KElement]) = {
    elements.flatMap {
      case KItems(KAtom(surf), KAtom(read), KAtom(writ),
        KAtom(posStr), KInt(posId),
        KAtom(catStr), KInt(catId),
        KAtom(conjTypeStr), KInt(conjTypeId),
        KAtom(conjFormStr), KInt(conjFormId),
        info, KList(features)) =>
        val cinfo = info match {
          case KList(Nil) => ""
          case KAtom(s) => s
        }
        KnpLexeme(
          surf, read, writ,
          JumanPosInfo(
            PosItem(posStr, posId),
            PosItem(catStr, catId),
            PosItem(conjTypeStr, conjTypeId),
            PosItem(conjFormStr, conjFormId)
          ),
        cinfo, parseFeatures(features)
      ) :: Nil
      case x => logger.warn("unsupported surface pattern " + x); Nil
    }
  }

  def parseKind(elements: List[KElement]) = {
    elements match {
      case List(KAtom(kind)) => kind
      case x => logger.warn("unknown kind pattern " + elements); "type:unknown"
    }
  }

  def parseLeaf(list: KList) = {
    list match {
      case KItems(KInt(num), KList(kind), KList(surface), KList(features), _) =>
        val pKind = parseKind(kind)
        val pSurface = parseSurface(surface)
        val pFeatures = parseFeatures(features)
        Some( KnpNode(num, pKind, pSurface, pFeatures) )
      case o => logger.warn("unknown leaf pattern " + o); None
    }
  }

  def parseItems(elements: List[KElement]): List[KnpNode] = {
    elements.flatMap {
      case e @ KList(KAtom(x) :: _) if intRe.pattern.matcher(x).matches() => //only one element that has int as a first element of list
        val leaf = parseLeaf(e)
        leaf
      case other: KList =>
        val tree = parseTree(other)
        tree
      case invalid =>
        logger.warn("unknown item pattern" + invalid + "\nsrc:" + elements ); Nil
    }
  }

  def parseTree(tree: KList): Option[KnpNode] = {
    tree match {
      case KList(KAtom("noun_para") :: children) =>
        val cnodes = parseItems(children)
        Some(KnpNode(-2, "type:N", Nil, Nil, cnodes))
      case KList(KAtom("pred_para") :: children) =>
        val cnodes = parseItems(children)
        Some(KnpNode(-3, "type:P", Nil, Nil, cnodes))
      case KList(l) if l.tail == Nil =>
        val lst = parseItems(l)
        lst.headOption
      case KList(nodes) =>
        val chead :: ctail = parseItems(nodes)
        Some(chead.copy(children = chead.children ++ ctail))
    }
  }
}
