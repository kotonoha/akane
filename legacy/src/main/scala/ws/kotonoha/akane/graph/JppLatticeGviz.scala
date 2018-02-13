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

package ws.kotonoha.akane.graph

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import java.nio.file.StandardOpenOption

import ws.kotonoha.akane.analyzers.jumanpp.JppLatticeParser
import ws.kotonoha.akane.analyzers.jumanpp.wire.{Lattice, LatticeNode}
import ws.kotonoha.akane.io.Charsets
import ws.kotonoha.akane.parser.JumanPosSet

/**
  * @author eiennohito
  * @since 2016/07/15
  */
class JppLatticeGviz(writer: Appendable) {
  private def println(s: String): this.type = {
    print(s)
    println()
    this
  }

  private def println(): this.type = {
    writer.append("\n")
    0.until(level).foreach(_ => writer.append(" "))
    this
  }

  private def print(s: String): this.type = {
    writer.append(s)
    this
  }

  var level = 0
  val increase = 2

  def indent() = level += increase
  def outdent() = level -= increase

  def renderEdges(nodes: Seq[LatticeNode]) = {
    val byId = nodes.map(n => n.nodeId -> n).toMap

    nodes.foreach { n =>
      val id = n.nodeId
      n.prevNodes.foreach { prev =>
        val left = byId.get(prev)
        val lid = s"n_$prev"
        val rid = s"n_$id"
        val common = left match {
          case None    => n.rank.toSet
          case Some(l) => l.rank.toSet.intersect(n.rank.toSet)
        }

        val sorted = common.toSeq.sorted
        val least = sorted.headOption
        val color = least match {
          case Some(1) => "red"
          case Some(2) => "blue"
          case Some(3) => "green"
          case _       => "black"
        }

        val label = sorted.mkString(",")

        println(s"""$lid -> $rid [color=$color, label="$label"]""")
      }
    }
  }

  val info = JumanPosSet.default

  def renderNodes(nodes: Seq[LatticeNode]) = {
    nodes.foreach { n =>
      val id = n.nodeId
      val ids = s"n_$id"
      val pos = info.pos(n.pos.pos)
      val sub = pos.subtypes(n.pos.subpos)
      val pform = n.pos.subpos match {
        case 0 => info.conjugatons(n.pos.category).conjugations(n.pos.conjugation).name
        case _ => sub.name
      }

      val repr =
        s"""label=<<table><tr><td border="0">${n.surface}</td><td border="0">${n.canonic}</td></tr><tr><td border="0">${pos.name}</td><td border="0">$pform</td></tr></table>>"""
      println(s"$ids [$repr]")
    }
  }

  def render(l: Lattice) = {
    println("digraph lattice {")
    indent()
    println("rankdir=LR")
    println("node [shape=plaintext]")
    renderEdges(l.nodes)
    println("n_0 [ label=\"BOS\" ]")
    renderNodes(l.nodes)
    outdent()
    println("}")
  }
}

object Jpp2Gviz {
  import ws.kotonoha.akane.resources.FSPaths._
  def main(args: Array[String]): Unit = {
    val file = args(0).p
    for (is <- file.inputStream) {
      val rdr = new BufferedReader(new InputStreamReader(is, Charsets.utf8))
      val prs = new JppLatticeParser
      val lattice = prs.parse(rdr)

      for (out <- (args(0) + ".dot").p
             .outputStream(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
        val writer = new OutputStreamWriter(out, Charsets.utf8)
        val prr = new JppLatticeGviz(writer)
        prr.render(lattice)
        writer.flush()
      }
    }
  }
}
