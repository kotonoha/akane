package ru.dijkstra.ranobe_parser
import java.io._
import scalax.file.Path
import java.nio.{BufferUnderflowException, ByteBuffer}
import annotation.tailrec
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.util.Scanner

object Application {
  def main(args: Array[String]): Unit = {
    var fileopen = new JFileChooser();
    fileopen.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
    val ret = fileopen.showDialog(null, "Open File");
    if (ret == JFileChooser.APPROVE_OPTION) {
      if (!fileopen.getSelectedFile.exists()) throw new Exception("Error: No such file")
      val sb = new StringBuilder()
      val reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileopen.getSelectedFile), "UTF8"))
      var line : String = null
      var end = false
      do {
        line = reader.readLine()
        if (line == null) end = true else {
        sb.append(line)
        sb.append('\n')                    }
      } while(!end)
      var str = sb.toString()
      System.gc()
      //print(str)
      import ru.dijkstra.ranobe_parser.old._
      val tokens = Tokenizer.tokenize(str)
      //print(tokens)
      val nodes = Parser.parse(tokens)
      //print(nodes)
      val html = html5render.render(nodes)
      //print(html)
      val fw = new FileOutputStream(fileopen.getSelectedFile.getAbsolutePath ++ ".html")
      fw.write(html.getBytes("UTF8"))
      fw.close()
      reader.close()
    }
  }
}
