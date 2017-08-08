import scala.concurrent.duration._

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

object Test extends TestSuite {
  val tests: framework.Tree[framework.Test] = this {

    "ExampleCfg" - {
      @Cfg
      case class ExampleCfg(num: Int, str: String = "foo", dur: Option[Duration])

      val cfg = ExampleCfg(ConfigFactory.parseString("num = 1"))
      assert( cfg.num == 1 )
      assert( cfg.str == "foo" )
      assert( cfg.dur.isEmpty )
    }

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
      @Cfg
      case class CompanionCfg(strs : List[String]) {
        val h: Int = $
      }
      object CompanionCfg {
        val xyz: String = "whatever"
      }

      val conf = ConfigFactory.parseString(
        """
          |strs = [ Calvin, Hobbes ]
          |h = 9
        """.stripMargin)
      val cfg = CompanionCfg(conf)
      cfg.strs  ==> List("Calvin", "Hobbes")
      cfg.h    ==> 9
    }

    "Companion2Cfg" - {
      @Cfg
      case class Companion2Cfg(str : String) {
        val hs: List[Int] = $
      }
      object Companion2Cfg {
        def apply(): Unit = ()
      }

      val conf = ConfigFactory.parseString(
        """
          |str = Calvin
          |hs = [9, 3]
        """.stripMargin)
      val cfg = Companion2Cfg(conf)
      cfg.str ==> "Calvin"
      cfg.hs  ==> List(9, 3)
    }

    "WithDefaultCfg" - {
      @Cfg
      case class WithDefaultCfg(
                                 int : Int    = 21,
                                 str : String = "someStr"
                               )

      val conf = ConfigFactory.parseString("")
      val cfg = WithDefaultCfg(conf)
      cfg.int  ==> 21
      cfg.str  ==> "someStr"
    }

    "WithOptCfg" - {
      @Cfg
      case class WithOptCfg(
                             int    : Option[Int],
                             str    : Option[String],
                             simple : Option[SimpleCfg]
                           ) {

        val simple2: Option[SimpleCfg] = $
      }

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

      @Cfg
      case class WithDurationCfg(
                                  dur    : Duration,
                                  durOpt : Option[Duration],
                                  durs   : List[Duration]
                                ) {

        val dur1    : Duration = 3.seconds
        val durOpt1 : Option[Duration] = $
        val durs1   : List[Duration] = $
      }

      val conf = ConfigFactory.parseString(
        """
          dur = 6h
          durs = [ 3600s, 1d ]
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

    "WithSizeInBytesCfg" - {
      @Cfg
      case class WithBytesCfg(
                               size    : SizeInBytes,
                               sizeOpt : Option[SizeInBytes],
                               sizes   : List[SizeInBytes]
                             ) {

        val size1    : SizeInBytes = $
        val sizeOpt1 : Option[SizeInBytes] = $
        val sizes1   : List[SizeInBytes] = $
      }

      val conf = ConfigFactory.parseString(
        """
          size = 2048K
          sizes = [ 1000, "64G", "16kB" ]
          size1 = 64G
          sizeOpt1 = 1kB
          sizes1 = [ 512 ]
        """.stripMargin)
      val cfg = WithBytesCfg(conf)
      cfg.size     ==> 2048*1024
      cfg.sizeOpt  ==> None
      cfg.sizes    ==> List(1000, 64*1024*1024*1024L, 16*1000)
      cfg.size1    ==> 64*1024*1024*1024L
      cfg.sizeOpt1 ==> Some(1000)
      cfg.sizes1   ==> List(512)
    }

    "WithVarsDefsCfg" - {
      @Cfg
      case class WithOtherStuffCfg(value: Long) {
        object bar {
          val name : String  = $
          val cool : Boolean = true
          var abc  : Double  = 0.2

          def isCool: Boolean = cool

          override def toString: String = s"<'$name', $cool>"
        }
        override def toString: String = s"<$value, $bar>"
      }

      val cfg = WithOtherStuffCfg(ConfigFactory.parseString(
        """
           value = 1
           bar {
             name = "Name"
             cool = false
           }
           cc = { x = 99 }
        """.stripMargin))

      cfg.value ==> 1
      cfg.bar.name ==> "Name"
      cfg.bar.isCool ==> false
      cfg.bar.abc ==> 0.2
      cfg.toString ==> "<1, <'Name', false>>"
    }

    "WithInnerCfgsCfg" - {
      @Cfg
      case class WithInnerCfgsCfg(value: Long = 11) {
        object bar {
          @Cfg
          case class BB(b: Int)

          val bb: BB = $
        }

        @Cfg
        case class CC(c: Int)

        val cc: CC = CC(33)
      }

      val cfg = WithInnerCfgsCfg(ConfigFactory.parseString(
        """
           bar {
             bb = { b = 22 }
           }
        """.stripMargin))

      cfg.value ==> 11
      cfg.bar.bb ==> cfg.bar.BB(22)
      cfg.cc ==> cfg.CC(33)
    }
  }
}
