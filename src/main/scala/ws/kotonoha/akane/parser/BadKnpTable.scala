package ws.kotonoha.akane.parser

import ws.kotonoha.akane.analyzers.knp._
import ws.kotonoha.akane.pipe.knp.{OldAndUglyKnpLexeme, KnpNode}

import scala.collection.mutable

@deprecated("use protobuf-based apis", "0.3")
trait LexemeStorage {
  def lexeme(num: Int): OldAndUglyKnpLexeme
  def lexemeCnt: Int
  def lexemes(from: Int, until: Int): IndexedSeq[LexemeApi]
}

@deprecated("use protobuf-based apis", "0.3")
class ArrayLexemeStorage(data: Array[OldAndUglyKnpLexeme]) extends LexemeStorage {
  override def lexeme(num: Int) = data(num)
  override def lexemes(from: Int, until: Int) = data.slice(from, until)
  override def lexemeCnt = data.length
}

@deprecated("use protobuf-based apis", "0.3")
trait KihonkuStorage {
  def kihonku(num: Int): OldAndUglyKihonku
  def kihonkuCnt: Int
}

@deprecated("use protobuf-based apis", "0.3")
class ArrayKihonkuStorage(data: Array[OldAndUglyKihonku]) extends KihonkuStorage {
  override def kihonku(num: Int) = data(num)
  override def kihonkuCnt = data.length
}

/**
 * A unit of knp table entry that represents either a bunsetsu
 * dependency tree or a kihonku dependency tree
 * @param number number of entry
 * @param depNumber number of direct dependency
 * @param depType type of dependency
 * @param features features that are assigned to table entry
 * @param lexs lexeme storage
 */
case class OldAndUglyBunsetsu(lexs: LexemeStorage, kihs: KihonkuStorage,
                    number: Int, depNumber: Int, depType: String, features: Array[String],
                    lexemeStart: Int, lexemeCnt: Int,
                    kihonkuStart: Int, kihonkuCnt: Int)
  extends LexemeHelper with KihonkuHelper with FeatureLocation with BunsetsuApi {

  def toNode = KnpNode(number, depType, lexemeIter.map(_.asInstanceOf[OldAndUglyKnpLexeme]).toList, features.toList, Nil)

  override def toString = {
    s"Bunsetsu($number,$depNumber,$depType,[${lexemes.map(_.surface).mkString}])"
  }

  //TODO: move to key-value API
  override protected def featureSeq = features
}

case class OldAndUglyKihonku(lexs: LexemeStorage, number: Int, depNumber: Int, depType: String, features: Array[String],
                   lexemeStart: Int, lexemeCnt: Int)
  extends LexemeHelper with FeatureLocation with KihonkuApi {
  override def toString = {
    s"Kihonku($number,$depNumber,$depType,[${lexemes.map(_.surface).mkString}])"
  }

  //TODO: move to key-value API
  override protected def featureSeq = features
}

@deprecated("use protobuf-based apis", "0.3")
trait FeatureLocation extends FeatureAccess {
  //TODO: move to key-value API
  protected def featureSeq: Traversable[String]

  def valueOfFeature(name: String): Option[String] = {
    val fts = featureSeq

    val iter = fts.toIterator

    while (iter.hasNext) {
      val f = iter.next()
      if (f.startsWith(name)) {
        if (name.length == f.length) {
          return Some("")
        } else {
          return Some(f.substring(name.length + 1))
        }
      }
    }

    None
  }

  def featureExists(name: String): Boolean = {
    val code = name.##

    val fiter = featureSeq.toIterator
    while (fiter.hasNext) {
      val f = fiter.next()
      if (code == f.## && f.equals(name)) return true
    }
    return false
  }
}

@deprecated("use protobuf-based apis", "0.3")
trait LexemeHelper extends LexemeAccess {
  def lexs: LexemeStorage
  def lexemeStart: Int
  def lexemeCnt: Int
  def lexemeEnd = lexemeStart + lexemeCnt

  def lexemes = lexs.lexemes(lexemeStart, lexemeEnd)

