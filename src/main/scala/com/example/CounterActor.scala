package com.example

import com.acervera.osm4scala.EntityIterator.fromBlob
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import com.acervera.osm4scala.model.NodeEntity
import org.openstreetmap.osmosis.osmbinary.fileformat.{Blob, BlobHeader}

import scala.collection.mutable

object CounterActor {
  sealed trait CounterMsg

  case class BlobTupleMsg(i: Int, header: BlobHeader, blob: Blob, replyTo: ActorRef[ControllerActor.ControllerMsg]) extends CounterMsg

  case class CounterResponse(replyTo: ActorRef[CounterActor.CounterMsg], counter: Map[String, Long]) extends CounterMsg

  private val tagsToStore = List("emergency", "public_transport", "building", "cycleway", "highway") // todo consider getting from config

  def apply(): Behavior[CounterMsg] = Behaviors.receive { (context, message) =>
    var i: Long = 0L
    message match {
      case CounterResponse(_, _) =>
        Behaviors.same
      case BlobTupleMsg(_, header, blob, replyTo) =>
        val entityIterator = fromBlob(blob)

        // todo consider moving so not re instantiated on every message, ie we share across messages then at the end request the total back in a different style message
        val counts = entityIterator.foldLeft(Map[String, Long]().withDefaultValue(0L)) {
          case (acc, NodeEntity(id, latitude, longitude, tags, info)) =>
            val updatedAcc = tagsToStore.foldLeft(acc) { (innerAcc, tag) =>
              tags.get(tag).fold(innerAcc) { v =>
                v.split(';').foldLeft(innerAcc) { (finalAcc, k) =>
                  finalAcc.updated(s"$tag: $k", finalAcc(s"$tag: $k") + 1L)
                }
              }
            }
            updatedAcc.updated("*", updatedAcc("*") + 1L)

          case (acc, _) => acc
        }

        replyTo ! ControllerActor.CounterResponseMsg(CounterResponse(context.self, counts))
        Behaviors.same
    }
  }
}
