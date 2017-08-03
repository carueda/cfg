package carueda.cfg

import scala.collection.immutable.Seq
import scala.meta._

/**
Before:
  {{{
  @Cfg
  case class MyCcCfg(
                      reqInt  : Int,
                      reqStr  : String,
                      bar     : Bar
                    ) {

    object foo {
      val bool  : Boolean = ???
    }
  }

  @Cfg
  class Bar(c: com.typesafe.config.Config) {
    val long : Long = ???

    object baz {
      val name : String = ???
    }
  }
  }}}

  After:
  {{{
  case class MyCcCfg(reqInt: Int, reqStr: String, bar: Bar) {

    object foo {
      private val $foo = MyCcCfg.$c.getConfig("foo")
      val bool: Boolean = $foo.getBoolean("bool")
    }

  }

  object MyCcCfg {
    private var $c: com.typesafe.config.Config = _

    def apply(c: com.typesafe.config.Config): MyCcCfg = {
      $c = c
      MyCcCfg(c.getInt("reqInt"), c.getString("reqStr"), new Bar(c.getConfig("bar")))
    }
  }

  class Bar(c: com.typesafe.config.Config) {
    val long: Long = c.getLong("long")

    object baz {
      private val $baz = c.getConfig("baz")
      val name: String = $baz.getString("name")
    }

  }
  }}}
  */
class Cfg extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case cls @ Defn.Class(Seq(Mod.Case()), name, _, Ctor.Primary(_, _, paramss), _) ⇒
        CcCfgUtil.handleCaseClass(cls, name.syntax + ".$c")

      case cls @ Defn.Class(_, _, _, Ctor.Primary(_, _, Seq(Seq(param))), _) ⇒
        CfgUtil.handleClass(cls, param.name.syntax)

      case _ ⇒
        println(defn.structure)
        abort("@Cfg must annotate a case class")
    }
  }
}

private object CcCfgUtil {

  def handleCaseClass(cls: Defn.Class, cn: String, level: Int = 0): Term.Block = {
    val Defn.Class(
    _,
    name,
    _,
    ctor,
    template@Template(_, _, _, Some(stats))
    ) = cls

    var templateStats: List[Stat] = List.empty
    stats foreach {
      case obj:Defn.Object ⇒
        templateStats :+= CfgUtil.handleObj(obj, cn, level + 1)

      case v:Defn.Val ⇒
        templateStats ++= CfgUtil.handleVal(v, cn)
    }

    val companion = {
      val decl = q"private var ${Pat.Var.Term(Term.Name("$c"))}: com.typesafe.config.Config = _"
      val applyMethod = createApply(name, ctor.paramss)

      q"""
          object ${Term.Name(name.value)} {
            $decl
            $applyMethod
          }
      """
    }

    Term.Block(Seq(
      cls.copy(templ = template.copy(stats = Some(templateStats))),
      companion))
  }

  def createApply(name: Type.Name, paramss: Seq[Seq[Term.Param]]): Defn.Def = {
    val args = paramss.map(_.map { param =>
      val declType = param.decltpe.get.syntax
      if (isBasic(declType)) {
        Term.Name("c.get" + declType + s"""("${param.name}")""")
      }
      else {
        val arg = Term.Name(s"""c.getConfig("${param.name.syntax}")""")

        val constructor = Ctor.Ref.Name(param.decltpe.get.syntax)

        q"new $constructor($arg)"
      }
    })
    q"""
        def apply(c: com.typesafe.config.Config): $name = {
          ${Term.Name("$c")} = c
          ${Ctor.Ref.Name(name.value)}(...$args)
        }
      """
  }

  def handleObj(obj: Defn.Object, cn: String, level: Int = 0): Stat = {
    val Defn.Object(_, name, template@Template(_, _, _, Some(stats))) = obj
    //println("handleObj:    " + name.structure)

    var templateStats: List[Stat] = List.empty

    val newCn = Pat.Var.Term(Term.Name(("$" * level) + name.syntax))
    val getter = Term.Name(s"""$cn.getConfig("${name.syntax}")""")
    templateStats :+= q"""private val $newCn = $getter"""

    stats foreach {
      case obj:Defn.Object ⇒
        templateStats :+= CfgUtil.handleObj(obj, newCn.syntax, level + 1)

      case v:Defn.Val ⇒
        templateStats ++= CfgUtil.handleVal(v, newCn.syntax)
    }

    obj.copy(templ = template.copy(stats = Some(templateStats)))
  }

  private def isBasic(typ: String): Boolean =
    Set("String", "Int", "Boolean", "Double", "Long"
    ).contains(typ)
}

private object CfgUtil {

  def handleClass(cls: Defn.Class, cn: String, level: Int = 0): Defn.Class = {
    val Defn.Class(_, _, _, _, template@Template(_, _, _, Some(stats))) = cls
    var templateStats: List[Stat] = List.empty
    stats foreach {
      case obj:Defn.Object ⇒
        templateStats :+= CfgUtil.handleObj(obj, cn, level + 1)

      case v:Defn.Val ⇒
        templateStats ++= CfgUtil.handleVal(v, cn)
    }
    cls.copy(templ = template.copy(stats = Some(templateStats)))
  }

  def handleVal(v: Defn.Val, cn: String): List[Stat] = {
    val Defn.Val(_, pats, Some(declTpe), _) = v
    //println("handleVal:    " + pats.structure + "   declTpe=" + declTpe.structure)
    var templateStats: List[Stat] = List.empty
    pats foreach {
      case t@Pat.Var.Term(Term.Name(name)) ⇒
        val getter = Term.Name(cn + ".get" + declTpe.syntax + s"""("$name")""")
        templateStats :+= q"""val $t: $declTpe = $getter"""
    }
    templateStats
  }

  def handleObj(obj: Defn.Object, cn: String, level: Int = 0): Stat = {
    val Defn.Object(_, name, template@Template(_, _, _, Some(stats))) = obj
    //println("handleObj:    " + name.structure)

    var templateStats: List[Stat] = List.empty

    val newCn = Pat.Var.Term(Term.Name(("$" * level) + name.syntax))
    val getter = Term.Name(s"""$cn.getConfig("${name.syntax}")""")
    templateStats :+= q"""private val $newCn = $getter"""

    stats foreach {
      case obj:Defn.Object ⇒
        templateStats :+= CfgUtil.handleObj(obj, newCn.syntax, level + 1)

      case v:Defn.Val ⇒
        templateStats ++= CfgUtil.handleVal(v, newCn.syntax)
    }

    obj.copy(templ = template.copy(stats = Some(templateStats)))
  }
}
