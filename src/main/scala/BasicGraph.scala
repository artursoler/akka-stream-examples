import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl._

object BasicGraph extends App {
  implicit val system = ActorSystem("BasicGraph")
  implicit val materializer = ActorFlowMaterializer()
  import system.dispatcher

  val out = Sink.foreach(println)

  val g = FlowGraph.closed(out) { implicit builder =>
    sink =>
      import FlowGraph.Implicits._
      val in = Source(1 to 10)

      val bcast = builder.add(Broadcast[Int](2))
      val merge = builder.add(Merge[Int](2))

      val f = Flow[Int].map(_ + 10)

      in ~> f ~> bcast ~> f ~> merge ~> f ~> sink
                 bcast ~> f ~> merge
  }

  g.run().onComplete(_ => system.shutdown())
}
