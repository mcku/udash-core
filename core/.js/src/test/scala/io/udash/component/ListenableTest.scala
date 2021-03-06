package io.udash
package component

import io.udash.testing.UdashFrontendTest

import scala.collection.mutable.ListBuffer

private final class TestListenable extends Listenable {
  override type EventType = TestListenableEvent

  def fireEvent(): Unit = {
    fire(TestListenableEvent(this))
  }

  def emptyRegistration(): Registration = listen { case _ => () }
}
private final case class TestListenableEvent(source: TestListenable) extends ListenableEvent

class ListenableTest extends UdashFrontendTest {
  private trait Fixture {
    protected val listenable = new TestListenable
  }

  "Listenable" should {
    "deactivate active Registrations on remove listeners called" in new Fixture {
      val registration = listenable.emptyRegistration()
      listenable.removeListeners()
      registration.isActive should ===(false)
    }
    "execute onEvent handler on fire called correct number of times" in new Fixture {
      var callCounter = 0
      listenable.listen {
        case _ => callCounter += 1
      }
      callCounter should ===(0)
      listenable.fireEvent()
      listenable.fireEvent()
      callCounter should ===(2)
      listenable.fireEvent()
      callCounter should ===(3)
    }
    "notify listeners in correct registration order" in new Fixture {
      val callSequenceIndicator = ListBuffer.empty[String]
      listenable.listen {
        case _ => callSequenceIndicator += "FirstListenerNotified"
      }
      listenable.listen {
        case _ => callSequenceIndicator += "SecondListenerNotified"
      }
      listenable.fireEvent()
      callSequenceIndicator(0) shouldBe "FirstListenerNotified"
      callSequenceIndicator(1) shouldBe "SecondListenerNotified"
    }
    "allow callback lifecycle management" in new Fixture {
      val callbacks = ListBuffer.empty[Int]

      val registration = listenable.listen {
        case TestListenableEvent(_) => callbacks += 1
      }

      listenable.listen {
        case TestListenableEvent(_) => callbacks += 2
      }

      callbacks shouldBe empty

      listenable.fireEvent()

      callbacks should contain theSameElementsInOrderAs Seq(1, 2)
      callbacks.clear()

      assert(registration.isActive)

      registration.restart() //moves first callback to the end

      assert(registration.isActive)

      listenable.fireEvent()

      callbacks should contain theSameElementsInOrderAs Seq(2, 1)
      callbacks.clear()

      registration.cancel()

      assert(!registration.isActive)

      listenable.fireEvent()

      callbacks should contain theSameElementsInOrderAs Seq(2)
      callbacks.clear()

      registration.restart()

      listenable.fireEvent()

      callbacks should contain theSameElementsInOrderAs Seq(2, 1) //order not maintained after restart
    }
  }

  "Registration" should {
    "be active initially" in new Fixture {
      listenable.emptyRegistration().isActive should ===(true)
    }
    "unregister on cancel called" in new Fixture {
      val registration = listenable.emptyRegistration()
      registration.cancel()
      registration.isActive should ===(false)
    }
    "reregister on restart called" in new Fixture {
      val registration = listenable.emptyRegistration()
      registration.cancel()
      registration.isActive should ===(false)
      registration.restart()
      registration.isActive should ===(true)
    }
  }

}
