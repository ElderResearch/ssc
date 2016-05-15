package eri.commons.config

import java.io.File
import java.net.InetAddress
import java.nio.file.{Paths, Path}
import java.time.Duration
import java.util.UUID

import com.typesafe.config.Config

/**
 * Typeclass specification and default implementations for reading a specific type from a `Config`
 *
 * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K.Fitch</a>
 * @since 5/15/16
 */
trait ConfigReader[T] {
  def apply(path: String, config: Config): T
}

object ConfigReader {
  implicit object BooleanReader extends ConfigReader[Boolean] {
    override def apply(path: String, config: Config): Boolean = config.getBoolean(path)
  }
  implicit object IntReader extends ConfigReader[Int] {
    override def apply(path: String, config: Config): Int = config.getInt(path)
  }
  implicit object DoubleReader extends ConfigReader[Double] {
    override def apply(path: String, config: Config): Double = config.getDouble(path)
  }
  implicit object LongReader extends ConfigReader[Long] {
    override def apply(path: String, config: Config): Long = config.getLong(path)
  }
  implicit object FloatReader extends ConfigReader[Float] {
    override def apply(path: String, config: Config): Float = config.getDouble(path).toFloat
  }
  implicit object StringReader extends ConfigReader[String] {
    override def apply(path: String, config: Config): String = config.getString(path)
  }
  implicit object DurationReader extends ConfigReader[Duration] {
    override def apply(path: String, config: Config): Duration = config.getDuration(path)
  }
  implicit object PathReader extends ConfigReader[Path] {
    override def apply(path: String, config: Config): Path = Paths.get(config.getString(path))
  }
  implicit object FileReader extends ConfigReader[File] {
    override def apply(path: String, config: Config): File = PathReader(path, config).toFile
  }
  implicit object UUIDReader extends ConfigReader[UUID] {
    override def apply(path: String, config: Config): UUID = UUID.fromString(config.getString(path))
  }
  implicit object InetAddrReader extends ConfigReader[InetAddress] {
    override def apply(path: String, config: Config): InetAddress = InetAddress.getByName(config.getString(path))
  }
  implicit object ConfigReader extends ConfigReader[Config] {
    override def apply(path: String, config: Config): Config = config.getConfig(path)
  }
  implicit object AnyRefReader extends ConfigReader[AnyRef] {
    override def apply(path: String, config: Config): AnyRef = config.getObject(path)
  }

}
