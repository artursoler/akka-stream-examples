import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{Sink, Source}

object SimpleStream extends App {
  implicit val system = ActorSystem("SimpleStream")
  implicit val materializer = ActorFlowMaterializer()
  import system.dispatcher

  Source(1 to 10)
    .map(x => x * x)
    .runWith(Sink.foreach(println))
    .onComplete(_ => system.shutdown())
}
