package psp
package tests

import psp.std._

/** TODO - actually generate the code via an sbt generator.
 */
object Generator {
  def tq    = "\"\"\""
  def lhses = sciList("\"abc\"", "scala.collection.Seq(1, 2, 3)", "scala.collection.mutable.Seq('a', 'b', 'c')", "Array(true, false, true)")
  def ops   = sciList("index", "indexOf", "contains", "hasElem")
  def rhses = sciList("59d", "(1: Int)", "('b': Char)", "100000")

  def exprs(ops: String*)     = for (lhs <- lhses ; op <- ops; rhs <- rhses) yield s"$lhs $op $rhs"
  def genScalaLibrary: String = exprs("indexOf", "contains").mkString("final val scalaLibraryCode = " + tq + "\n    ", "\n    ", "\n  " + tq)
  def genPsp: String          = exprs("index", "hasElem").mkString("final val pspCode = " + tq + "\n    ", "\n    ", "\n  " + tq)
  def gen: Unit = {
    println(genScalaLibrary)
    println(genPsp)
  }
}

object Generated {
  final val pspShowCode = """
    /* no */ class Bippy ; val x = new Bippy ; show"$x"
    /* ok */ class Bippy ; val x = new Bippy ; implicit val s = Show[Bippy](_ => "yo") ; show"$x"
  """

  final val scalaLibraryCode = """
    "abc" indexOf 59d
    "abc" indexOf (1: Int)
    "abc" indexOf ('b': Char)
    "abc" indexOf 100000
    "abc" contains 59d
    "abc" contains (1: Int)
    "abc" contains ('b': Char)
    "abc" contains 100000
    scala.collection.Seq(1, 2, 3) indexOf 59d
    scala.collection.Seq(1, 2, 3) indexOf (1: Int)
    scala.collection.Seq(1, 2, 3) indexOf ('b': Char)
    scala.collection.Seq(1, 2, 3) indexOf 100000
    scala.collection.Seq(1, 2, 3) contains 59d
    scala.collection.Seq(1, 2, 3) contains (1: Int)
    scala.collection.Seq(1, 2, 3) contains ('b': Char)
    scala.collection.Seq(1, 2, 3) contains 100000
    scala.collection.mutable.Seq('a', 'b', 'c') indexOf 59d
    scala.collection.mutable.Seq('a', 'b', 'c') indexOf (1: Int)
    scala.collection.mutable.Seq('a', 'b', 'c') indexOf ('b': Char)
    scala.collection.mutable.Seq('a', 'b', 'c') indexOf 100000
    scala.collection.mutable.Seq('a', 'b', 'c') contains 59d
    scala.collection.mutable.Seq('a', 'b', 'c') contains (1: Int)
    scala.collection.mutable.Seq('a', 'b', 'c') contains ('b': Char)
    scala.collection.mutable.Seq('a', 'b', 'c') contains 100000
    Array(true, false, true) indexOf 59d
    Array(true, false, true) indexOf (1: Int)
    Array(true, false, true) indexOf ('b': Char)
    Array(true, false, true) indexOf 100000
    Array(true, false, true) contains 59d
    Array(true, false, true) contains (1: Int)
    Array(true, false, true) contains ('b': Char)
    Array(true, false, true) contains 100000
  """

  final val pspCode = """
    "abc" indexByEquals 59d
    "abc" indexByEquals (1: Int)
    "abc" indexByEquals ('b': Char)
    "abc" indexByEquals 100000
    "abc" containsByEquals 59d
    "abc" containsByEquals (1: Int)
    "abc" containsByEquals ('b': Char)
    "abc" containsByEquals 100000
    scala.collection.Seq(1, 2, 3) indexByEquals 59d
    scala.collection.Seq(1, 2, 3) indexByEquals (1: Int)
    scala.collection.Seq(1, 2, 3) indexByEquals ('b': Char)
    scala.collection.Seq(1, 2, 3) indexByEquals 100000
    scala.collection.Seq(1, 2, 3) containsByEquals 59d
    scala.collection.Seq(1, 2, 3) containsByEquals (1: Int)
    scala.collection.Seq(1, 2, 3) containsByEquals ('b': Char)
    scala.collection.Seq(1, 2, 3) containsByEquals 100000
    scala.collection.mutable.Seq('a', 'b', 'c') indexByEquals 59d
    scala.collection.mutable.Seq('a', 'b', 'c') indexByEquals (1: Int)
    scala.collection.mutable.Seq('a', 'b', 'c') indexByEquals ('b': Char)
    scala.collection.mutable.Seq('a', 'b', 'c') indexByEquals 100000
    scala.collection.mutable.Seq('a', 'b', 'c') containsByEquals 59d
    scala.collection.mutable.Seq('a', 'b', 'c') containsByEquals (1: Int)
    scala.collection.mutable.Seq('a', 'b', 'c') containsByEquals ('b': Char)
    scala.collection.mutable.Seq('a', 'b', 'c') containsByEquals 100000
    Array(true, false, true) indexByEquals 59d
    Array(true, false, true) indexByEquals (1: Int)
    Array(true, false, true) indexByEquals ('b': Char)
    Array(true, false, true) indexByEquals 100000
    Array(true, false, true) containsByEquals 59d
    Array(true, false, true) containsByEquals (1: Int)
    Array(true, false, true) containsByEquals ('b': Char)
    Array(true, false, true) containsByEquals 100000
  """
}
