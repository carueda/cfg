// See https://github.com/carueda/cfg/issues/1
// Fix implemented (0.0.8), but tests commented out
// because of external factors.

/*
import carueda.cfg._
import com.typesafe.config.ConfigFactory
import utest._

object TestBackticks extends TestSuite {
  val tests: framework.Tree[framework.Test] = this {

    "backtick1" - {
      @Cfg
      case class WithBackticks(`my-param`: Int = 1)

      val cfg = WithBackticks(ConfigFactory.parseString(
        "my-param = 2"
      ))

      cfg.`my-param` ==> 2
    }

    "backtick2" - {
      @Cfg
      case class WithBackticks() {
        object `foo-object` {
          val `bar-baz`: Int = $
          val `name`: String = "xyz"
        }
      }

      val cfg = WithBackticks(ConfigFactory.parseString(
        """my-param = 2
          |foo-object.bar-baz = 11
        """.stripMargin
      ))

      cfg.`foo-object`.`bar-baz` ==> 11
      cfg.`foo-object`.`name` ==> "xyz"
    }
  }
}
*/
