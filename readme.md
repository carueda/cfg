[![Build Status](https://travis-ci.org/carueda/cfg.svg?branch=master)](https://travis-ci.org/carueda/cfg)
[![Coverage Status](https://coveralls.io/repos/github/carueda/cfg/badge.svg?branch=master)](https://coveralls.io/github/carueda/cfg?branch=master)

# `Cfg` 

Implemented using [Scalameta](http://scalameta.org/), 
`Cfg` is an annotation that allows to specify the _schema_ of your 
application or library configuration using case classes and inner vals and objects.
As a [Typesafe Config](https://github.com/typesafehub/config) wrapper,
the `Cfg` annotation generates an `apply(c: com.typesafe.config.Config)` 
method in the companion object to instantiate your case class from a given
Typesafe Config object.
With `Cfg` you enjoy type safety all the way from configuration spec to 
configuration access along with all the typical associated features
of your IDE related with code completion, navigation, and refactoring. 
 
`Cfg` supports all types handled by Typesafe Config, which, in Scala, are
represented with the standard types  
`String`, `Int`, `Long`, `Double`, `Boolean`, `scala.concurrent.duration.Duration`, 
`SizeInBytes` (alias for `Long`), 
along with `List[T]` and `Option[T]` 
(where `T` is, recursively, any supported type). 


## Usage

In your `build.sbt`:

```scala
libraryDependencies += "com.github.carueda" %% "cfg" % "0.0.7" % "provided"

addCompilerPlugin(
  ("org.scalameta" % "paradise" % "3.0.0-M8").cross(CrossVersion.full)
)
```

Use the `Cfg` annotation to specify the schema of your configuration:

```scala
import carueda.cfg._

@Cfg
case class SimpleCfg(int: Int, str: String)
```

Use any usual Typesafe Config mechanism to load a concrete configuration,
for example:

```scala
val conf = com.typesafe.config.ConfigFactory.parseString(
  """
  int = 1
  str = "Hobbes"
  """)
```

Then, just create the wrapper and enjoy the benefits:

```scala
val cfg = SimpleCfg(conf)

cfg.int  ==> 1
cfg.str  ==> "Hobbes"
```

### Default values

Just initialize the entries in your class as you would normally do:

```scala
@Cfg
case class WithDefaultCfg(
                      int    : Int       = 21,
                      str    : String    = "someStr",
                      simple : SimpleCfg = SimpleCfg(1, "A")
                    )

val conf = ConfigFactory.parseString("")
val cfg = WithDefaultCfg(conf)
cfg.int  ==> 21
cfg.str  ==> "someStr"
cfg.simple.int  ==> 1
cfg.simple.str  ==> "A"
```

### Optional entries

For completely optional entries (i.e., without any default value), use `Option[T]`:

```scala
@Cfg
case class WithOptCfg(
                      int    : Option[Int],
                      str    : Option[String],
                      simple : Option[SimpleCfg]
                    )

val conf = ConfigFactory.parseString(
  """
  int =  8
  simple {
    int = 1
    str = str
  }
  """)
  
val cfg = WithOptCfg(conf)
cfg.int     ==> Some(8)
cfg.str     ==> None
cfg.simple  ==> Some(SimpleCfg(1, "str"))
```

### Class members

You can also include a body with members in the case class:

```scala
@Cfg
case class BarCfg(
                   reqInt : Int,
                   reqStr : String
                 ) {

  object foo {
    val bool : Boolean = $

    object baz {
      val who   : String = "Calvin"
      val other : Int    = $
    }
  }
  val long : Long = $
}
```

This, in particular, allows to directly embed the specification of inner objects
without necessarily having to introduce a class for them.
The `$` is a placeholder that gets replaced with appropriate extraction logic by
the `Cfg` annotation.

Using `BarCfg`:

```scala
val bar = BarCfg(ConfigFactory.parseString(
  """
  reqInt = 9393
  reqStr = "reqStr"
  long = 1212100
  foo {
    bool = false
    baz {
      long = 1212100
    }
  }
  """))

bar.reqInt        ==> 9393
bar.reqStr        ==> "reqStr"
bar.foo.bool      ==> false
bar.foo.baz.long  ==> 1212100
bar.foo.baz.who   ==> "Calvin"
bar.long          ==> 1212100
```

### Lists

As you would expect, just use `List[T]`:

```scala
@Cfg
case class WithListCfg(
                      ints     : List[Int],
                      strs     : List[String],
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
  """)

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
```

### Duration

```scala
import scala.concurrent.duration._

@Cfg
case class WithDurationCfg(
                      dur    : Duration,
                      durOpt : Option[Duration],
                      durs   : List[Duration]
                    )

val conf = ConfigFactory.parseString(
  """
  dur = 6h
  durs = [ 3600s, 1d ]
  """)
val cfg = WithDurationCfg(conf)
cfg.dur.toHours  ==> 6
cfg.durOpt  ==> None
cfg.durs.map(_.toHours)  ==> List(1, 24)
```

### Size-in-bytes

This is represented with a long type in the Typesafe Config library.
In `Cfg`, to tell this type apart from a regular `Long`, 
use the alias `SizeInBytes`:

```scala
@Cfg
case class WithBytesCfg(
                      size    : SizeInBytes,
                      sizeOpt : Option[SizeInBytes],
                      sizes   : List[SizeInBytes]
                    )

val conf = ConfigFactory.parseString(
  """
  size = 2048K
  sizes = [ 1000, "64G", "16kB" ]
  """)
val cfg = WithBytesCfg(conf)
cfg.size     ==> 2048*1024
cfg.sizeOpt  ==> None
cfg.sizes    ==> List(1000, 64*1024*1024*1024L, 16*1000)
```


## tests

https://github.com/carueda/cfg/tree/master/src/test/scala
