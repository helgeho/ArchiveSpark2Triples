/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Helge Holzmann
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.l3s.archivespark2triples

class TripleDoc(val key: String, val subject: String, val typeName: String, val triples: Seq[(String, Seq[String])], val children: Seq[(String, Seq[TripleDoc])]) {
  val TabSize = 4
  val PadLength = 20
  val TypePredicate = "rdf:type"

  private def nestedTripleStrings: Seq[Seq[String]] = {
    val tab = " " * TabSize
    Seq(Seq(s"$subject $TypePredicate $typeName")) ++ (triples ++ children.map{case (predicate, docs) => (predicate, docs.map(_.subject))}).flatMap { case (predicate, objects) =>
      objects.headOption.map { head =>
        val paddedPredicate = predicate.padTo(PadLength, " ").mkString
        val indent = tab + (" " * paddedPredicate.length) + " "
        Seq(s"$tab$paddedPredicate $head") ++ objects.drop(1).map(indent + _)
      }
    }
  }

  private def intendedString(indent: Int): String = {
    val str = nestedTripleStrings.map{strs =>
      strs.map((" " * (indent * TabSize)) + _).mkString(",\n")
    }.mkString(" ;\n") + " .\n"
    "\n" + (Seq(str) ++ children.flatMap{case (predicate, docs) => docs}.map(_.intendedString(indent + 1))).mkString
  }

  override def toString = intendedString(0).replace("\\", "\\\\")

  def appendTriples(predicate: String, values: String*): TripleDoc = new TripleDoc(key, subject, typeName, this.triples :+ (predicate -> values), children)
  def appendChildren(predicate: String, children: Seq[TripleDoc]): TripleDoc = new TripleDoc(key, subject, typeName, triples, this.children :+ (predicate -> children))
}

object TripleDoc {
  def apply(key: String, subject: String, typeName: String, triples: Seq[(String, Seq[String])], children: Seq[(String, Seq[TripleDoc])]): TripleDoc = new TripleDoc(key, subject, typeName, triples, children)
  def apply(key: String, subject: String, typeName: String, triples: Seq[(String, Seq[String])]): TripleDoc = new TripleDoc(key, subject, typeName, triples, Seq.empty)
  def apply(subject: String, typeName: String, triples: Seq[(String, Seq[String])]): TripleDoc = new TripleDoc(subject, subject, typeName, triples, Seq.empty)
}