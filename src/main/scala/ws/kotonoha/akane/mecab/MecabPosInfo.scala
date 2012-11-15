/*
 * Copyright 2012 eiennohito
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

package ws.kotonoha.akane.mecab

/**
 * @author eiennohito
 * @since 14.11.12 
 */
case class MecabPosInfo (pos: String, cat: String, i1: String, i2: String, conjClass: String, conjForm: String)

object MecabPosInfo {
  def apply(args: String*) = {
    args match {
      case Seq(a1, a2, a3, a4, a5, a6) => new MecabPosInfo(a1, a2, a3, a4, a5, a6)
      case _ => throw new RuntimeException("Invalid arg count")
    }
  }
}

trait MecabEntryInfo {
  def pos: MecabPosInfo
  def surface: String
  def dicForm: String
  def dicWriting: Option[String]
  def dicReading: Option[String]
  def safeWriting = dicWriting getOrElse dicForm
  def reading: String
  def normReading: String
}

object MVerb {
  def unapply(en: MecabEntryInfo) = {
    en.pos match {
      case MecabPosInfo("動詞", _, _, _, _, _) => Some(en)
      case _ => None
    }
  }
}
