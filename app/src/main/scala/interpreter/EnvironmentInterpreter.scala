package ai.newmap.interpreter

import ai.newmap.model._
import ai.newmap.interpreter.TypeChecker._
import ai.newmap.interpreter.Processor
import ai.newmap.util.{Outcome, Success, Failure}
import ai.newmap.environment.envCreater.envCreate
import ai.newmap.environment.envCreater.envCopy
import ai.newmap.environment.envReader.envLogIn
import ai.newmap.environment.envReader.envLogOff
import ai.newmap.environment.envPrinter.envPrint
import ai.newmap.environment.envPrinter.envsPrint
import ai.newmap.environment.envPrinter.prettyPrinter
import ai.newmap.environment.envCommenter.envComment
import ai.newmap.environment.envCommiter.envCommit
import ai.newmap.environment.envCommiter.printLog
import ai.newmap.environment.envCommiter.checkout
import ai.newmap.environment.envCommiter.resetHard
import ai.newmap.environment.envCommiter.reset

class EnvironmentInterpreter() {
  var env: Environment = Environment.Base
  var chanName = ""
  var userName = ""

  def setChanName(chanName: String) {
    this.chanName = chanName
  }

  def setUserName(userName: String) {
    this.userName = userName
  }


  for (code <- EnvironmentInterpreter.initialCommands) {
    apply(code)
  }

  /*
   * @param code The code entered into the interpreter
   * @return The response from the interpreter
   */
  def apply(code: String): Outcome[String, String] = {
    applyInterpCommand(code) match {
      case CommandPrintSomething(response) => Success(response)
      case CommandExit => Success(":exit")
      case CommandPassThrough => applyEnvCommand(code)
      case CommandFileProcessor(filepath) => {
        val fileProc = new Processor
        val response = fileProc.FileProcessor(filepath)
        Success(response)
      }
      case CommandTextBlockProcessor(textString) => {
        val textBlockProc = new Processor
        val response = textBlockProc.textBlockProcessor(textString)
        Success(response)
      }
    }
  }

  abstract class CommandInterpResponse
  case class CommandPrintSomething(s: String) extends CommandInterpResponse
  case object CommandExit extends CommandInterpResponse
  case object CommandPassThrough extends CommandInterpResponse
  case class CommandFileProcessor(s: String) extends CommandInterpResponse
  case class CommandTextBlockProcessor(s: String) extends CommandInterpResponse

  def createEnv(input: String): CommandInterpResponse = {
    val cont = input.split("\\s+")
    if (cont.size != 2){
      return CommandPrintSomething("*Wrong input format, please check :help for detail*")
    }
    val envName = cont(0)
    val envAccessCode = cont(1)
    val ret: Boolean = envCreate(this.chanName, this.userName, envName, envAccessCode)
    if(ret)CommandPrintSomething("*Environment create success*")
    else{
      CommandPrintSomething("*"+envName+" already exist*")
    }
  }

  def logInEnv(input: String): CommandInterpResponse = {
    val cont = input.split("\\s+")
    if (cont.size != 2){
      return CommandPrintSomething("*Wrong input format, please check :help for detail*")
    }
    val envName = cont(0)
    val envAccessCode = cont(1)
    // return 1: environment not exsit
    // return 2: wrong password
    // return 0: loged in
    val ret: Int = envLogIn(this.chanName, this.userName, envName, envAccessCode)
    if(ret == 0)CommandPrintSomething("*Logged into Environment "+envName+"*")
    else if(ret == 1) {
      CommandPrintSomething("*Could not log in, environment not exist*")
    }else{
      CommandPrintSomething("*Could not log in, wrong password*")
    }
  }

  def logOffEnv(): CommandInterpResponse = {
    val ret: Boolean = envLogOff(this.chanName, this.userName)
    if(!ret){
      CommandPrintSomething("*Could not log off, because you didn't logged in*")
    }else{
      CommandPrintSomething("*Logged off success*")
    }
  }

