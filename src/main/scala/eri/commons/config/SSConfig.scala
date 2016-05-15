/*
 * Copyright (c) 2016. Elder Research, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eri.commons.config

import com.typesafe.config.{Config ⇒ TConfig, ConfigException, ConfigFactory}

import scala.language.dynamics

/**
 * This class serves as a convenience entry point into configuration items.
 * With a configuration like this:
 *
 * {{{
 *   farmer.fred {
 *       foo = 34.0
 *       bar = 9
 *       other {
 *           fred = "fun"
 *       }
 *   }
 * }}}
 *
 * You can get the value with:
 * {{{
 *   object Config extends SSConfig()
 *   val f = Config.farmer.fred.foo.as[Float]
 *   val b = Config.farmer.fred.bar.as[Int]
 *   val n = Config.farmer.fred.other.fred.as[String]
 * }}}
 *
 * To create a config with an inner scope, provide the path to the constructor:
 * {{{
 *   object Config2 extends SSConfig("farmer.fred")
 *   val f2 = Config2.foo.as[Float]
 *   val b2 = Config2.bar.as[Int]
 *   val n2 = Config2.other.fred.as[String]
 * }}}
 *
 * @param relPath Scoping path specifier
 * @param relConfig Configuration data
 * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K. Fitch</a>
 * @since 3/24/16
 */
class SSConfig(relPath: String = "", relConfig: TConfig = ConfigFactory.load()) extends Dynamic {
  def this(config: TConfig) = this("", config)

  /**
   * Pulls a value from the configuration with an expected type
   * Supported types are: `Boolean, Int, Double, Long, Float, String, Duration, Path, File, AnyRef, Config`
   * @tparam A expected type as type parameter
   * @return configuration value
   */
  @throws[ConfigException.Generic]("Type parameter not specified or not supported")
  def as[A: ConfigReader] = {
    val reader = implicitly[ConfigReader[A]]
    reader(relPath, relConfig)
  }

  /** Special case accessor for values defined in config using typical memory size suffixes (G, M, K etc.)*/
  def asSize = relConfig.getBytes(relPath)

  /**
   * Same as `as[A]` except result is wrapped in an Option[A].
   * You get a `None` instead of a thrown exception when the configuration path doesn't exist
   * @tparam A expected type as type parameter
   * @return configuration value
   */
  def asOption[A: ConfigReader] = {
    if(relConfig.hasPath(relPath)) Some(as[A])
    else None
  }

  /** Traversal magic as supported by `scala.Dynamic`. */
  def selectDynamic(name: String) = {
    val next = if (relPath.nonEmpty && relConfig.hasPath(relPath))
      relConfig.getConfig(relPath)
    else relConfig
    new SSConfig(name, next)
  }
}
