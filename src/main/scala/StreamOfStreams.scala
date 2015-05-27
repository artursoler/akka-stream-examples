import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{Sink, Source}

object StreamOfStreams extends App {
  implicit val system = ActorSystem("StreamOfStreams")
  implicit val materializer = ActorFlowMaterializer()
  import system.dispatcher

  def groupConsumer(input: (Char, Source[String, Unit])) = {
    val (key, group) = input
    group.runForeach(x => println(s"key: $key element: $x"))
  }

  Source(1 to 100)
    .map(_.toString)
    .filter(_.length == 2)
    .groupBy(_.last)
    .runWith(Sink.foreach {
      case (index, source) =>
        source.runWith(Sink.fold("")(_ + " " + _)).map(result => println(s"$index: $result"))
    })
    .onComplete(_ => system.shutdown())
}
