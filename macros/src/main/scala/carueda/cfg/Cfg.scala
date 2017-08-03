package carueda.cfg

import scala.collection.immutable.Seq
import scala.meta._

class Cfg extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    // is there a specific api to check whether a mod is a Mod.Case()?
    def isCase(mod: Mod): Boolean = mod.toString() == Mod.Case().toString()

    defn match {
      case cls @ Defn.Class(mods, name, _, _, _) if mods.exists(isCase) ⇒
        CfgUtil.handleCaseClass(cls, name.syntax + ".$c")

      case _ ⇒
        println(defn.structure)
        abort("@Cfg must annotate a case class")
    }
  }
}

private object CfgUtil {

  def handleCaseClass(cls: Defn.Class, cn: String, level: Int = 0): Term.Block = {
    val Defn.Class(
    _,
    name,
    _,
    ctor,
    template@Template(_, _, _, statsOpt)
    ) = cls

    var templateStats: List[Stat] = List.empty

    for (stats ← statsOpt) stats foreach {
      case obj:Defn.Object ⇒
        templateStats :+= CfgUtil.handleObj(obj, cn, level + 1)

      case v:Defn.Val ⇒
        templateStats ++= CfgUtil.handleVal(v, cn)
    }

    val hasBodyElements = templateStats.nonEmpty

    val companion = {
      val applyMethod = createApply(name, ctor.paramss, hasBodyElements)

      if (hasBodyElements) {
        val decl = q"private var ${Pat.Var.Term(Term.Name("$c"))}: com.typesafe.config.Config = _"
        q"""
            object ${Term.Name(name.value)} {
              $decl
              $applyMethod
            }
        """
      }
      else
        q"""
            object ${Term.Name(name.value)} {
              $applyMethod
            }
        """
    }

    Term.Block(Seq(
      cls.copy(templ = template.copy(stats = Some(templateStats))),
      companion))
  }

  def createApply(name: Type.Name, paramss: Seq[Seq[Term.Param]], hasBodyElements: Boolean): Defn.Def = {
    val args = paramss.map(_.map { param ⇒
      val declType = param.decltpe.get
      if (isBasic(declType.syntax)) {
        Term.Name("c.get" + declType + s"""("${param.name}")""")
      }
      else {
        val arg = Term.Name(s"""c.getConfig("${param.name.syntax}")""")

        val constructor = Ctor.Ref.Name(declType.syntax)

        q"$constructor($arg)"
      }
    })

    val ctor = q"${Ctor.Ref.Name(name.value)}(...$args)"
    if (hasBodyElements)
      q"""
          def apply(c: com.typesafe.config.Config): $name = {
            ${Term.Name("$c")} = c
            $ctor
          }
      """
    else
      q"""
          def apply(c: com.typesafe.config.Config): $name = {
            $ctor
          }
      """
  }

  private def isBasic(typ: String): Boolean =
    Set("String", "Int", "Boolean", "Double", "Long"
    ).contains(typ)

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
