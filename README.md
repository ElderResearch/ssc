# Simple Scala Config

_Typesafe Config wrapped in a [`Dynamic`][dsd] blanket._

[![Build Status](https://travis-ci.org/ElderResearch/ssc.svg?branch=develop)](https://travis-ci.org/ElderResearch/ssc)

## Overview

[_Typesafe Config_][tc] is about as perfect as an application configuration system can be. [HOCON][hocon] is fantastic to work with, and the underlying Java implementation is both robust and consistent.

However, as a Scala developer, I'm not immune to wanting a little extra "sugar" mixed in with the robustly fantastic Java goodness. _Simple Scala Config_ is an extremely thin wrapper (less than 100 SLOCs) around _Typesafe Config_, allowing retrieval of configuration values using field-dereference syntax:

So instead of doing this:

```scala
// Load default config file
val conf = ConfigFactory.load()
// Get required config value
val timeout = conf.getDuration("akka.actor.typed.timeout")
// Get maybe existing config value
val abort = if(conf.hasPath("app.shouldAbort"))
  conf.getBoolean("app.shouldAbort")
else false
```

You can do this:

```scala
// Load default config file
val conf = new SSConfig()
// Get required config value
val timeout = conf.akka.actor.typed.timeout.as[Duration]
// Get maybe existing config value
val abort = conf.app.shouldAbort.asOption[Boolean].getOrElse(false)
```

_Simple Scala Config_ is able to do this via the use of Scala's [`Dynamic`][dsd] facility. I find using this wrapper a bit more "idiomaticaly Scala" without being too far removed from the core library.  The downside to using it is the configuration dereferencing _looks_ like it is valid--due to the compiler accepting it--but in reality, dereferencing errors will be detected at runtime (just as they would with the core _Typesafe Config_), unless you always use the `asOption[T]` transform.

## Using

The library is published via bintray. Add this to your sbt build definitions:

```scala
resolvers += "ERI OSS" at "http://dl.bintray.com/elderresearch/OSS"

libraryDependencies += "com.elderresearch" %% "ssc" % "0.1.0"
```

It will transitively pull in the Typesafe Config and Scala Reflection libraries:

```scala
"com.typesafe" % "config" % "1.3.0"
"org.scala-lang" %  "scala-reflect"  % scalaVersion.value
```

Note: **Requires Java 8**, as does _Typesafe Config_ >= `1.3.0`

## Examples

### Basic

To use the default config loader in the root scope, instantiate or subclass `SSConfig`:


``` scala
object MyConfig extends SSConfig()
// SSC adds support for Java `Path` and `File` types
val tmp = MyConfig.myapp.tempdir.as[Path]
// NB: Typesafe Config merges in system properties for you
val runtime = MyConfig.java.runtime.name.as[String]
```

### Nested Scope

To specify a nested scope, pass the path string into the constructor:

```scala
object AkkaConfig extends SSConfig("akka")
val akkaVersion = AkkaConfig.version.as[String]
val timeout = AkkaConfig.actor.`creation-timeout`.as[Duration].getSeconds
```

### Custom Loading

To bypass the default config loading, pass in results from `ConfigFactory` (which also supports traditional Java properties files):

```scala
val props = new SSConfig(ConfigFactory.load("myprops.properties"))
val version = props.version.as[String]
```

### Supported types

The standard _Typesafe Config_ types are supported via a type parameter to the `as[T]` and `asOption[T]` methods:


```scala

val src =
  """
    | booleanVal = true
    | intVal = 3
    | doubleVal = 1e-200
    | longVal = 4878955355435272204
    | floatVal = 3.14
    | stringVal = "Ceci n'est pas une pipe."
    | durationVal = 400ns
    | sizeVal = 0.5GB
    | pathVal = /dev/null
    | fileVal = /dev/zero
    | configVal = { a = 1, b = 2, c = 3 }
  """.stripMargin

val conf = new SSConfig(ConfigFactory.parseString(src))
val bv: Boolean = conf.booleanVal.as[Boolean]
val iv: Int = conf.intVal.as[Int]
val dv: Double = conf.doubleVal.as[Double]
val lv: Long = conf.longVal.as[Long]
val fv: Float = conf.floatVal.as[Float]
val sv: String = conf.stringVal.as[String]
val tv: Duration = conf.durationVal.as[Duration]
```

The "binary storage size" type supported by _Typesafe Config_ via the `Config.asBytes(String): Long` method has a special accessor, since the return type is `Long` and not (unfortunately) a unique type unto itself:

```scala
val gv: Long = conf.sizeVal.asSize
```

In addition to the types supported by _Typesafe Config_, Java's `File` and `Path` types are also available:


```scala
val pv: Path = conf.pathVal.as[Path]
val zv: File = conf.fileVal.as[File]
```

Others can be supported by PR :-)

Access to the underlying `Config` object is also supported:

```scala
val cv: Config = conf.configVal.as[Config]
```

## Bugs/Missing Features

* Currently does not support list config values (On the TODO list.)

## License

The license is Apache 2.0. See [LICENSE](LICENSE).


[tc]: https://github.com/typesafehub/config
[hocon]: https://github.com/typesafehub/config/blob/master/HOCON.md
[dsd]: http://www.scala-lang.org/api/2.11.8/#scala.Dynamic
