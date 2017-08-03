import carueda.cfg._
import com.typesafe.config.ConfigFactory
import utest._

@Cfg
class BarCfg(c: com.typesafe.config.Config) {
  val reqInt  : Int    = $
  val reqStr  : String = $

  object foo {
    val bool  : Boolean = $

    object baz {
      val long : Long = $
      val name : String = $
    }
  }
}

@Cfg
case class CaseCfg(
                    reqInt  : Int,
                    reqStr  : String,
                    bar     : BarCfg
                  ) {
  object foo {
    val bool : Boolean = $
  }
}

object Test extends TestSuite {
  val tests: framework.Tree[framework.Test] = this {

    "BarCfg" - {
      val conf = ConfigFactory.parseString(
        """
        reqInt = 9393
        reqStr = "reqStr"
        foo {
          bool = false
          baz {
            long = 1212100
            name = calvin
          }
        }
      """)

      val bar = new BarCfg(conf)

      bar.reqInt        ==> 9393
      bar.reqStr        ==> "reqStr"
      bar.foo.bool      ==> false
      bar.foo.baz.long  ==> 1212100
      bar.foo.baz.name  ==> "calvin"
    }

    "CaseCfg" - {
      val cfg = CaseCfg(ConfigFactory.parseString(
        """
        reqInt = 2130
        reqStr = "reqStr"
        foo {
          bool = true
        }
        bar {
          reqInt = 9393
          reqStr = "reqStr"
          foo {
            bool = false
            baz {
              long = 1212100
              name = calvin
            }
          }
        }
      """))


      cfg.reqInt        ==> 2130
      cfg.reqStr        ==> "reqStr"
      cfg.foo.bool      ==> true

      val bar = cfg.bar
      bar.reqInt        ==> 9393
      bar.reqStr        ==> "reqStr"
      bar.foo.bool      ==> false
      bar.foo.baz.long  ==> 1212100
      bar.foo.baz.name  ==> "calvin"
    }
  }
}
