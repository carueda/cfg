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
      val who  : String =  "Calvin"
      val other: Int    = $
    }
  }
}

@Cfg
case class WithOtherCfg(
                     reqInt  : Int,
                     reqStr  : String,
                     bar     : BarCfg
                   ) {
  val simple : SimpleCfg = SimpleCfg(11, "11")
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

@Cfg
case class WithDefaultCfg(
                      int : Int    = 21,
                      str : String = "someStr"
                    )

object Test extends TestSuite {
  val tests: framework.Tree[framework.Test] = this {

    "SimpleCfg" - {
      val conf = ConfigFactory.parseString(
        """
        int = 1
        str = "Hobbes"
      """)

      val cfg = SimpleCfg(conf)

      cfg.int  ==> 1
      cfg.str  ==> "Hobbes"
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
            other = 10
          }
        }
      """))

      bar.reqInt        ==> 9393
      bar.reqStr        ==> "reqStr"
      bar.long          ==> 1212100
      bar.foo.bool      ==> false
      bar.foo.baz.who   ==> "Calvin"
      bar.foo.baz.other ==> 10
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
              other = 20
            }
          }
        }
      """))

      cfg.reqInt        ==>  2130
      cfg.reqStr        ==> "reqStr"
      cfg.foo.bool      ==> true
      cfg.other         ==> 1010

      val bar = cfg.bar
      bar.reqInt        ==> 9393
      bar.reqStr        ==> "reqStr"
      bar.long          ==> 1212100
      bar.foo.bool      ==> false
      bar.foo.baz.who   ==> "Calvin"
      bar.foo.baz.other ==> 20

      cfg.simple.int    ==> 11
      cfg.simple.str    ==> "11"
    }
    
    "CompanionCfg" - {
      val conf = ConfigFactory.parseString(
        """
          |str = Hobbes
          |h = 9
        """.stripMargin)
      val cfg = CompanionCfg(conf)
      cfg.str  ==> "Hobbes"
      cfg.h    ==> 9
    }

    "WithDefaultCfg" - {
      val conf = ConfigFactory.parseString("")
      val cfg = WithDefaultCfg(conf)
      cfg.int  ==> 21
      cfg.str  ==> "someStr"
    }
  }
}
