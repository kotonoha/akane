package ru.dijkstra.ranobe_parser

import akka.actor._
import java.io.{FileInputStream, InputStreamReader, BufferedReader}
      /*
class FileReaderActor(filename: String, pipeOutput: Actor) extends Actor {
  val reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"))
  protected def receive = {
    case ProcessFile => {
      // todo: rewrite in non-imperative fashion
      var line : String = null
      var end = false
      do {
        line = reader.readLine()
        //todo: fix this part
        if (line == null) end = true else pipeOutput ! ProcessLine(line)
      } while(!end)
      reader.close()
    }
  }
}                   */