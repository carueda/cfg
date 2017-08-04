import java.time.Duration

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

@Cfg
case class WithOptCfg(
                      int    : Option[Int],
                      str    : Option[String],
                      simple : Option[SimpleCfg]
                    ) {

  val simple2: Option[SimpleCfg] = $
}

@Cfg
case class WithListCfg(
                      ints  : List[Int],
                      strs  : List[String],
                      simples1 : List[SimpleCfg],
                      simpless : List[List[SimpleCfg]]
                    ) {

  val strss   : List[List[String]] = $
  val strsss  : List[List[List[String]]] = $
  val simples2: List[SimpleCfg] = $
}

@Cfg
case class WithDurationCfg(
                      dur    : Duration
                      ,durOpt : Option[Duration]
                      ,durs   : List[Duration]
                    ) {

  val dur1    : Duration = $
  val durOpt1 : Option[Duration] = $
  val durs1   : List[Duration] = $
}

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

    "WithOptCfg" - {
      val conf = ConfigFactory.parseString(
        """
          int =  8
          simple2 {
            int = 1
            str = str
          }
        """.stripMargin)
      val cfg = WithOptCfg(conf)
      cfg.int     ==> Some(8)
      cfg.str     ==> None
      cfg.simple  ==> None
      cfg.simple2 ==> Some(SimpleCfg(1, "str"))
    }

    "WithListCfg" - {
      val conf = ConfigFactory.parseString(
        """
          ints  = [1,2,3]
          strs  = [ hello, world ]
          strss = [
            [ abc, de ]
            [ fgh ]
          ]
          strsss = [
            [
              [ a, b ]
              [ c, d, e ]
            ],
            [
              [ x, y ]
              [ j, k ]
            ]
          ]
          simples1 = [
            { int = 1, str = "1" }
          ]
          simpless = [[
            { int = 9, str = "9" }
          ]]
          simples2 = [
            { int = 2, str = "2" },
            { int = 3, str = "3" },
          ]
        """.stripMargin)
      val cfg = WithListCfg(conf)
      cfg.ints   ==> List(1, 2, 3)
      cfg.strs   ==> List("hello", "world")
      cfg.strss  ==> List(
        List("abc", "de"),
        List("fgh")
      )
      cfg.strsss ==> List(
        List(
          List("a", "b"), List("c", "d", "e")
        ),
        List(
          List("x", "y"), List("j", "k")
        )
      )
      cfg.simples1 ==> List(
        SimpleCfg(1, "1")
      )
      cfg.simpless ==> List(
        List(SimpleCfg(9, "9"))
      )
      cfg.simples2 ==> List(
        SimpleCfg(2, "2"),
        SimpleCfg(3, "3")
      )
    }

    "WithDurationCfg" - {
      val conf = ConfigFactory.parseString(
        """
          dur = 6h
          durs = [ 3600s, 1d ]
          dur1 = 3s
          durOpt1 = 3h
          durs1 = [ 120m ]
        """.stripMargin)
      val cfg = WithDurationCfg(conf)
      cfg.dur.toHours  ==> 6
      cfg.durOpt ==> None
      cfg.durs.map(_.toHours)  ==> List(1, 24)
      cfg.dur1.toMillis ==> 3000
      cfg.durOpt1.map(_.toMinutes) ==> Some(180)
      cfg.durs1.map(_.toHours)  ==> List(2)
    }
  }
}
