package eri.commons.config

import java.io.File
import java.nio.file.Path
import java.time.Duration

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FunSpec

/**
 *
 * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K.Fitch</a>
 * @since 5/14/16
 */
class ExamplesTest extends FunSpec {
  describe("README examples") {
    it("is example 1") {
      val conf = ConfigFactory.load()
      // Required config value
      val timeout = conf.getDuration("akka.actor.typed.timeout")
      // Maybe existing config value
      val abort = if(conf.hasPath("app.shouldAbort"))
        conf.getBoolean("app.shouldAbort")
      else false
    }

    it("is example 2") {
      val conf = new SSConfig()
      // Required config value
      val timeout = conf.akka.actor.typed.timeout.as[Duration]
      // Maybe existing config value
      val abort = conf.app.shouldAbort.asOption[Boolean].getOrElse(false)
    }

    it("is example 3") {
      object MyConfig extends SSConfig()
      // SSC adds support for Java `Path` and `File` types
      val tmp = MyConfig.myapp.tempdir.as[Path]
      // NB: Typesafe Config merges in system properties for you
      val runtime = MyConfig.java.runtime.name.as[String]
    }

    it("is example 4") {
      object AkkaConfig extends SSConfig("akka")
      val akkaVersion = AkkaConfig.version.as[String]
      val timeout = AkkaConfig.actor.`creation-timeout`.as[Duration].getSeconds
    }

    it("is example 5") {
      val props = new SSConfig(ConfigFactory.load("myprops.properties"))
      val version = props.version.as[String]
    }

    it("is example 6") {
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
      val gv: Long = conf.sizeVal.asSize
      val pv: Path = conf.pathVal.as[Path]
      val zv: File = conf.fileVal.as[File]
      val cv: Config = conf.configVal.as[Config]
    }
  }
}
