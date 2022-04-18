package ai.newmap.interpreter

import ai.newmap.model._
import ai.newmap.interpreter.TypeChecker._
import ai.newmap.util.{Outcome, Success, Failure}
import org.jline.terminal.TerminalBuilder

// TODO - create a custom shell
object Repl {
  var envInterp = new EnvironmentInterpreter()

  def main(args: Array[String]): Unit = {
    var continue = true

    var history: List[String] = List()
    var reverseHistory: List[String] = List("")

    while(continue) {
      print(" > ")

      val terminal = TerminalBuilder.builder().jna(true).system(true).build()
      terminal.enterRawMode()

      val reader = terminal.reader()
      val writer = terminal.writer()
      var input = -1

      var code = ""

      while (input != 13) {
        input = reader.read

        input match {
          case 65 => //Up
             if (!history.isEmpty){
               print("\r")
               val elem = history.apply(history.length -1)
               code = elem
               print("C> " + elem)
               print(" "*100)
               print("\b"*100)
               history = history.dropRight(1)
               reverseHistory = reverseHistory ::: (elem :: Nil)
             }
          case 66 => //Bottom
            if (!reverseHistory.isEmpty){
              print("\r")
              val elem = reverseHistory.apply(reverseHistory.length -1)
              code = elem
              print("C> " + elem)
              print(" "*100)
              print("\b"*100)
              reverseHistory = reverseHistory.dropRight(1)
              history = history ::: (elem :: Nil)
            }
          case 67 => //Right
             print()
          case 68 => //Left
            print("\b")
            code = code.slice(0, code.length - 1)
          case 127 => // BackSpace
            print("\b")
            print(" ")
            print("\b")
            code = code.slice(0, code.length - 1)
          case 13 => //Enter
            history = history ::: (code :: Nil)
          case unknown =>
            print(unknown.toChar)
            code = code + unknown.toChar
        }
      }

      println()

      val response = envInterp(code)
      response match {
        case Success(s) => {
          if (s == ":exit") {
            continue = false
          } else {
            if (s.length > 0) println(s)
          }
        }
        case Failure(s) => println("Error:\n" + s)
      }
    }
  }
}