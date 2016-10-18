package eri.commons.config

import java.io.File
import java.net.InetAddress
import java.nio.file.{Paths, Path}
import java.time.Duration
import java.util.UUID
import scala.collection.JavaConversions._
import com.typesafe.config.{ConfigMemorySize, Config}

/**
 * Typeclass specification and default implementations for reading a specific type from a `Config`
 *
 * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K.Fitch</a>
 * @since 5/15/16
 */
trait ConfigReader[T] {
  def apply(path: String, config: Config): T
}

trait StringReader[T] {
  def apply(valueStr: String): T
}

object StringReader {
  // ---- Custom Type Readers ----
  implicit object PathReader extends StringReader[Path] {
    def apply(valueStr: String): Path = Paths.get(valueStr)
  }
  implicit object FileReader extends StringReader[File] {
    def apply(valueStr: String): File = PathReader(valueStr).toFile
  }
  implicit object UUIDReader extends StringReader[UUID] {
    def apply(valueStr: String): UUID = UUID.fromString(valueStr)
  }
  implicit object InetAddrReader extends StringReader[InetAddress] {
    def apply(valueStr: String): InetAddress = InetAddress.getByName(valueStr)
  }
}

object ConfigReader {
  // ---- Native Typesafe Config Type Readers ----
  implicit object BooleanReader extends ConfigReader[Boolean] {
    def apply(path: String, config: Config): Boolean = config.getBoolean(path)
  }
  implicit object BooleanSeqReader extends ConfigReader[Seq[Boolean]] {
    def apply(path: String, config: Config): Seq[Boolean] =
      config.getBooleanList(path).map(_.booleanValue())
  }
  implicit object IntReader extends ConfigReader[Int] {
    def apply(path: String, config: Config): Int = config.getInt(path)
  }
  implicit object IntSeqReader extends ConfigReader[Seq[Int]] {
    def apply(path: String, config: Config): Seq[Int] =
      config.getIntList(path).map(_.intValue())
  }
  implicit object DoubleReader extends ConfigReader[Double] {
    def apply(path: String, config: Config): Double = config.getDouble(path)
  }
  implicit object DoubleSeqReader extends ConfigReader[Seq[Double]] {
    def apply(path: String, config: Config): Seq[Double] =
      config.getDoubleList(path).map(_.doubleValue())
  }
  implicit object LongReader extends ConfigReader[Long] {
    def apply(path: String, config: Config): Long = config.getLong(path)
  }
  implicit object LongSeqReader extends ConfigReader[Seq[Long]] {
    def apply(path: String, config: Config): Seq[Long] =
      config.getLongList(path).map(_.longValue())
  }
  implicit object FloatReader extends ConfigReader[Float] {
    def apply(path: String, config: Config): Float =
      config.getDouble(path).toFloat
  }
  implicit object FloatSeqReader extends ConfigReader[Seq[Float]] {
    def apply(path: String, config: Config): Seq[Float] =
      LongSeqReader(path, config).map(_.toFloat)
  }
  implicit object MemorySizeReader extends ConfigReader[ConfigMemorySize] {
    def apply(path: String, config: Config): ConfigMemorySize = config.getMemorySize(path)
  }
  implicit object MemorySizeSeqReader extends ConfigReader[Seq[ConfigMemorySize]] {
    def apply(path: String, config: Config): Seq[ConfigMemorySize] =
      config.getMemorySizeList(path)
  }
  implicit object StringReader extends ConfigReader[String] {
    def apply(path: String, config: Config): String = config.getString(path)
  }
  implicit object StringSeqReader extends ConfigReader[Seq[String]] {
    def apply(path: String, config: Config): Seq[String] =
      config.getStringList(path)
  }
  implicit object DurationReader extends ConfigReader[Duration] {
    def apply(path: String, config: Config): Duration = config.getDuration(path)
  }
  implicit object DurationSeqReader extends ConfigReader[Seq[Duration]] {
    def apply(path: String, config: Config): Seq[Duration] = config.getDurationList(path)
  }
  implicit object ConfigReader extends ConfigReader[Config] {
    def apply(path: String, config: Config): Config = config.getConfig(path)
  }
  implicit object ConfigSeqReader extends ConfigReader[Seq[Config]] {
    def apply(path: String, config: Config): Seq[Config] = config.getConfigList(path)
  }
  implicit object AnyRefReader extends ConfigReader[AnyRef] {
    def apply(path: String, config: Config): AnyRef = config.getObject(path)
  }
  implicit object AnyRefSeqReader extends ConfigReader[Seq[AnyRef]] {
    def apply(path: String, config: Config): Seq[AnyRef] =
      config.getObjectList(path)
  }

  /** Given a `StringReader[T]`, creates a `ConfigReader[T]` */
  implicit def customConfigReader[T: StringReader]: ConfigReader[T] = new ConfigReader[T] {
    override def apply(path: String, config: Config): T = {
      val reader = implicitly[StringReader[T]]
      reader(config.getString(path))
    }
  }

  /** Given a `StringReader[T]`, creates a `ConfigReader[Seq[T]]` */
  implicit def customConfigSeqReader[T: StringReader]: ConfigReader[Seq[T]] = new ConfigReader[Seq[T]] {
    def apply(path: String, config: Config): Seq[T] = {
      val reader = implicitly[StringReader[T]]
      config.getStringList(path).map(reader.apply)
    }
  }
}