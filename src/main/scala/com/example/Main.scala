package com.example

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors

import java.io.File


@main def main()={
  val rootBehavior = Behaviors.setup[Nothing] { context =>
    val file = new File("/home/james/Downloads/denmark-latest.osm.pbf") // todo get from config /  cli
    val logger = LoggerFactory.createLogger("main")
    logger.info(s"running in ${os.pwd}")
//    val xxx = os.
    val controller = context.spawn(ControllerActor(file), "controller")
    val numberWorkerActors = 12// todo get from config
    controller ! ControllerActor.AddActorsMsg(numberWorkerActors)
    Behaviors.empty
  }

  ActorSystem[Nothing](rootBehavior, "pbf_counter_controller")

}
