package ai.newmap.model

import scala.collection.mutable.StringBuilder
import ai.newmap.model._

object PrintNewMapObject {
  def apply(obj: NewMapObject): String = obj match {
    case Index(i) => i.toString
    case CountT => "Count"
    case TypeT => s"Type"
    case AnyT => s"Any"
    case IsCommandFunc => s"IsCommandFunc"
    case IsSimpleFunction => s"IsSimpleFunction"
    case IsVersionedFunc => s"IsVersionedFunc"
    case IncrementFunc => s"Increment"
    case IdentifierT => "Identifier"
    case IdentifierInstance(s) => s + "~Id"
    case MapT(key, value, completeness, featureSet) => {
      (completeness, featureSet) match {
        case (CommandOutput, BasicMap) => "Map(" + this(key) + ", " + this(value) + ")"
        case (RequireCompleteness, SimpleFunction) => "ReqMap(" + this(key) + ", " + this(value) + ")"
        case (RequireCompleteness, FullFunction) => {
          // TODO(2022): Change the way lambda input works so that it's more like Map
          "\\(" + this(key) + ": " + this(value) + ")"
        }
        case _ => {
          // TODO(2022): Improve Notation so that we don't need this!
          "SpecialMap(" + this(key) + ", " + this(value) + ", " + completeness + ", " + featureSet + ")"
        }  
      }
    }
    case MapInstance(values, mapT) => s"MI:${mapToString(values)}~${this(mapT)}"
    case ApplyFunction(func, input) => {
      this(func) + " " + this(input)
    }
    case AccessField(struct, field) => s"${this(struct)}.${this(field)}"
    case ParamId(name) => s"$name~pi"
    case ParameterObj(uuid, nType) => s"$uuid~Po:(${this(nType)})"
    case StructT(params) => "Struct " + this(params)
    case CaseT(cases) => "Case " + this(cases)
    case StructInstance(value, structT) => {
      val sb: StringBuilder = new StringBuilder()
      sb.append("StructInstance(")
      var bindings: Vector[String] = Vector.empty
      for {
        (k, v) <- value
      } {
        bindings :+= k + ": " + this(v)
      }
      sb.append(bindings.mkString(", "))
      sb.append(")")
      sb.toString
    }
    case CaseInstance(constructor, value, _) => {
      constructor + " " + this(value)
    }
    //TODO(2022): we might not want to print out the full parent here, because it could be large
    // - instead, we link to the function or map somehow... when we give things uniqueids we can figure this out
    case x@SubtypeT(isMember) => s"Subtype(${this(isMember)})"
    case RangeFunc(i) => s"<$i"
    case VersionedObject(currentState: NewMapObject, commandType: NewMapObject, v: Long) => {
      this(currentState) + s"v$v"
    }
    case BranchedVersionedObject(vo: NewMapObject, base: NewMapObject, changeLog: Vector[NewMapObject]) => {
      this(vo)
    }
  }

  def printParams(params: Vector[(String, NewMapObject)]): String = {
    mapToString(params.map(x => ObjectPattern(IdentifierInstance(x._1)) -> x._2))
  }

  def mapToString(values: Vector[(NewMapPattern, NewMapObject)]): String = {
    val sb: StringBuilder = new StringBuilder()
    sb.append("(")

    var bindings: Vector[String] = Vector.empty
    for {
      (k, v) <- values
    } {
      bindings :+= patternToString(k) + ": " + this(v)
    }
    sb.append(bindings.mkString(", "))

    sb.append(")")
    sb.toString
  }

  def patternToString(nPattern: NewMapPattern): String = nPattern match {
    case ObjectPattern(nObject) => this(nObject)
    case TypePattern(name, nType) => s"$name: ${this(nType)}"
    case StructPattern(params) => s"(${params.map(patternToString(_)).mkString(", ")})"
    case MapTPattern(input, output, featureSet) => s"MapTPattern($input, $output, $featureSet)"
    case MapPattern(mapTPattern) => s"MapPattern(${patternToString(mapTPattern)})"
  }
}