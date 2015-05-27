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

      val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

      in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> sink
                  bcast ~> f4 ~> merge
  }

  g.run().onComplete(_ => system.shutdown())
}
