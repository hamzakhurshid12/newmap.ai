# newmap.ai
This is the repo cloned from the newmap.ai project: language and interpreter

# Changes in this fork

 - Back arrow allows you to edit what you've already typed in (regular stdin only supports backspace)
 - Up and down arrows allows you to cycle through your history of commands. It also keeps track of history.

# Usage

Compile:
sbt compile

Run newmap command line
sbt run

Run tests:
sbt test

Compile and Run:
sbt compile && sbt run
