package ai.newmap.interpreter

import org.scalatest._
import ai.newmap.model._
import ai.newmap.util.{Outcome, Success, Failure}

class TestEvaluator extends FlatSpec {
  def assertFunctionWorkedAndReturnedResult(
    attempt: Outcome[NewMapObject, String],
    result: NewMapObject
  ): Unit = {
    attempt match {
      case Success(output) => assert(output == result)
      case Failure(f) => fail(s"Apply function failed: $f")
    }
  }

  def Index(i: Long): NewMapObject = TaggedObject(UIndex(i), CountT)

  "isCommandFunc " should " work properly on ranges" in {
    val result = Evaluator.applyFunctionAttempt(TaggedObject(
      IsCommandFunc,
      MapT(TypeT, Index(2), CommandOutput, SimpleFunction)
    ), Index(10), Environment.Base)
    assertFunctionWorkedAndReturnedResult(result, Index(1))
  }

  it should " work properly on count" in {
    val result = Evaluator.applyFunctionAttempt(TaggedObject(
      IsCommandFunc,
      MapT(TypeT, Index(2), CommandOutput, SimpleFunction)
    ), CountT, Environment.Base)
    assertFunctionWorkedAndReturnedResult(result, Index(1))
  }

  it should " work properly on TypeT, and return false" in {
    val result = Evaluator.applyFunctionAttempt(TaggedObject(
      IsCommandFunc,
      MapT(TypeT, Index(2), CommandOutput, SimpleFunction)
    ), TypeT, Environment.Base)
    assertFunctionWorkedAndReturnedResult(result, Index(1))
  }

  // TODO: remove this when IsSimpleFunction is eliminated
  "IsSimpleFunction" should "work properly" in {
    assertFunctionWorkedAndReturnedResult(
      Evaluator.applyFunctionAttempt(TaggedObject(
        IsSimpleFunction,
        MapT(AnyT, Index(2), CommandOutput, SimpleFunction)
      ), TypeT, Environment.Base),
      Index(0)
    )

    assertFunctionWorkedAndReturnedResult(
      Evaluator.applyFunctionAttempt(TaggedObject(
        IsSimpleFunction,
        MapT(AnyT, Index(2), CommandOutput, SimpleFunction)
      ), TaggedObject(UMap(Vector.empty), MapT(Index(10), Index(10), CommandOutput, SimpleFunction)), Environment.Base),
      Index(1)
    )

    assertFunctionWorkedAndReturnedResult(
      Evaluator.applyFunctionAttempt(TaggedObject(
        IsSimpleFunction,
        MapT(AnyT, Index(2), CommandOutput, SimpleFunction)
      ), TaggedObject(UMap(Vector.empty), MapT(Index(10), Index(10), CommandOutput, BasicMap)), Environment.Base),
      Index(1)
    )

    assertFunctionWorkedAndReturnedResult(
      Evaluator.applyFunctionAttempt(TaggedObject(
        IsSimpleFunction,
        MapT(AnyT, Index(2), CommandOutput, SimpleFunction)
      ), TaggedObject(UMap(Vector.empty), MapT(Index(10), Index(10), CommandOutput, FullFunction)), Environment.Base),
      Index(0)
    )
  }
}