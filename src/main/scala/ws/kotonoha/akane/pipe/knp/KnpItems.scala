package ws.kotonoha.akane.pipe.knp

/**
 * @author eiennohito
 * @since 2013-09-04
 */
case class PosItem(name: String, id: Int)

case class JumanPosInfo(pos: PosItem, category: PosItem, conjType: PosItem, conjForm: PosItem)

//かわったり かわったり かわる 動詞 2 * 0 子音動詞ラ行 10 タ系連用タリ形 15 "代表表記:代わる/かわる 自他動詞:他:代える/かえる"
case class KnpLexeme(
                      surface: String,
                      reading: String,
                      dicForm: String,
                      pos: JumanPosInfo,
                      info: String,
                      tags: List[String])

case class KnpItemRelation(to: Int, tags: List[String])

case class KnpItem(num: Int, star: KnpItemRelation, plus: KnpItemRelation, lexems: Seq[KnpLexeme])

case class KnpNode(num: Int, kind: String, surface: List[KnpLexeme], features: List[String], children: List[KnpNode] = Nil)
