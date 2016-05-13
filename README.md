# Simple Scala Config
 
_Typesafe Config wrapped in a [`Dynamic`](dsd) blanket._

## Overview 

[Typesafe Config](https://github.com/typesafehub/config) is about as perfect as an application configuration system can be. [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) is fantastic to work with, and the underlying Java implementation is robust and consistent. However, as a Scala developer, I'm not immune to wanting a little extra "sugar" mixed in with the robustly fantastic. Simple Scala Config is an *extremely* thin wrapper around Typesafe Config to allow the retrieval of configuration values via the use of field dereference syntax:

```
val conf = new SSConfig()
// Expected config value
val timeout = conf.akka.actor.typed.timeout.as[Duration]
// Maybe existing config value
val maybeAbort = conf.app.shouldAbort.asOption[Boolean]
```

Simple Scala Config (ssc) is able to do this via the use of the [`Dynamic`](dsd) facility. The cost of the nice syntax is that the configuration dereferencing _looks_ like it is valid--due to the compiler accepting it--but in reality errors will be detected at runtime. So buyer beware!

## Using

The library is published via XXX. Add this to your sbt build definitions:


```
libraryDependencies += "com.elderresearch" %% "ssc" % "0.1.0-SNAPSHOT"
```

## License

The license is Apache 2.0. See [LICENSE](LICENSE).


[dsd]: http://www.scala-lang.org/api/2.11.8/#scala.Dynamic