  def lexeme(idx: Int) = {
    assert(idx >= lexemeStart)
    assert(idx < lexemeEnd)
    lexs.lexeme(idx)
  }
}


@deprecated("use protobuf-based apis", "0.3")
trait KihonkuHelper extends KihonkuAccess {
  def kihs: KihonkuStorage
  def kihonkuEnd = kihonkuStart + kihonkuCnt

  def kihonku(idx: Int) = {
    assert(idx >= kihonkuStart)
    assert(idx < kihonkuEnd)
    kihs.kihonku(idx)
  }
}

case class OldAngUglyKnpTable(info: String, lexemes: Array[OldAndUglyKnpLexeme], bunsetsuData: Array[OldAndUglyBunsetsu], kihonkuData: Array[OldAndUglyKihonku])
  extends KihonkuStorage with LexemeAccess with TableApi {

  override def lexeme(idx: Int) = lexemes(idx)

  override def lexemeStart = 0
  override def lexemeEnd = lexemes.length
  override def lexemeCnt = lexemes.length

  private def makeNode(unit: OldAndUglyBunsetsu, units: Traversable[OldAndUglyBunsetsu]): KnpNode = {
    val node = unit.toNode
    val children = units.filter(_.depNumber == unit.number)
    node.copy(children = children.map(n => makeNode(n, units)).toList)
  }

  def bunsetsuTree: KnpNode = {
    val root = bunsetsuData.find(_.depNumber == -1)
      .getOrElse(throw new NullPointerException("There is no root node in tree!"))
    makeNode(root, bunsetsuData)
  }

  def toJson: JsonKnpTable = {
    def jsonizeB(units: Array[OldAndUglyBunsetsu]): Array[JsonTableUnit] = {
      units.map { u => JsonTableUnit(u.number, u.depNumber, u.depType, u.features, u.lexemeCnt, u.kihonkuCnt)  }
    }

    def jsonizeK(units: Array[OldAndUglyKihonku]): Array[JsonTableUnit] = {
      units.map { u => JsonTableUnit(u.number, u.depNumber, u.depType, u.features, u.lexemeCnt, 0)  }
    }

    JsonKnpTable(info, lexemes, jsonizeB(bunsetsuData), jsonizeK(kihonkuData))
  }

  override def bunsetsu(idx: Int) = bunsetsuData(idx)
  override def bunsetsuStart = 0
  override def bunsetsuCnt = bunsetsuData.length
  override def bunsetsuEnd = bunsetsuData.length

  override def kihonku(num: Int) = kihonkuData.apply(num)
  override def kihonkuStart = 0
  override def kihonkuCnt = kihonkuData.length
  override def kihonkuEnd = kihonkuData.length
}


case class JsonKnpTable(info: String, lexemes: Array[OldAndUglyKnpLexeme],
                        bunsetsu: Array[JsonTableUnit], kihonku: Array[JsonTableUnit]) {
  def toModel: OldAngUglyKnpTable = {
    def normalizeK(lexs: LexemeStorage, units: Array[JsonTableUnit]) = {
      val bldr = new mutable.ArrayBuilder.ofRef[OldAndUglyKihonku]()
      var start = 0
      for (k <- units) {
        bldr += OldAndUglyKihonku(lexs, k.number, k.depNumber, k.depType, k.features, start, k.lexemes)
        start += k.lexemes
      }
      bldr.result()
    }
    def normalizeB(lexs: LexemeStorage, kis: KihonkuStorage, data: Array[JsonTableUnit]) = {
      val bldr = new mutable.ArrayBuilder.ofRef[OldAndUglyBunsetsu]()
      var lexStart = 0
      var kiStart = 0
      for (b <- data) {
        bldr += OldAndUglyBunsetsu(lexs, kis, b.number, b.depNumber, b.depType, b.features,
          lexStart, b.lexemes, kiStart, b.kihonku)
        lexStart += b.lexemes
        kiStart += b.kihonku
      }
      bldr.result()
    }
    val lexs = new ArrayLexemeStorage(lexemes)
    val kh = normalizeK(lexs, kihonku)
    val khs = new ArrayKihonkuStorage(kh)
    OldAngUglyKnpTable(info, lexemes, normalizeB(lexs, khs, bunsetsu), kh)
  }
}
case class JsonTableUnit(number: Int, depNumber: Int, depType: String, features: Array[String],
                         lexemes: Int, kihonku: Int)
