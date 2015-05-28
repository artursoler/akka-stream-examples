import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Tcp.{ServerBinding, IncomingConnection}
import akka.stream.scaladsl.{Flow, Tcp, Source}
import akka.util.ByteString
import scala.concurrent.Future

object TcpStream extends App {
  implicit val system = ActorSystem("SimpleStream")
  implicit val materializer = ActorFlowMaterializer()
  import system.dispatcher

  val connections: Source[IncomingConnection, Future[ServerBinding]] =
    Tcp().bind("0.0.0.0", 8888)

  connections runForeach { connection =>
    println(s"New connection from: ${connection.remoteAddress}")

    val echo = Flow[ByteString]
      .transform(() => Parser.parseLines("\r\n", maximumLineBytes = 256))
      .map(_.toInt)
      .scan(0)(_ + _)
      .map(_.toString + "\n")
      .map(ByteString(_))

    connection.handleWith(echo)
  }
}
