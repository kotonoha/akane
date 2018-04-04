package ws.kotonoha.akane.analyzers.jumanpp.grpc

import java.io.{BufferedReader, Closeable, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

import org.slf4j.LoggerFactory

import scala.concurrent._
import scala.util.Try

case class JumanppGrpcConfig(
  executable: Path,
  config: Path,
  numThreads: Int
)


object JumanppGrpcProcess {

  /**
    * Read a port number from Juman++ gRPC process input stream.
    * This function is unsafe because it can block forever if there is no input on stdin.
    * @param proc
    * @return
    */
  def unsafeReadPort(proc: Process): Try[Int] = {
    val stdin = proc.getInputStream
    var port = 0
    val continue = true
    try {
      while (continue) {
        stdin.read() match {
          case -1 => return scala.util.Failure(new Exception("unexpected eof"))
          case '\n' => return scala.util.Success(port)
          case x if x >= '0' && x <= '9' =>
            port *= 10
            port += (x - '0')
          case x => return scala.util.Failure(new Exception(s"unexpected char=$x, curport=$port"))
        }
      }
    } catch {
      case e: Exception => return scala.util.Failure(e)
    }
    throw new Exception("unreachable")
  }

  /**
    * Read the port in another thread on the execution context,
    * handle the indefinite blocking with timeouts.
    * @param proc
    * @param ec
    * @return
    */
  def safeReadPort(proc: Process)(implicit ec: ExecutionContext): Try[Int] = {
    val p = Promise[Int]

    ec.execute(new Runnable {
      override def run(): Unit = {
        p.complete(unsafeReadPort(proc))
      }
    })

    try {
      val future = p.future
      import scala.concurrent.duration._
      Await.ready(future, 1.second)
      future.value.get
    } catch {
      case e: TimeoutException =>
        proc.destroyForcibly()
        scala.util.Failure(e)
      case e: Throwable =>
        proc.destroyForcibly()
        throw e
    }
  }

  private val counter = new AtomicInteger()

  def spawnProcess(config: JumanppGrpcConfig): Process = {
    val cmd = new java.util.ArrayList[String]
    cmd.add(config.executable.toString)
    cmd.add(s"--config=${config.config}")
    cmd.add(s"--threads=${config.numThreads}")
    cmd.add("--port=0")

    val pb = new ProcessBuilder(cmd)
    pb.start()
  }

  def spawn(config: JumanppGrpcConfig)(implicit ec: ExecutionContext): JumanppGrpcProcess = {
    val proc = spawnProcess(config)
    try {
      val port = safeReadPort(proc)
      new JumanppGrpcProcess(proc, port.get)
    } catch {
      case e: Throwable =>
        proc.destroyForcibly()
        throw e
    }
  }
}

class JumanppGrpcProcess(process: Process, val port: Int) extends Closeable {
  private val id = JumanppGrpcProcess.counter.getAndIncrement()
  private val stdout = process.getInputStream
  private val stderr = process.getErrorStream

  private val stderrThread = new Thread(s"jpp-grpc-stderr-$id") {
    this.setDaemon(true)
    override def run(): Unit = {
      val reader = new InputStreamReader(stderr, StandardCharsets.UTF_8)
      val buffReader = new BufferedReader(reader)
      val logger = LoggerFactory.getLogger("ws.kotonoha.akane.analyzers.jumanpp.grpc.process.Stderr")

      var line = ""
      while ({
        line = buffReader.readLine()
        line != null
      }) {
        logger.warn(line)
      }
    }
  }

  private val stdoutThread = new Thread(s"jpp-grpc-stdout-$id") {
    this.setDaemon(true)

    override def run(): Unit = {
      val reader = new InputStreamReader(stderr, StandardCharsets.UTF_8)
      val buffReader = new BufferedReader(reader)
      val logger = LoggerFactory.getLogger("ws.kotonoha.akane.analyzers.jumanpp.grpc.process.Stdout")

      var line = ""
      while ({
        line = buffReader.readLine()
        line != null
      }) {
        logger.warn(line)
      }
    }
  }

  override def close(): Unit = {
    process.destroyForcibly()
  }
}