  def copyEnv(input: String): CommandInterpResponse = {
    val cont = input.split("\\s+")
    if(cont.size != 5){
      return CommandPrintSomething("*Wrong input format, please check :help for detail*")
    }
    val fromChanName = cont(0)
    val envName = cont(1)
    val envAccessCode = cont(2)
    val newEnvName = cont(3)
    val newAccessCode = cont(4)
    // ret 1: coppied environment not exist
    // ret 2: wrong access code
    // ret 3: new environment name already exist
    // ret 0: coppied success
    val ret: Int = envCopy(this.chanName, fromChanName, this.userName, envName, envAccessCode, newEnvName, newAccessCode)
    if(ret == 1){CommandPrintSomething("*Could not copy, "+envName+" not exist*")}
    else if(ret == 2){CommandPrintSomething("*Could not copy, wrong password*")}
    else if(ret == 3){CommandPrintSomething("*Could not copy, "+newEnvName+" already exist*")}
    else{
      CommandPrintSomething("*Environment copied success*")
    }
  }

  def commentEnv(input: String): CommandInterpResponse = {

    val cont = input.split("\\(")
    if(cont.size != 2){
      return CommandPrintSomething("*Wrong input format, please check :help for detail*")
    }
    val comment: String = cont(1).stripSuffix(")")
    if(cont(0).split("\\s+").size != 2){
      return CommandPrintSomething("*Wrong input format, please check :help for detail*")
    }
    val envName: String = cont(0).split("\\s+")(0)
    val envAccessCode: String = cont(0).split("\\s+")(1)

    val ret: Int = envComment(this.chanName, this.userName, envName, envAccessCode, comment)
    if(ret == 1){CommandPrintSomething("*Could not comment, "+envName+" not exist*")}
    else if(ret == 2){CommandPrintSomething("*Could not comment, wrong password*")}
    else{
      CommandPrintSomething("*Comment success*")
    }
  }

  def commitEnv(input: String): CommandInterpResponse = {
    val ret: Int = envCommit(this.chanName, this.userName, input)
    if(ret == 1){CommandPrintSomething("*Couldn't commit, Please log in first*")}
    else if(ret == 2){CommandPrintSomething("*Env up to date, no need to commit*")}
    else{
      CommandPrintSomething("*Env commit success*")
    }
  }

  def checkoutVer(input: String): CommandInterpResponse = {
    val ret: String = checkout(this.chanName, this.userName, input)
    CommandPrintSomething(ret)
  }

  def resetHardCommit(input: String): CommandInterpResponse = {
    val cont = input.split("\\s+")
    if (cont.size != 2){
      return CommandPrintSomething("*Wrong input format, please check :help for detail*")
    }
    val uuid = cont(0)
    val envAccessCode = cont(1)
    val ret = resetHard(this.chanName, this.userName, uuid, envAccessCode)
    CommandPrintSomething(ret)
  }

  def resetCommit(input: String): CommandInterpResponse = {
    val cont = input.split("\\s+")
    if (cont.size != 2){
      return CommandPrintSomething("*Wrong input format, please check :help for detail*")
    }
    val uuid = cont(0)
    val envAccessCode = cont(1)
    val ret = reset(this.chanName, this.userName, uuid, envAccessCode)
    CommandPrintSomething(ret)
  }


