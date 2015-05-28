import akka.actor.{Props, ActorSystem}
import akka.stream.ActorFlowMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Flow, Sink, Source}

object ActorIntegration extends App {
  implicit val system = ActorSystem("SimpleStream")
  implicit val materializer = ActorFlowMaterializer()
  import system.dispatcher

  object JobManager {
    def props: Props = Props[JobManager]

    final case class Job(payload: String)
  }

  class JobManager extends ActorPublisher[JobManager.Job] {
    import akka.stream.actor.ActorPublisherMessage._
    import JobManager._

    var buf = Vector.empty[Job]

    def receive = {
      case job: Job =>
        if (buf.isEmpty && totalDemand > 0)
          onNext(job)
        else {
          buf :+= job
          deliverBuf()
        }
      case Request(_) =>
        deliverBuf()
      case Cancel =>
        context.stop(self)
    }

    final def deliverBuf(): Unit =
      if (totalDemand > 0) {
        val (use, keep) = buf.splitAt(totalDemand.toInt)
        buf = keep
        use foreach onNext
      }
  }

  val jobManagerSource = Source.actorPublisher[JobManager.Job](JobManager.props)
  val ref = Flow[JobManager.Job]
    .map(_.payload.toUpperCase)
    .to(Sink.foreach(println))
    .runWith(jobManagerSource)

  ref ! JobManager.Job("a")
  ref ! JobManager.Job("b")
  ref ! JobManager.Job("c")
}
