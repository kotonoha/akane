package ru.dijkstra

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FreeSpec
import scalax.file.Path

class SimpleTest extends FreeSpec with ShouldMatchers {
  "This test" - {
    "at least runs" in {
      println("It runs")
    }

    "and sometimes fails" in {
      throw new Exception("My fail, sorry!")
    }

    "and even lists your root dir" in {
      val f = Path("/")
      f.descendants(depth = 1) foreach { fl => println(fl.toAbsolute.path) }
    }
  }
}
