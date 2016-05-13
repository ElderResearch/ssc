# Simple Scala Config

_Typesafe Config wrapped in a [`Dynamic`](dsd) blanket._

## Overview

[Typesafe Config](https://github.com/typesafehub/config) is about as perfect as an application configuration system can be. [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) is fantastic to work with, and the underlying Java implementation is robust and consistent. However, as a Scala developer, I'm not immune to wanting a little extra "sugar" mixed in with the robustly fantastic. Simple Scala Config is an *extremely* thin wrapper (less than 100 SLOCs) around Typesafe Config, allowing the retrieval of configuration values via the field dereference syntax:

```scala
val conf = new SSConfig()
// Expected config value
val timeout = conf.akka.actor.typed.timeout.as[Duration]
// Maybe existing config value
val abort = conf.app.shouldAbort.asOption[Boolean].getOrElse(false)
```

Simple Scala Config is able to do this via the use of the [`Dynamic`](dsd) facility. The cost of the nice syntax is that the configuration dereferencing _looks_ like it is valid--due to the compiler accepting it--but in reality errors will be detected at runtime. So buyer beware!

## Using

The library is published via XXX. Add this to your sbt build definitions:

```scala
libraryDependencies += "com.elderresearch" %% "ssc" % "0.1.0-SNAPSHOT"
```

It will transitively pull in the Typesafe Config and Scala Reflection libraries:

```scala
"com.typesafe" % "config" % "1.3.0"
"org.scala-lang" %  "scala-reflect"  % scalaVersion.value
```

## Examples

Example usage:

``` scala
// To use the default config loader in the root scope,
// instantiate or subclass SSConfig
object MyConfig extends SSConfig()
val tmp = MyConfig.myapp.tempdir.as[Path]
// Typesafe Config merges in system properties
val runtime = MyConfig.java.runtime.name.as[String]

// To specify a nested scope,
// pass the path string into the constructor
object AkkaConfig extends SSConfig("akka")
val akkaVersion = AkkaConfig.version.as[String]
val timeout = AkkaConfig.actor.`creation-timeout`.as[Duration].getSeconds

// To bypass the default config loading, pass in results from 
// ConfigFactory (which also supports traditional Java properties files) 
val props = SSConfig(ConfigFactory.load("myprops.properties"))
val version = props.version.as[String]
```

## Bugs/Missing Features

* Currently does not support list config values (TODO)


## License

The license is Apache 2.0. See [LICENSE](LICENSE).


[dsd]: http://www.scala-lang.org/api/2.11.8/#scala.Dynamic
