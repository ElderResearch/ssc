package eri.commons.config

import java.io.File
import java.net.{ UnknownHostException, InetAddress }
import java.nio.file.{ Paths, Path }
import java.time.Duration
import java.util.UUID

import com.typesafe.config.{ ConfigException, ConfigFactory }
import org.scalatest.FunSpec

/**
 * Test rig for Scala Simple Config.
 *
 * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K.Fitch</a>
 * @since 5/12/16
 */
class SSConfigTest extends FunSpec {
  describe("initialization") {
    it("should allow creation from root config") {
      val conf = new SSConfig()
      assert(conf.ints.fortyTwoAgain.as[Int] === 42)
    }
    it("should allow creation from nested config") {
      val conf = new SSConfig("floats")
      assert(conf.fortyTwoPointOne.as[Float] === 42.1f)
    }
    it("should allow creation from externally configured config") {
      import scala.collection.JavaConverters._
      val inline = Map("one" -> Int.box(1), "two" -> Int.box(2)).asJava
      val conf = new SSConfig(ConfigFactory.parseMap(inline))
      assert(conf.two.as[Int] === 2)

      val prefs = new SSConfig(ConfigFactory.load("myprops.properties"))
      assert(prefs.something.as[String] === "nothing")
    }
  }
  describe("base configuration types") {
    val conf = new SSConfig()
    it("should support integers") {
      assert(conf.ints.fortyTwo.as[Int] === 42)
      assert(conf.ints.fortyTwo.as[Long] === 42l)
    }
    it("should support floating point numbers") {
      assert(conf.floats.pointThirtyThree.as[Float] === 0.33f)
      assert(conf.floats.pointThirtyThree.as[Double] === 0.33)
    }
    it("should support booleans") {
      assert(conf.booleans.trueAgain.as[Boolean])
      assert(!conf.booleans.`false`.as[Boolean])
    }
    it("should support strings") {
      assert(conf.strings.concatenated.as[String] === "null bar 42 baz true 3.14 hi")
    }
    it("should support durations") {
      assert(conf.durations.halfSecond.as[Duration] === Duration.ofMillis(500))
    }
    it("should support sizes") {
      assert(conf.memsizes.meg.asSize === 1024 * 1024)
    }
    it("should support paths") {
      assert(conf.system.userhome.as[Path] === Paths.get(sys.props("user.home")))
      assert(conf.system.userhome.as[File] === Paths.get(sys.props("user.home")).toFile)
    }
    it("should support UUIDs") {
      val uuid = conf.extended.uuid.as[UUID]
      assert(uuid.version() === 4)

      intercept[IllegalArgumentException] {
        conf.extended.notUuid.as[UUID]
      }
    }
    it("should support InetAddress") {
      val addr1 = conf.extended.addr1.as[InetAddress]
      val addr2 = conf.extended.addr2.as[InetAddress]
      val addr3 = conf.extended.addr3.as[InetAddress]

      intercept[Exception] {
        conf.extended.notAddr.as[InetAddress]
      }
    }
  }
  describe("behavior of missing or `Option`al config values") {
    val conf = new SSConfig()
    it("should support `Some`") {
      assert(conf.system.javaversion.asOption[String] === Some(sys.props("java.version")))
    }
    it("should support `None`") {
      assert(conf.am.not.here.asOption[Int] === None)
    }
    it("should support default `ConfigException` behavior") {
      intercept[ConfigException] {
        conf.system.oops.as[String]
      }
    }
  }
  describe("sequence configuration types") {
    val conf = new SSConfig()
    it("should support boolean sequence") {
      assert(conf.arrays.ofBoolean.as[Seq[Boolean]] === Seq(true, false))
    }
    it("should support int sequence") {
      assert(conf.arrays.ofInt.as[Seq[Int]] === Seq(1, 2, 3))
    }
    it("should support double sequence") {
      assert(conf.arrays.ofDouble.as[Seq[Double]] === Seq(3.14, 4.14, 5.14))
    }
    it("should support string sequence") {
      assert(conf.arrays.ofString.as[Seq[String]] === Seq("a", "b", "c"))
    }
    it("should support duration sequence") {
      assert(conf.durations.secondsList.as[Seq[Duration]] === Seq(1l, 2l, 3l, 4l).map(Duration.ofSeconds))
    }
    it("should support Inet address sequence") {
      assert(conf.extended.addresses.as[Seq[InetAddress]] === Seq("192.168.32.42", "0:0:0:0:0:0:0:1").map(InetAddress.getByName))
    }

  }
  describe("miscellaneous features") {
    val conf = new SSConfig()
    it("should support system properties") {
      assert(conf.java.runtime.name.as[String].contains("Java"))
    }
    it("should allow custom types") {
      case class PhoneNumber(countryCode: Int, areaCode: Int, exchange: Int, extension: Int)
      implicit object PhoneReader extends StringReader[PhoneNumber] {
        val pat = "(\\d)-(\\d+)-(\\d+)-(\\d+)".r
        def apply(valueStr: String): PhoneNumber = {
          val pat(cc, ac, ex, et) = valueStr
          PhoneNumber(cc.toInt, ac.toInt, ex.toInt, et.toInt)
        }
      }

      val phone = conf.my.phone.as[PhoneNumber]
      assert(phone.extension === 1212)
    }
  }
}
