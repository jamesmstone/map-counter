//#full-example
package com.example

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
//import com.example.Greeter.Greeted
import org.scalatest.wordspec.AnyWordSpecLike

// TODO
/*//#definition
class PekkoQuickstartSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
//#definition

  "A Greeter" must {
    //#test
    "reply to greeted" in {
      val replyProbe = createTestProbe[Greeted]()
      val underTest = spawn(Greeter())
      underTest ! Greet("Santa", replyProbe.ref)
      replyProbe.expectMessage(Greeted("Santa", underTest.ref))
    }
    //#test
  }

}
//#full-example*/
