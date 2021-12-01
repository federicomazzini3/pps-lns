package lns.subsystems.prolog

import indigo.shared.FrameContext
import indigo.shared.events.*
import lns.StartupData
import lns.core.ContextFixture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }
import lns.subsystems.prolog.AsyncSession

import scala.collection.immutable.HashMap

case class MockClient(
    theory: String,
    query: Option[Query],
    results: List[(QueryId, Substitution)] = List[(QueryId, Substitution)]()
) extends AsyncSession {

  def withResults(results: List[(QueryId, Substitution)]) = copy(results = results)

  override def doQuery(query: Query): Unit = ()

  override def askAnswer(queryId: QueryId): Unit = ()

  override def getAnswerResult(queryId: QueryId): Option[Substitution] = None

  override def getAllAnswersResults: List[(QueryId, Substitution)] = results

}

trait PrologSystemFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  val queryId: QueryId     = "query_test"
  val sessionId: SessionId = "sess_test"
  val substitution         = Substitution(HashMap())

  var system: PrologService            = _
  var systemWithResults: PrologService = _

  object MockClientFactory extends ((String, Option[Query]) => AsyncSession) {
    def apply(theory: String, query: Option[Query] = None): AsyncSession = MockClient(theory, query)
  }

  object MockClientWithResultsFactory extends ((String, Option[Query]) => AsyncSession) {
    def apply(theory: String, query: Option[Query] = None): AsyncSession =
      MockClient(theory, query).withResults(List((queryId, substitution)))
  }

  override def beforeEach() = {

    system = new PrologService(MockClientFactory)
    systemWithResults = new PrologService(MockClientWithResultsFactory)

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
          val updatedModel = systemWithResults
            .update(getSubSytemContext(1), systemWithResults.initialModel.unsafeGet)(
              PrologCommand.Consult(sessionId, "test(X).\n", Some(Query(queryId, "test(1).")))
            )
            .unsafeGet

          val events = systemWithResults
            .update(getSubSytemContext(1), updatedModel)(FrameTick)

          assert(events.globalEventsOrNil == List(PrologEvent.Answer(queryId, substitution)))
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
