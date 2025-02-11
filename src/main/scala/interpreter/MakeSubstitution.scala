package ai.newmap.interpreter

import ai.newmap.model._
import ai.newmap.util.{Outcome, Success, Failure}

// Subsitute the given parameters for their given values in the expression
object MakeSubstitution {
  def apply(
    expression: NewMapExpression,
    parameters: Map[String, NewMapObject],
    env: Environment
  ): NewMapExpression = {
    expression match {
      case ObjectExpression(nObject) => {
        // Temporary solution is to dig through to find the map expressions with the parameters
        // Permanent solution is to make a "build map construction + functions"
        val fixedObject = substObject(nObject, parameters, env)
        ObjectExpression(fixedObject)
      }
      case ApplyFunction(func, input) => {
        ApplyFunction(
          this(func, parameters, env),
          this(input, parameters, env)
        )
      }
      case ParamId(name) => {
        parameters.get(name) match {
          case Some(obj) => ObjectExpression(obj)
          case None => expression
        }
      }
      case BuildCase(constructor, input, caseType) => {
        BuildCase(constructor, this(input, parameters, env), caseType)
      }
      case BuildMapT(inputType, outputType, completeness, featureSet) => {
        BuildMapT(
          this(inputType, parameters, env),
          this(outputType, parameters, env),
          completeness,
          featureSet
        )
      }
      case BuildTableT(keyType, requiredValues) => {
        BuildTableT(
          this(keyType, parameters, env),
          this(requiredValues, parameters, env)
        )
      }
      case BuildExpandingSubsetT(parentType) => {
        BuildExpandingSubsetT(this(parentType, parameters, env))
      }
      case BuildSubtypeT(isMember) => {
        BuildSubtypeT(this(isMember, parameters, env))
      }
      case BuildCaseT(cases) => BuildCaseT(this(cases, parameters, env))
      case BuildStructT(params) => BuildStructT(this(params, parameters, env))
      case BuildMapInstance(values, mapT) => {
        val newMapValues = for {
          (k, v) <- values
        } yield {
          val nps = Evaluator.newParametersFromPattern(k).toSet
          val newValue = this(v, parameters.filter(x => !nps.contains(x._1)), env)
          k -> newValue
        }

        BuildMapInstance(newMapValues, mapT)
      }
    }
  }

  // TODO - this will become unneccesary when we create a "buildMap" instead of relying on NewMapObject
  // NewMapObject should not contain any outside parameters!!!
  def substObject(
    nObject: NewMapObject,
    parameters: Map[String, NewMapObject],
    env: Environment
  ): NewMapObject = {
    nObject match {
      case TaggedObject(UMap(values), mapT) => {
        val newValues = for {
          (k, v) <- values

          internalParams = Evaluator.newParametersFromPattern(k)
          // We cannot replace params that have the same name

          remainingParameters = parameters -- internalParams
        } yield (k -> this(v, remainingParameters, env))

        TaggedObject(
          UMap(values),
          mapT
        )
      }
      case _ => nObject
    }
  }
}