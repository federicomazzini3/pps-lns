package lns.subsystems.prolog

import indigo.shared.FrameContext
import indigo.shared.events.*
import lns.StartupData
import lns.core.ContextFixture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }
import org.mockito.Mockito.*
import org.mockito.{ InjectMocks, Mock, MockitoAnnotations, Spy }
import lns.subsystems.prolog.AsyncSession
import org.mockito.ArgumentMatchers.any

import scala.collection.immutable.HashMap

trait PrologSystemFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  val queryId: QueryId     = "query_test"
  val sessionId: SessionId = "sess_test"

  var mockClient: AsyncSession = _
  var system: PrologService    = _

  override def beforeEach() = {

    mockClient = mock(classOf[AsyncSession])

    object MockClientFactory extends AsyncSessionFactory {
      def create(theory: String, query: Option[Query] = None): AsyncSession = mockClient
    }

    system = new PrologService(MockClientFactory)

    super.beforeEach()
  }
}

class PrologServiceTest extends AnyFreeSpec with PrologSystemFixture {

  "A PrologSystem" - {
    "after the event PrologCommand.Consult" - {
      "if session doesn't already exists should" - {
        "create a new session in the model" in {

          val updatedModel = system
            .update(getSubSytemContext(1), system.initialModel.unsafeGet)(
              PrologCommand.Consult(sessionId, "test(X).\n", Some(Query(queryId, "test(1).")))
            )
            .unsafeGet

          assert(updatedModel.isDefinedAt("sess_test"))
        }
      }
      "if session already exists should" - {
        "not update the model" in {
          val updatedModel = system
            .update(getSubSytemContext(1), system.initialModel.unsafeGet)(
              PrologCommand.Consult(sessionId, "test(X).\n", Some(Query(queryId, "test(1).")))
            )
            .unsafeGet

          val updatedModel2 = system
            .update(getSubSytemContext(1), updatedModel)(
              PrologCommand.Consult(sessionId, "test(X).\n", Some(Query(queryId, "test(1).")))
            )
            .unsafeGet

          assert(updatedModel == updatedModel2)
        }
      }
    }
    "after the event FrameTick" - {
      "if no consult received should" - {
        "not update the model" in {
          val updatedModel = system
            .update(getSubSytemContext(1), system.initialModel.unsafeGet)(FrameTick)
            .unsafeGet

          assert(updatedModel == system.initialModel.unsafeGet)
        }
        "return no events " in {
          val events = system
            .update(getSubSytemContext(1), system.initialModel.unsafeGet)(FrameTick)

          assert(events.globalEventsOrNil == Nil)
        }
      }
      "if a consult done but no answers received should" - {
        "return no events" in {
          doReturn(List[(QueryId, Substitution)]()).when(mockClient).getAllAnswersResults;

          val updatedModel = system
            .update(getSubSytemContext(1), system.initialModel.unsafeGet)(
              PrologCommand.Consult(sessionId, "test(X).\n", Some(Query(queryId, "test(1).")))
            )
            .unsafeGet

          val events = system
            .update(getSubSytemContext(1), updatedModel)(FrameTick)

          assert(events.globalEventsOrNil == Nil)
        }
      }
      "if a consult done and answer result ready" - {
        "return an PrologEvent.Answer event" in {
          val sub = Substitution(HashMap())
          doReturn(List((queryId, sub))).when(mockClient).getAllAnswersResults;

          val updatedModel = system
            .update(getSubSytemContext(1), system.initialModel.unsafeGet)(
              PrologCommand.Consult(sessionId, "test(X).\n", Some(Query(queryId, "test(1).")))
            )
            .unsafeGet

          val events = system
            .update(getSubSytemContext(1), updatedModel)(FrameTick)

          assert(events.globalEventsOrNil == List(PrologEvent.Answer(queryId, sub)))
        }
      }
    }
  }
}

/*
val newSub = new TauSubstitution(
    links = Dictionary[TauTerm](
      (
        "L",
        TauTerm(
          ".",
          js.Array[TauTerm | TauVar | TauNum](
            TauTerm(
              ".",
              js.Array[TauTerm | TauVar | TauNum](
                TauNum(0, false),
                TauNum(0, false),
                TauTerm("s", js.Array[TauTerm | TauVar | TauNum]())
              )
            ),
            TauTerm(
              ".",
              js.Array[TauTerm | TauVar | TauNum](
                TauTerm(
                  ".",
                  js.Array[TauTerm | TauVar | TauNum](
                    TauNum(1, false),
                    TauNum(0, false),
                    TauTerm("e", js.Array[TauTerm | TauVar | TauNum]())
                  )
                ),
                TauTerm("[]", js.Array[TauTerm | TauVar | TauNum]())
              )
            )
          )
        )
      )
    )
  )
  println(newSub)
 * */
