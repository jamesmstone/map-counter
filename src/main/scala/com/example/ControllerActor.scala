package com.example

import com.acervera.osm4scala.BlobTupleIterator
import com.example.ControllerActor.{AddActorsMsg, ControllerMsg, CounterResponseMsg}
import com.example.CounterActor.{BlobTupleMsg, CounterResponse}
import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import org.apache.pekko.actor.typed.{ActorRef, Behavior}

import java.io.{File, FileInputStream, InputStream}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object ControllerActor {
  sealed trait ControllerMsg
  case class AddActorsMsg(actors: Int) extends ControllerMsg
  case class CounterResponseMsg(response: CounterResponse) extends ControllerMsg

  def apply(pbfFile: File): Behavior[ControllerMsg] = Behaviors.setup { context =>
    new ControllerActor(context, pbfFile).controllerBehavior()
  }
}

class ControllerActor(context: ActorContext[ControllerMsg], pbfFile: File) {
  private val pbfIS: InputStream = new FileInputStream(pbfFile)
  private val blobIterator: BlobTupleIterator = BlobTupleIterator.fromPbf(pbfIS)
  private val actorPool: ListBuffer[ActorRef[CounterActor.CounterMsg]] = ListBuffer.empty
  private val counter = mutable.Map[String, Long]()
  private val startTime = System.currentTimeMillis()
  private var i = 0

  private def controllerBehavior(): Behavior[ControllerMsg] = Behaviors.receiveMessage {
    case AddActorsMsg(actors) =>
      assert(actors > 0)
      val originalPoolSize = actorPool.size
      for (idx <- 1 to actors) {
        val newActor = context.spawn(CounterActor(), s"counter_${originalPoolSize + idx}")
        actorPool += newActor
        nextBlob(newActor)
      }
      Behaviors.same

    case CounterResponseMsg(response) =>
      response.counter.keysIterator.foreach(key=>{
       counter.updateWith(key) {
         case Some(value) => Some(value + response.counter.getOrElse(key, 0L))
         case None => Some(response.counter.getOrElse(key, 0L))
       }
      })
      if (actorPool.nonEmpty) {
        nextBlob(response.replyTo)
      }
      Behaviors.same
  }

  private def nextBlob(counterActor: ActorRef[CounterActor.CounterMsg]): Unit = {
    if (blobIterator.hasNext) {
      val nextBlob = blobIterator.next()
      i+=1
      counterActor ! BlobTupleMsg(i, nextBlob._1, nextBlob._2, context.self)
    } else {
      actorPool -= counterActor
      context.stop(counterActor)

      if (actorPool.isEmpty) {
        printResults()
        pbfIS.close()
        context.system.terminate()
      }
    }
  }

  private def printResults(): Unit = {
    println("Results:")
    counter.toVector.sortBy(_._2).iterator.foreach(item => {
      println(s" ${item._1}: ${item._2}")

    })
    println(f"Finished in ${(System.currentTimeMillis() - startTime) / 1e3}%,2.2f sec.")
  }
}
