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

package ws.kotonoha.akane.dic.lucene.jmdict

import org.apache.lucene.index._
import org.apache.lucene.store.RAMDirectory
import org.joda.time.LocalDate
import org.scalatest.{FreeSpec, Matchers}
import ws.kotonoha.akane.dic.jmdict.JmdictParser
import ws.kotonoha.akane.resources.Classpath

import scala.concurrent.ExecutionContext

/**
  * @author eiennohito
  * @since 2017/02/16
  */
class JmdictImporterSpec extends FreeSpec with Matchers {
  "JMDict Lucene importer" - {
    "imports usable jmdict" in {
      val lucDir = new RAMDirectory()
      val lucWr = new IndexWriter(lucDir, new IndexWriterConfig())

      for (is <- Classpath.inputStream("jmdict_3ents.xml")) {
        val jmd = new JmdictParser().parse(is)
        val importer = new LuceneImporter(lucWr)
        jmd.foreach { e => importer.add(e) }
        importer.commit(LocalDate.now())
      }

      val rdr = DirectoryReader.open(lucDir)
      val data = rdr.getIndexCommit.getUserData
      val linfo = LuceneImporter.parseUserData(data)
      val jmd = new LuceneJmdictImpl(rdr, ExecutionContext.global, linfo)

      val res = jmd.find(JmdictQuery(limit = 1, other=JmdictQueryPart("*") :: Nil, langs = Seq("rus")))
      res.data should not be empty

      linfo.langs.freqs should have length (8)
    }
  }
}
