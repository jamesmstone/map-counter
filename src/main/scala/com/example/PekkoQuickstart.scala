//#full-example
package com.example


import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors

import java.io.File


//#main-class
object PekkoQuickstart extends App {
  private val rootBehavior = Behaviors.setup[Nothing] { context =>
    val file = new File("/home/james/Downloads/denmark-latest.osm.pbf") // todo get from config /  cli
    val controller = context.spawn(ControllerActor(file), "controller")
    val numberWorkerActors = 6// todo get from config
    controller ! ControllerActor.AddActorsMsg(numberWorkerActors)
    Behaviors.empty
  }

  //#actor-system
  ActorSystem[Nothing](rootBehavior, "pbf_counter_controller")

}
//#main-class
//#full-example
