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

  case class CounterResponse(replyTo: ActorRef[CounterActor.CounterMsg], counter: mutable.Map[String, Long]) extends CounterMsg

  private val tagsToStore = List("emergency", "public_transport", "building" ,"cycleway", "highway") // todo consider getting from config
  def apply(): Behavior[CounterMsg] = Behaviors.receive { (context, message) =>
    var i : Long = 0L
    message match {
      case CounterResponse(_, _) =>
        Behaviors.same
      case BlobTupleMsg(_, header, blob, replyTo) =>
        val entityIterator = fromBlob(blob)

        val counts = mutable.Map[String, Long]() // todo consider moving so not re instantiated on every message, ie we share across messages then at the end request the total back in a different style message

        entityIterator.foreach {
          case NodeEntity(id, latitude, longitude, tags, info) => {
            i += 1L
            tagsToStore.foreach(tag => {
              tags.get(tag) match {
                case Some(v) => {
                  val keys = v.split(';')
                  keys.foreach(k => {
                    counts.updateWith(s"$tag: $k") {
                      case Some(value) => Some(value + 1L)
                      case None => Some(1L)
                    }
                  })

                }
                case _ => false
              }
            })
          }
          case _ => false
        }
        counts.updateWith("*") {
          case Some(value) => Some(value + i)
          case None => Some(i)
        }
        replyTo ! ControllerActor.CounterResponseMsg(CounterResponse(context.self, counts))
        Behaviors.same
    }
  }
}
