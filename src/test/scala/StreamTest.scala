import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{Matchers, FlatSpec}

class StreamTest extends FlatSpec with Matchers {
  implicit val system = ActorSystem("SimpleStream")
  implicit val materializer = ActorFlowMaterializer()

  "A scala test" should "check things" in {
    Source(1 to 4)
      .filter(_ % 2 == 0)
      .map(_ * 2)
      .runWith(TestSink.probe[Int])
      .request(2)
      .expectNext(4, 8, 9)
      .expectComplete()
  }
}
