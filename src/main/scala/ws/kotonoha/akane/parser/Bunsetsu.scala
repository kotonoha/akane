package ws.kotonoha.akane.parser

import ws.kotonoha.akane.pipe.knp.{KnpNode, KnpLexeme}

import scala.collection.mutable


trait LexemeStorage {
  def lexeme(num: Int): KnpLexeme
  def lexemeCnt: Int
  def lexemes(from: Int, until: Int): IndexedSeq[KnpLexeme]
}

class ArrayLexemeStorage(data: Array[KnpLexeme]) extends LexemeStorage {
  override def lexeme(num: Int) = data(num)
  override def lexemes(from: Int, until: Int) = data.slice(from, until)
  override def lexemeCnt = data.length
}

trait KihonkuStorage {
  def kihonku(num: Int): Kihonku
  def kihonkuCnt: Int
}

class ArrayKihonkuStorage(data: Array[Kihonku]) extends KihonkuStorage {
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
case class Bunsetsu(lexs: LexemeStorage, kihs: KihonkuStorage,
                    number: Int, depNumber: Int, depType: String, features: Array[String],
                    lexemeStart: Int, lexemeCnt: Int,
                    kihonkuStart: Int, kihonkuCnt: Int) extends LexemeHelper with KihonkuHelper with FeatureLocation {

  def toNode = KnpNode(number, depType, lexemes.toList, features.toList, Nil)

  override def toString = {
    s"Bunsetsu($number,$depNumber,$depType,[${lexemes.map(_.surface).mkString}])"
  }
}

case class Kihonku(lexs: LexemeStorage, number: Int, depNumber: Int, depType: String, features: Array[String],
                   lexemeStart: Int, lexemeCnt: Int) extends LexemeHelper with FeatureLocation {
  override def toString = {
    s"Kihonku($number,$depNumber,$depType,[${lexemes.map(_.surface).mkString}])"
  }
}

trait FeatureLocation {
  def features: Array[String]

  def findFeature(name: String): Option[String] = {
    val fs = features
    var i = 0
    val end = fs.length

    while (i < end) {
      val f = fs(i)
      if (f.startsWith(name)) {
        if (name.length == f.length) {
          return Some("")
        } else {
          return Some(f.substring(name.length + 1))
        }
      }
      i += 1
    }
    None
  }

  def noParamFeatureExists(name: String): Boolean = {
    val fs = features
    var i = 0
    val end = fs.length

    val code = name.##

    while (i < end) {
      val f = fs(i)
      if (code == f.## && f.equals(name)) {
        return true
      }
      i += 1
    }
    false
  }
}

trait LexemeAccess {
  def lexeme(idx: Int): KnpLexeme
  def lexemeStart: Int
  def lexemeEnd: Int
  def lexemeCnt: Int

  def lexemeIter: Iterator[KnpLexeme] = new Iterator[KnpLexeme] {
    private var pos = lexemeStart
    override def hasNext = pos < lexemeEnd
    override def next() = {
      val l = lexeme(pos)
      pos += 1
      l
    }
  }

  def charLength = {
    var i = lexemeStart
    val e = lexemeEnd
    var len = 0
    while (i < e) {
      val l = lexeme(i)
      len += l.surface.length
      i += 1
    }
    len
  }
}

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

trait KihonkuAccess {
  def kihonku(idx: Int): Kihonku
  def kihonkuStart: Int
  def kihonkuCnt: Int
  def kihonkuEnd: Int

  def kihonkuIter: Iterator[Kihonku] = new Iterator[Kihonku] {
    private var pos = kihonkuStart
    override def hasNext = pos < kihonkuEnd
    override def next() = {
      val k = kihonku(pos)
      pos += 1
      k
    }
  }
}

trait KihonkuHelper extends KihonkuAccess {
  def kihs: KihonkuStorage
  def kihonkuEnd = kihonkuStart + kihonkuCnt

