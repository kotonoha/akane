package ws.kotonoha.akane.pipe

import java.util.concurrent.atomic.AtomicInteger
import scalax.file.Path

/**
 * @author eiennohito
 * @since 2013-09-03
 */
object NamedPipes {
  val directory = Path.fromString("/tmp/kotonoha/pipes/")
  val counter = new AtomicInteger(directory.children().count(_ => true) + 1)

  def pipe() = {
    if (directory.nonExistent)
      directory.createDirectory(createParents = true)
    val path = directory / counter.addAndGet(1).toString
    val proc = new ProcessBuilder("/usr/bin/mkfifo", path.path).start()
    if (proc.waitFor() == 0)
      path.path
    else throw new RuntimeException("could not create named pipe")
  }
}