  def applyInterpCommand(code: String): CommandInterpResponse = {
    code match {
      case code if code.startsWith(":create")  => {
                        val cont = code.stripPrefix(":create ")
                        this.createEnv(cont)
                      }
      case code if code.startsWith(":log in ") => {
                        val cont = code.stripPrefix(":log in ")
                        this.logInEnv(cont)
                      }
      case code if code.startsWith(":copy ") => {
                        val cont = code.stripPrefix(":copy ")
                        this.copyEnv(cont)
                      }
      case code if code.startsWith(":comment on ") => {
                        val cont = code.stripPrefix(":comment on ")
                        this.commentEnv(cont)
                      }
      case code if code.startsWith(":commit") => {
                        this.commitEnv(code.stripPrefix(":commit"))
                      }
      case code if code.startsWith(":checkout ")=> {
                        this.checkoutVer(code.stripPrefix(":checkout "))
                      }
      case code if code.startsWith(":hard reset ")=> {
                        this.resetHardCommit(code.stripPrefix(":hard reset "))
                      }
      case code if code.startsWith(":reset ")=> {
                        this.resetCommit(code.stripPrefix(":reset "))
                      }
      case code if code.startsWith(":script")=> {
        if (code.substring(8, 13) == "-file")
          CommandFileProcessor(code.drop(14))
        else
          CommandTextBlockProcessor(code.drop(8))
      }
      //case ":env" => CommandPrintSomething(env.toString)
      case ":log off" => logOffEnv
      case ":printEnv" => CommandPrintSomething(envPrint(this.chanName, this.userName))
      case ":envs" => CommandPrintSomething(envsPrint(this.chanName))
      case ":printLog" => CommandPrintSomething(printLog(this.chanName, this.userName))
      case ":exit" | ":quit" => CommandExit
      case ":help" => CommandPrintSomething(
        "*List of environment commands*\n" ++
        "*:envs*\tPrint the environments under current channel\n" ++
        //":exit | :quit\tExit this repl\n" ++
        "*:create <env name> <env password>*\tCreate a new environment\n" ++
        "*:log in <env name> <env password>*\tLog in to an exist environment\n" ++
        "*:log off*\tLog off from current environment\n" ++
        "*:printEnv*\tPrint the current environment\n" ++
        "*:copy <From chan name> <env name> <env password> <new env name> <new env password>*\tCopy an exist environment to a new environment\n"++
        "*:comment on <env name> <env password> (<comment>)*\tComment on an exist environment\n"++
        "*:commit <comment>* or *:commit*\tCommit current environment\n"++
        "*:printLog*\tPrint the committed versions of current environment\n"++
        "*:checkout <uuid>*\tCheck out privious committed version environment\n"++
        "*:reset <commit id> <env password>*\tReset to the commit version with that commit id\n"++
        "*:hard reset <commit id> <env password>*\tReset to commit version with that commit id and delete afterwards versions\n"++
        "*:script [-file]*\tRead from a file or a text block\n"++
        "*:help*\tPrint this help message\n"
      )
      case _ => CommandPassThrough
    }
  }

  def applyEnvCommand(code: String): Outcome[String, String] = {
    for {
      tokens <- Lexer(code)
      statementParse <- NewMapParser.statementParse(tokens)
      envCommands <- StatementInterpreter(statementParse, env)
    } yield {
      env = env.newCommands(envCommands)
      envCommands.map(_.toString).mkString("\n")
    }
  }
}

object EnvironmentInterpreter {
  val initialCommands: Vector[String] = Vector(
    "val MutableDescriptor: Type = Struct(versionType: (Count => Type), init: (versionType 0), commandType: Type, update: ((version: Count, current: versionType version, command: commandType) => versionType (increment version)))",
    "val CounterV: MutableDescriptor = (versionType: ((n: Count) => Count), init: 0, commandType: Struct(), update: ((version: Count, current: versionType version, command: commandType) => increment current))",
    "val StackV: ((T: Type, default: T) => MutableDescriptor) = (T: Type, default: T) => (versionType: ((n: Count) => T), init: default, commandType: T, update: ((version: Count, current: versionType version, command: commandType) => command))",
    "val SequenceV: ((T: Type, default: T) => MutableDescriptor) = (T: Type, default: T) => (versionType: ((n: Count) => Map n T default), init: (), commandType: T, update: ((version: Count, current: versionType version, command: commandType) => appendSeq version commandType default current command))",
    "val MapV: ((keyType: Type, valueType: Type, default: valueType) => MutableDescriptor) = (keyType: Type, valueType: Type, default: valueType) => (versionType: ((n: Count) => Map keyType valueType default), init: (), commandType: Map keyType valueType default, update: ((version: Count, current: versionType version, command: commandType) => appendMap keyType valueType default current command))"
  )
}
