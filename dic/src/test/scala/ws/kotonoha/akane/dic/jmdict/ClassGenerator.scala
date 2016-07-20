/*
 * Copyright 2016 eiennohito (Tolmachev Arseny)
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

package ws.kotonoha.akane.dic.jmdict

import ws.kotonoha.akane.resources.{Classpath, FSPaths}

/**
  * @author eiennohito
  * @since 2016/07/20
  */
case class Entity(name: String, comment: String)

object ClassGenerator {
  import ws.kotonoha.akane.resources.FSPaths._


  val entityRe = """[^<]*<!ENTITY ([^ ]+) "([^"]+)">""".r

  val nameOverrides = Map(
    "oK" -> "outKanji",
    "ok" -> "outKana",
    "int" -> "interj",
    "uK" -> "useKanji",
    "uk" -> "useKana",
    "eK" -> "exclKanj",
    "ek" -> "exclKana",
    "ik" -> "irrKana",
    "iK" -> "irrKanji"
  )

  def main(args: Array[String]): Unit = {
    val root = args(0).p

    val dtd = Classpath.lines("jmdict.dtd")
    val entities = dtd.flatMap {
      case entityRe(title, explanation) =>
        List(Entity(title, explanation))
      case _ => Nil
    }

    val main = root / "src" / "main"
    val pbuf = main / "protobuf" / "jmdict_tags.proto"
    pbuf.write(generateEnumProtobuf(entities))
    FSPaths.ensureParent(pbuf)

    val mapping = main / "scala" / "ws" / "kotonoha" / "akane" / "dic" / "jmdict" / "JmdictTagMap.scala"
    FSPaths.ensureParent(mapping)
    mapping.write(generateMapping(entities))
  }

  def camelCase(in: String, out: java.lang.StringBuilder): Unit = {
    var i = 0
    val end = in.length
    var needUp = false
    while (i < end) {
      var c = in.codePointAt(i)
      if (c == '-') {
        needUp = true
        i += 1
      } else {
        if (needUp) {
          c = Character.toUpperCase(c)
          needUp = false
        }
        out.appendCodePoint(c)
        i += (if (Character.isBmpCodePoint(c)) 1 else 2)
      }
    }
  }

  def generateMapping(entities: Seq[Entity]) = {
    val buffer = new java.lang.StringBuilder(1000)

    buffer.append(
      """
        |package ws.kotonoha.akane.dic.jmdict
        |
        |
        |object JmdictTagMap {
        |  val tagMap = Map[String, JmdictTag](
        |""".stripMargin)

    for (e <- entities) {
      buffer.append("    //").append(e.comment).append("\n")
      buffer.append("    ").append('"').append(e.name).append('"')
        .append(" -> ").append("JmdictTag.")

      val tf = nameOverrides.getOrElse(e.name, e.name)
      camelCase(tf, buffer)

      buffer.append(",\n")
    }

    buffer.replace(buffer.length() - 2, buffer.length(), "\n")

    buffer.append("  )\n}\n")
  }


  def generateEnumProtobuf(entities: Seq[Entity]) = {
    val buffer = new java.lang.StringBuilder(1000)

    buffer.append(
      """
        |syntax = "proto2";
        |package jmdict;
        |option java_package = "ws.kotonoha.akane.dic.jmdict";
        |""".stripMargin)

    buffer.append("\n\nenum JmdictTag {\n")


    var seq = 0
    for (e <- entities) {
      buffer.append("  //")
      buffer.append(e.name).append(" : ").append(e.comment)
      buffer.append("\n  ")
      val tfed = nameOverrides.getOrElse(e.name, e.name)
      camelCase(tfed, buffer)
      buffer.append(" = ").append(seq).append(";\n\n")
      seq += 1
    }

    buffer.append("}\n")

    buffer.toString
  }
}
