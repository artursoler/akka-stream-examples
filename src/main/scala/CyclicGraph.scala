import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl._

object CyclicGraph extends App {
  implicit val system = ActorSystem("CyclicGraph")
  implicit val materializer = ActorFlowMaterializer()
  import system.dispatcher

  val out = Sink.foreach(println)

  val g = FlowGraph.closed(out) { implicit builder =>
    sink =>
      import FlowGraph.Implicits._
      val in = Source(1 to 10)

      val zip = builder.add(Zip[Int, Int]())
      val broadcast = builder.add(Broadcast[Int](2))
      val concat = builder.add(Concat[Int]())

      val logger = Flow[(Int, Int)].map { x => println(x); x }
      val adapter = Flow[(Int, Int)].map(_._1)

      in ~> zip.in0
            zip.out ~> logger ~> adapter ~> broadcast ~> sink
                                            broadcast ~> concat.in(1)

      Source.single(0) ~> concat.in(0)
      concat.out ~> zip.in1
  }

  g.run().onComplete(_ => system.shutdown())
}
