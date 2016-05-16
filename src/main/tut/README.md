# Simple Scala Config

_Typesafe Config wrapped in a [`Dynamic`][dsd] blanket._

[![Build Status](https://travis-ci.org/ElderResearch/ssc.svg?branch=master)](https://travis-ci.org/ElderResearch/ssc)
[ ![Download](https://api.bintray.com/packages/elderresearch/OSS/ssc/images/download.svg) ](https://bintray.com/elderresearch/OSS/ssc/_latestVersion)
[![Gitter](https://badges.gitter.im/ElderResearch/ssc.svg)](https://gitter.im/ElderResearch/ssc?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

## Overview

[_Typesafe Config_][tc] is about as perfect as an application configuration system can be. [HOCON][hocon] is fantastic to work with, and the underlying Java implementation is both robust and consistent.

However, as a Scala developer, I'm not immune to wanting a little extra "sugar" mixed in with the robustly fantastic Java goodness. _Simple Scala Config_ is an extremely thin wrapper (less than 100 SLOCs plus `Reader`s) around _Typesafe Config_, allowing retrieval of configuration values using field-dereference syntax:

So instead of doing this:

```tut:invisible
import eri.commons.config._
import java.io.File
import java.nio.file.Path
import java.net.InetAddress
import java.util.UUID
import java.time.Duration
import com.typesafe.config._
```

```tut:silent
// Load default config file
val conf = ConfigFactory.load()
```
```tut:book
// Get required config value
val timeout = conf.getDuration("akka.actor.typed.timeout")
// Get maybe existing config value
val abort = if (conf.hasPath("app.shouldAbort")) {
  conf.getBoolean("app.shouldAbort")
} else false
```

You can do this:

```tut:book
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
libraryDependencies += "com.elderresearch" %% "ssc" % "0.2.0"
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

```tut:book
object MyConfig extends SSConfig()
// SSC adds support for Java `Path`
val tmp = MyConfig.myapp.tempdir.as[Path]
// NB: Typesafe Config merges in system properties for you
val runtime = MyConfig.java.runtime.name.as[String]
```

### Nested Scope

To specify a nested scope, pass the path string into the constructor:

```tut:book
object AkkaConfig extends SSConfig("akka")
val akkaVersion = AkkaConfig.version.as[String]
val timeout = AkkaConfig.actor.`creation-timeout`.as[Duration].getSeconds
```

### Custom Loading

To bypass the default config loading, pass in results from `ConfigFactory` (which also supports traditional Java properties files):

```tut:book
val props = new SSConfig(ConfigFactory.load("myprops.properties"))
val version = props.version.as[String]
```

### Supported Types

#### Standard _Typesafe Config_ Types

The standard _Typesafe Config_ types are supported via a type parameter to the `as[T]` and `asOption[T]` methods:

```tut:silent
// Define an example in-line config
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
    | sizeVals = [ 0.5K, 1M, 2G, 3T, 4P ]
    | pathVal = /dev/null
    | fileVal = /dev/zero
    | addrVal = 192.168.34.42
    | uuidVal = fed6cc29-1cc4-46ed-9c04-56261730f44c
    | timeVals = [ 1m, 5m, 15m, 30m, 45m, 1h ]
    | phoneVal = "1-881-555-1212"
    | configVal = { a = 1, b = 2, c = 3 }
  """.stripMargin
val conf = new SSConfig(ConfigFactory.parseString(src))
```
```tut:book
val bv: Boolean = conf.booleanVal.as[Boolean]
val iv: Int = conf.intVal.as[Int]
val dv: Double = conf.doubleVal.as[Double]
val lv: Long = conf.longVal.as[Long]
val fv: Float = conf.floatVal.as[Float]
val sv: String = conf.stringVal.as[String]
val tv: Duration = conf.durationVal.as[Duration]
val mv: ConfigMemorySize = conf.sizeVal.as[ConfigMemorySize]
```

Access to the underlying `Config` object is also supported:

```tut:book
val cv: Config = conf.configVal.as[Config]
```

#### Extended Type Support

In addition to the types supported by _Typesafe Config_, converters for some additional Java types are provided (see [Defining New Readers](#defining-new-readers) below for instructions on adding your own.):

```tut:book
val pv: Path = conf.pathVal.as[Path]
val zv: File = conf.fileVal.as[File]
val av: InetAddress = conf.addrVal.as[InetAddress]
val uv: UUID = conf.uuidVal.as[UUID]
```

### Array/Sequence Values

HOCON supports array values. To retrieve this values, use `as[Seq[T]]` or `asOption[Seq[T]]`. For any type `T` you should be able to also retrieve `Seq[T]` if defined in an HOCON array.

```tut:book
val times = conf.timeVals.as[Seq[Duration]]
val sizes = conf.sizeVals.as[Seq[ConfigMemorySize]]
```

### Defining New `Reader`s

Both standard and core types are extracted through the `as[T]` and `asOption[T]` methods via [`ConfigReader[T]`](src/main/scala/eri/commons/config/ConfigReader.scala) and `StringReader[T]` type classes. To define a converter from a `String` to your desired type `Foo`, place in scope an `implicit` instance of `StringReader[Foo]`. For example, supposed we wanted to support reading in a phone number type:  

```tut:book
case class PhoneNumber(countryCode: Int, areaCode: Int, exchange: Int, extension: Int)
implicit object PhoneReader extends StringReader[PhoneNumber] {
  val pat = "(\\d)-(\\d+)-(\\d+)-(\\d+)".r
  def apply(valueStr: String): PhoneNumber = {
    val pat(cc, ac, ex, et) = valueStr
    PhoneNumber(cc.toInt, ac.toInt, ex.toInt, et.toInt)
  }
}
val phone = conf.phoneVal.as[PhoneNumber]
```

## License

The license is Apache 2.0. See [LICENSE](LICENSE).


[tc]: https://github.com/typesafehub/config
[hocon]: https://github.com/typesafehub/config/blob/master/HOCON.md
[dsd]: http://www.scala-lang.org/api/2.11.8/#scala.Dynamic