  def kihonku(idx: Int) = {
    assert(idx >= kihonkuStart)
    assert(idx < kihonkuEnd)
    kihs.kihonku(idx)
  }
}

case class KnpTable(info: KnpInfo, lexemes: Array[KnpLexeme], bunsetsu: Array[Bunsetsu], kihonkuData: Array[Kihonku]) extends KihonkuStorage {

  private def makeNode(unit: Bunsetsu, units: Traversable[Bunsetsu]): KnpNode = {
    val node = unit.toNode
    val children = units.filter(_.depNumber == unit.number)
    node.copy(children = children.map(n => makeNode(n, units)).toList)
  }

  def bunsetsuTree: KnpNode = {
    val root = bunsetsu.find(_.depNumber == -1)
      .getOrElse(throw new NullPointerException("There is no root node in tree!"))
    makeNode(root, bunsetsu)
  }

  def toJson: JsonKnpTable = {
    def jsonizeB(units: Array[Bunsetsu]): Array[JsonTableUnit] = {
      units.map { u => JsonTableUnit(u.number, u.depNumber, u.depType, u.features, u.lexemeCnt, u.kihonkuCnt)  }
    }

    def jsonizeK(units: Array[Kihonku]): Array[JsonTableUnit] = {
      units.map { u => JsonTableUnit(u.number, u.depNumber, u.depType, u.features, u.lexemeCnt, 0)  }
    }

    JsonKnpTable(info, lexemes, jsonizeB(bunsetsu), jsonizeK(kihonkuData))
  }

  def kihonkuIdxForSurface(pos: Int): Int = {
    var i = 0
    var cnt = 0
    val blen = kihonkuCnt
    while (i < blen) {
      val b = kihonku(i)
      var j = b.lexemeStart
      val jend = b.lexemeEnd

      while (j < jend) {
        val lex = lexemes(j)
        cnt += lex.surface.length
        if (cnt > pos)
          return i
        j += 1
      }

      i += 1
    }
    return -1
  }

  /**
   * Transforms kihonku scope to bunsetsu scope
   * @param kihonkuScope sorted array of kihonku indexes
   * @return array of bunsetsu indices
   */
  def bunsetsuScope(kihonkuScope: Array[Int]): Array[Int] = {
    val indices = new mutable.BitSet()

    var curKih = 0

    var i = 0
    var cnt = 0
    val blen = bunsetsu.length

    while (i < blen) {
      val bnst = bunsetsu(i)

      cnt += bnst.kihonkuCnt

      while (curKih < kihonkuScope.length &&
             kihonkuScope(curKih) < cnt) {
        indices += i
        curKih += 1
      }

      i += 1
    }

    indices.toArray
  }

  def bunsetsuIdxForKihonku(kih: Int): Int = {

    var i = 0
    var cnt = 0
    val blen = bunsetsu.length

    while (i < blen) {
      val bnst = bunsetsu(i)

      cnt += bnst.kihonkuCnt

      if (cnt > kih) return i

      i += 1
    }

    -1
  }

  override def kihonku(num: Int) = kihonkuData.apply(num)

  override def kihonkuCnt = kihonkuData.length
}


case class JsonKnpTable(info: KnpInfo, lexemes: Array[KnpLexeme],
                        bunsetsu: Array[JsonTableUnit], kihonku: Array[JsonTableUnit]) {
  def toModel: KnpTable = {
    def normalizeK(lexs: LexemeStorage, units: Array[JsonTableUnit]) = {
      val bldr = new mutable.ArrayBuilder.ofRef[Kihonku]()
      var start = 0
      for (k <- units) {
        bldr += Kihonku(lexs, k.number, k.depNumber, k.depType, k.features, start, k.lexemes)
        start += k.lexemes
      }
      bldr.result()
    }
    def normalizeB(lexs: LexemeStorage, kis: KihonkuStorage, data: Array[JsonTableUnit]) = {
      val bldr = new mutable.ArrayBuilder.ofRef[Bunsetsu]()
      var lexStart = 0
      var kiStart = 0
      for (b <- data) {
        bldr += Bunsetsu(lexs, kis, b.number, b.depNumber, b.depType, b.features,
          lexStart, b.lexemes, kiStart, b.kihonku)
        lexStart += b.lexemes
        kiStart += b.kihonku
      }
      bldr.result()
    }
    val lexs = new ArrayLexemeStorage(lexemes)
    val kh = normalizeK(lexs, kihonku)
    val khs = new ArrayKihonkuStorage(kh)
    KnpTable(info, lexemes, normalizeB(lexs, khs, bunsetsu), kh)
  }
}
case class JsonTableUnit(number: Int, depNumber: Int, depType: String, features: Array[String],
                         lexemes: Int, kihonku: Int)
