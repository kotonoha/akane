/*
 * Copyright 2012-2016 eiennohito (Tolmachev Arseny)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ws.kotonoha.akane.analyzers.knp

import ws.eiennohito.nlp.util.ArrayLexemeAccess
import ws.eiennohito.utils.Foreach
import ws.kotonoha.akane.analyzers.juman.{JumanLexeme, JumanOption}
import ws.kotonoha.akane.analyzers.knp.wire.{Bunsetsu, Kihonku, KnpTable}

import scala.collection.mutable.ArrayBuffer

/**
  * @author eiennohito
  * @since 2015/09/18
  */
final class TableWrapper(
    val message: KnpTable,
    lexemes: LexemeAccess,
    bunsetsus: BunsetsuAccess,
    kihonkus: KihonkuAccess)
    extends TableApi {
  override def kihonkuStart = kihonkus.kihonkuStart
  override def kihonkuEnd = kihonkus.kihonkuEnd
  override def kihonkuCnt = kihonkus.kihonkuCnt
  override def kihonku(idx: Int) = kihonkus.kihonku(idx)

  override def bunsetsuStart = bunsetsus.bunsetsuStart
  override def bunsetsuEnd = bunsetsus.bunsetsuEnd
  override def bunsetsuCnt = bunsetsus.bunsetsuCnt
  override def bunsetsu(idx: Int) = bunsetsus.bunsetsu(idx)

  override def lexemeStart = lexemes.lexemeStart
  override def lexemeEnd = lexemes.lexemeEnd
  override def lexemeCnt = lexemes.lexemeCnt
  override def lexeme(idx: Int) = lexemes.lexeme(idx)

  override def toString = lexemeIter.map(_.surface).mkString
}

final class KihonkuAccessImpl(kihs: IndexedSeq[KihonkuApi]) extends KihonkuAccess {
  override def kihonku(idx: Int) = kihs(idx)
  override def kihonkuEnd = kihs.length
  override def kihonkuCnt = kihs.length
  override def kihonkuStart = 0
}

final class BunsetsuAccessImpl(bnsts: IndexedSeq[BunsetsuApi]) extends BunsetsuAccess {
  override def bunsetsu(idx: Int) = bnsts(idx)
  override def bunsetsuCnt = bnsts.length
  override def bunsetsuEnd = bnsts.length
  override def bunsetsuStart = 0
}

object TableWrapper {
  def makeLexemes(table: KnpTable): LexemeAccess = {
    val indLexs = table.lexemes.view.map(l => new LexemeWrapper(l)).to[ArrayBuffer]
    ArrayLexemeAccess.wrapIndSeq(indLexs)
  }

  def makeKihonku(table: KnpTable, lacc: LexemeAccess): KihonkuAccess = {
    val arbuf = new ArrayBuffer[KihonkuApi]()
    val wireKihs = table.kihonkuInfo
    var lexStart = 0
    Foreach.fori(0, wireKihs.length) { i =>
      val wk = wireKihs(i)
      val lexCnt = wk.lexemeCnt
      val kih = new KihonkuWrapper(i, wk, lacc, lexStart, lexCnt)
      arbuf += kih
      lexStart += lexCnt
    }
    new KihonkuAccessImpl(arbuf)
  }

  def makeBunsetsu(table: KnpTable, lexemes: LexemeAccess, kihs: KihonkuAccess) = {
    val arbuf = new ArrayBuffer[BunsetsuApi]()
    val wireBnsts = table.bunsetsuInfo
    var lexStart = 0
    var kihStart = 0
    Foreach.fori(0, wireBnsts.length) { i =>
      val wb = wireBnsts(i)
      val kihCnt = wb.kihonkuCnt

      var lexCnt = 0
      Foreach.fori(kihStart, kihStart + kihCnt) { kidx =>
        val k = kihs.kihonku(kidx)
        lexCnt += k.lexemeCnt
      }

      val bnst = new BunsetsuWrapper(i, wb, lexemes, lexStart, lexCnt, kihs, kihStart, kihCnt)
      lexStart += lexCnt
      kihStart += kihCnt
      arbuf += bnst
    }
    new BunsetsuAccessImpl(arbuf)
  }

  def wrap(table: KnpTable): TableWrapper = {
    val lexemes = makeLexemes(table)
    val kihs = makeKihonku(table, lexemes)
    val bnsts = makeBunsetsu(table, lexemes, kihs)
    new TableWrapper(table, lexemes, bnsts, kihs)
  }
}

trait OptionFeatures extends FeatureAccess {
  override def valueOfFeature(name: String) = optionSequence.find(_.key == name).flatMap(_.value)
  override def featureExists(name: String) = optionSequence.exists(_.key == name)
  protected def optionSequence: Seq[JumanOption]
  override def featureKeys = optionSequence.map(_.key)
}

final class LexemeWrapper(lex: JumanLexeme) extends LexemeApi with OptionFeatures {
  override def surface = lex.surface
  override def reading = lex.reading
  override def dicForm = lex.baseform
  override def pos = lex.posInfo
  override def canonicForm() = valueOfFeature("代表表記").getOrElse(s"$dicForm/$reading")
  override protected def optionSequence = lex.options

  override def toString = surface
}

final class KihonkuWrapper(
    val number: Int,
    kih: Kihonku,
    out: LexemeAccess,
    val lexemeStart: Int,
    val lexemeCnt: Int)
    extends KihonkuApi
    with OptionFeatures {

  val lexemeEnd = lexemeStart + lexemeCnt
  override protected def optionSequence = kih.features
  override def depNumber = kih.dependency
  override def depType = kih.dependencyType
  override def lexeme(idx: Int) = {
    assert(idx >= lexemeStart)
    assert(idx < lexemeEnd)
    out.lexeme(idx)
  }

  override def toString = lexemeIter.map(_.surface).mkString
}

final class BunsetsuWrapper(
    val number: Int,
    bnst: Bunsetsu,
    lexemes: LexemeAccess,
    val lexemeStart: Int,
    val lexemeCnt: Int,
    kihonkus: KihonkuAccess,
    val kihonkuStart: Int,
    val kihonkuCnt: Int
) extends BunsetsuApi
    with OptionFeatures {
  val lexemeEnd = lexemeStart + lexemeCnt
  val kihonkuEnd = kihonkuStart + kihonkuCnt

  override def depNumber = bnst.dependency
  override def depType = bnst.dependencyType

  override protected def optionSequence = bnst.features

  override def kihonku(idx: Int) = {
    assert(idx >= kihonkuStart)
    assert(idx < kihonkuEnd)
    kihonkus.kihonku(idx)
  }

  override def lexeme(idx: Int) = {
    assert(idx >= lexemeStart)
    assert(idx < lexemeEnd)
    lexemes.lexeme(idx)
  }

  override def toString = lexemeIter.map(_.surface).mkString
}
