import carueda.cfg._
import com.typesafe.config.ConfigFactory
import utest._

@Cfg
case class SimpleCfg(
                   int : Int,
                   str : String
                 )

@Cfg
case class BarCfg(
                   reqInt : Int,
                   reqStr : String
                 ) {

  val long : Long = $

  object foo {
    val bool  : Boolean = $

    object baz {
      val name : String = $
    }
  }
}

@Cfg
case class WithOtherCfg(
                     reqInt  : Int,
                     reqStr  : String,
                     bar     : BarCfg
                   ) {
  val simple : SimpleCfg = $
  object foo {
    val bool : Boolean = $
  }
  val other : Long = $
}

@Cfg
case class CompanionCfg(str : String) {
  val h: Int = $
}
object CompanionCfg {
  val xyz: String = "whatever"
  def apply(zyx: Int): Option[Any] = None
}


object Test extends TestSuite {
  val tests: framework.Tree[framework.Test] = this {

    "SimpleCfg" - {
      val conf = ConfigFactory.parseString(
        """
        int = 1
        str = "hobbes"
      """)

      val cfg = SimpleCfg(conf)

      cfg.int  ==> 1
      cfg.str  ==> "hobbes"
    }

    "BarCfg" - {
      val bar = BarCfg(ConfigFactory.parseString(
        """
        reqInt = 9393
        reqStr = "reqStr"
        long = 1212100
        foo {
          bool = false
          baz {
            name = calvin
          }
        }
      """))

      bar.reqInt        ==> 9393
      bar.reqStr        ==> "reqStr"
      bar.long          ==> 1212100
      bar.foo.bool      ==> false
      bar.foo.baz.name  ==> "calvin"
    }

    "WithOtherCfg" - {
      val cfg = WithOtherCfg(ConfigFactory.parseString(
        """
        reqInt = 2130
        reqStr = "reqStr"
        foo {
          bool = true
        }
        other = 1010
        bar {
          reqInt = 9393
          reqStr = "reqStr"
          long = 1212100
          foo {
            bool = false
            baz {
              name = calvin
            }
          }
        }
        simple {
          int = 0
          str = "aha"
        }
      """))

      cfg.reqInt        ==> 2130
      cfg.reqStr        ==> "reqStr"
      cfg.foo.bool      ==> true
      cfg.other         ==> 1010

      val bar = cfg.bar
      bar.reqInt        ==> 9393
      bar.reqStr        ==> "reqStr"
      bar.long          ==> 1212100
      bar.foo.bool      ==> false
      bar.foo.baz.name  ==> "calvin"

      cfg.simple.int    ==> 0
      cfg.simple.str    ==> "aha"
    }
    
    "CompanionCfg" - {
      val conf = ConfigFactory.parseString(
        """
          |str = hobbes
          |h = 9
        """.stripMargin)
      val cfg = CompanionCfg(conf)
      cfg.str  ==> "hobbes"
      cfg.h    ==> 9
    }
  }
}
