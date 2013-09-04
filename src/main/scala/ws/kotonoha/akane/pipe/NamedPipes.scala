package ws.kotonoha.akane.pipe

import java.util.concurrent.atomic.AtomicInteger
import scalax.file.Path
import java.io.Closeable
import org.bridj.Pointer
import windows.Kernel32

/**
 * @author eiennohito
 * @since 2013-09-03
 */
object NamedPipes {
  lazy val piper = System.getProperty("os.name") match {
    case n if n.contains("Windows") => WindowsNamedPipes
    case _ => UnixPipeGenerator
  }

  def pipe() = piper.apply()
}

object UnixPipeGenerator extends PipeGenerator{
  val directory = Path.fromString("/tmp/kotonoha/pipes/")
  val counter = new AtomicInteger(directory.children().count(_ => true) + 1)

  def apply() = {
    if (directory.nonExistent)
      directory.createDirectory(createParents = true)
    val path = directory / counter.addAndGet(1).toString
    val proc = new ProcessBuilder("/usr/bin/mkfifo", path.path).start()
    if (proc.waitFor() == 0)
      UnixPipe(path.path)
    else throw new RuntimeException("could not create named pipe")
  }
}

trait PipeGenerator {
  def apply(): Pipe
}

trait Pipe extends Closeable {
  def name: String
}

case class UnixPipe(name: String) extends Pipe {
  def close() { Path.fromString(name).delete() }
}

case class WindowsPipe(name: String, handle: Long) extends Pipe {
  def close() {
    Kernel32.DisconnectNamedPipe(handle)
  }
}

object WindowsNamedPipes extends PipeGenerator {
  val directory = """\\.\pipe\"""
  val name = "akane"
  val pid = getClass.getClassLoader.hashCode()
  val counter = new AtomicInteger(0)
  val time = System.currentTimeMillis()

  def apply() = {
    ???
    val pipename = s"$name-$time-$pid-${counter.getAndAdd(1)}"
    val fullname = directory + pipename
    val ptr = Pointer.pointerToCString(fullname)
    val data = Kernel32.CreateNamedPipeA(ptr, 3, 0, 255, 4096, 4096, 5000, Pointer.NULL)
    fullname
    WindowsPipe(fullname, data)
  }
}
