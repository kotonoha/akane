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

import ws.kotonoha.akane.analyzers.juman.{JumanText, JumanPos, JumanLexeme, JumanOption}
import ws.kotonoha.akane.analyzers.knp.wire.{Kihonku, Bunsetsu, KnpTable}
import ws.kotonoha.akane.analyzers.knp.raw.{OldAngUglyKnpTable => OldTable, OldAndUglyBunsetsu => OldBunsetsu, OldAndUglyKihonku => OldKihonku}
import ws.kotonoha.akane.analyzers.knp.raw.OldAndUglyKnpLexeme

/**
 * @author eiennohito
 * @since 2015/09/18
 */
//noinspection ScalaDeprecation
class TableConverter(filterLexeme: Set[String], filterBunsetsu: Set[String], filterKihonku: Set[String]) {
  final def transformFeatures(features: Seq[String], filter: Set[String]): Seq[JumanOption] = {
    if (filter.isEmpty) {
      transformWoFilter(features)
    } else {
      transformWithFilter(features, filter)
    }
  }

  final def transformWithFilter(features: Seq[String], filter: Set[String]): Seq[JumanOption] = {
    features.flatMap { f =>
      val semi = f.indexOf(':')
      val key = if (semi == -1) f else f.substring(0, semi)
      if (filter.contains(key))
        Seq(if (semi == -1) JumanOption(key, None) else JumanOption(key, Some(f.substring(semi + 1, f.length))))
      else Seq.empty
    }
  }

  final def transformWoFilter(features: Seq[String]): Seq[JumanOption] = {
    features.map { f =>
      val semi = f.indexOf(':')
      if (semi == -1) JumanOption(f, None)
      else JumanOption(f.substring(0, semi), Some(f.substring(semi + 1, f.length)))
    }
  }

  final def fromBunsetsu(bunsetsu: OldBunsetsu): Bunsetsu = {
    Bunsetsu(
      dependency = bunsetsu.depNumber,
      dependencyType = bunsetsu.depType,
      features = transformFeatures(bunsetsu.features, filterBunsetsu),
      kihonkuCnt = bunsetsu.kihonkuCnt
    )
  }

  final def fromKihonku(kihonku: OldKihonku): Kihonku = {
    Kihonku(
      dependency = kihonku.depNumber,
      dependencyType = kihonku.depType,
      features = transformFeatures(kihonku.features, filterKihonku),
      lexemeCnt = kihonku.lexemeCnt
    )
  }

  final def fromLexeme(lexeme: OldAndUglyKnpLexeme): JumanLexeme = {
    JumanLexeme(
      surface = lexeme.surface,
      reading = lexeme.reading,
      baseform = lexeme.dicForm,
      posInfo = JumanPos(
        pos = lexeme.pos.pos,
        subpos = lexeme.pos.subpos,
        category = lexeme.pos.category,
        conjugation = lexeme.pos.conjugation
      ),
      options = (JumanText.parseOptionsInner(lexeme.info, 0, lexeme.info.length) ++ transformFeatures(lexeme.tags, filterLexeme)).distinct
    )
  }

  final def fromOld(tab: OldTable): KnpTable = {
    val lexemes = tab.lexemes.map(fromLexeme)
    val bunsetsuData = tab.bunsetsuData.map(fromBunsetsu)
    val kihonkuData = tab.kihonkuData.map(fromKihonku)
    KnpTable(
      comment = if (tab.info != null && tab.info.length > 1) Some(tab.info) else None,
      lexemes = lexemes,
      kihonkuInfo = kihonkuData,
      bunsetsuInfo = bunsetsuData
    )
  }
}

object TableConverter extends TableConverter(Set(), Set(), Set())
