package ws.kotonoha.akane.pipe.knp

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.lang3.StringUtils
import ws.kotonoha.akane.analyzers.knp.KihonkuApi

/**
 * http://nlp.ist.i.kyoto-u.ac.jp/index.php?KNP%2F%E6%A0%BC%E8%A7%A3%E6%9E%90%E7%B5%90%E6%9E%9C%E6%9B%B8%E5%BC%8F
 * @author eiennohito
 * @since 15/02/20
 */
case class CaseFrameInfo (wordRepr: String, kind: String, usages: Seq[CaseUsage])
/**
 * http://nlp.ist.i.kyoto-u.ac.jp/index.php?KNP%2F%E6%A0%BC%E8%A7%A3%E6%9E%90%E7%B5%90%E6%9E%9C%E6%9B%B8%E5%BC%8F
 */
case class CaseUsage(kaku: String, flag: Char, writing: String, kihonku: Int, prevSentIdx: Int, sentId: String)

object CaseFrameInfo extends StrictLogging {

  def parseUsages(caseUsages: String) = {
    val splitUsages = StringUtils.split(caseUsages, ';')
    splitUsages.view.map(s => StringUtils.split(s, '/')).collect {
        case Array(caze, kind, surface, id, prev, sent) if kind != "U" =>
          CaseUsage(caze, kind.charAt(0), surface, id.toInt, prev.toInt, sent)
    }.toIndexedSeq
  }

  def inKihonku(kihonku: KihonkuApi) = {
    kihonku.findFeature("格解析結果").flatMap(parsePredarg)
  }

  def parsePredarg(f: String): Option[CaseFrameInfo] = {
    val items = StringUtils.split(f, ':')
    items match {
      case Array(predInfo, code, data) =>
        val parsedUsages = parseUsages(data)
        Some(CaseFrameInfo(predInfo, code, parsedUsages))
      case Array(predInfo, code) =>
        Some(CaseFrameInfo(predInfo, code, Seq.empty))
      case _ =>
        logger.debug(s"could not parse case frame: $f")
        None
    }
  }
}
