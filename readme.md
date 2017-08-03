[![Build Status](https://travis-ci.org/carueda/cfg.svg?branch=master)](https://travis-ci.org/carueda/cfg)

# `@Cfg` 

`@Cfg` is a [Scalameta](http://scalameta.org/)-based annotation that allows to
specify a configuration schema such that concrete configurations can be loaded 
and used while enjoying full type safety and the code completion and navigation 
capabilities of your IDE.

WIP

## Usage

Use the `@Cfg` annotation to specify the schema of your configuration:

```scala
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
```

Use any usual [Typesafe Config](https://github.com/typesafehub/config) 
mechanism to load a concrete configuration, for example:

```scala 
val config = ConfigFactory.parseString(
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
```

Then, just create the wrapper and enjoy the benefits:

```scala
val bar = new BarCfg(conf)

bar.reqInt        ==> 9393
bar.reqStr        ==> "reqStr"
bar.foo.bool      ==> false
bar.foo.baz.long  ==> 1212100
bar.foo.baz.name  ==> "calvin"
```

`@Cfg` also works on case classes:

```scala
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
```
