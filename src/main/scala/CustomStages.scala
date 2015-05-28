import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.stage.{SyncDirective, Directive, Context, PushPullStage}

object CustomStages extends App {
  implicit val system = ActorSystem("CustomStages")
  implicit val materializer = ActorFlowMaterializer()
  import system.dispatcher

  class Map[A, B](f: A => B) extends PushPullStage[A, B] {
    override def onPush(elem: A, ctx: Context[B]): SyncDirective =
      ctx.push(f(elem))

    override def onPull(ctx: Context[B]): SyncDirective =
      ctx.pull()
  }

  class Filter[A](p: A => Boolean) extends PushPullStage[A, A] {
    override def onPush(elem: A, ctx: Context[A]): SyncDirective =
      if (p(elem)) ctx.push(elem)
      else ctx.pull()

    override def onPull(ctx: Context[A]): SyncDirective =
      ctx.pull()
  }

  Source(1 to 10)
    .transform(() => new Map[Int, Int](_ * 3))
    .transform(() => new Filter[Int](_.toString.head != '2'))
    .runWith(Sink.foreach(println))
    .onComplete(_ => system.shutdown())
}

