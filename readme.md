[![Build Status](https://travis-ci.org/carueda/cfg.svg?branch=master)](https://travis-ci.org/carueda/cfg)

# `@Cfg` 

`@Cfg` is a [Typesafe Config](https://github.com/typesafehub/config) wrapper that allows to: 
- specify the configuration of your application or library using case classes and inner vals and objects;
- access concrete configurations in a type-safe manner while enjoying the code 
  completion, navigation, and refactoring capabilities of your IDE. 

`@Cfg` is implemented using a [Scalameta](http://scalameta.org/). 

WIP

## Usage

Use the `@Cfg` annotation to specify the schema of your configuration:

```scala
@Cfg
case class SimpleCfg(
                   int : Int,
                   str : String
                 )
```

> The annotation generates a companion object with `apply` method expecting
> a `com.typesafe.config.Config` instance:
> 
> ```scala
> object SimpleCfg {
>   def apply(c: com.typesafe.config.Config): SimpleCfg = {
>     SimpleCfg(c.getInt("int"), c.getString("str"))
>   }
> }
> ```


Use any usual [Typesafe Config](https://github.com/typesafehub/config) 
mechanism to load a concrete configuration, for example:

```scala
val conf = ConfigFactory.parseString(
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

You can also include members in the case class:

```scala
@Cfg
case class BarCfg(
                   reqInt : Int,
                   reqStr : String
                 ) {
  val long : Long = $

  object foo {
    val bool : Boolean = $

    object baz {
      val who  : String = "Calvin"
      val other: Int    = $
    }
  }
}
```

which, in particular, allows to directly embed the specification of inner objects
without necessarily having to introduce a class for them.

The `$` is a placeholder that gets replaced with appropriate extraction logic by
the macro. A concrete initialization value (e.g., `"Calvin"` for the `who` entry above)
indicates that the entry is optional, with the given value as the default.

Using `BarCfg`:

```scala
val bar = BarCfg(ConfigFactory.parseString(
  """
  reqInt = 9393
  reqStr = "reqStr"
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
```

Of course, you can refer to other `@Cfg`-annotated classes: 

```scala
@Cfg
case class WithOtherCfg(
                    reqInt  : Int,
                    reqStr  : String,
                    bar     : BarCfg
                  ) {
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
    foo {
      bool = false
      baz {
        long = 1212100
      }
    }
  }
"""))

cfg.reqInt        ==> 2130
cfg.reqStr        ==> "reqStr"
cfg.foo.bool      ==> true
cfg.other         ==> 1010

val bar = cfg.bar
bar.reqInt        ==> 9393
bar.reqStr        ==> "reqStr"
bar.foo.bool      ==> false
bar.foo.baz.long  ==> 1212100
bar.foo.baz.who   ==> "Calvin"
```

## TODO

- handle optional entry
- handle list
- Duration
- Size-in-bytes
- ...
