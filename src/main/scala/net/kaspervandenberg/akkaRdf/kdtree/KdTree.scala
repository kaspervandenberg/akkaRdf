// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.kdtree

import scala.collection.immutable.List;
import scala.math.Ordering;

/**
 * A 𝑘-dimensional tree: store <var>A</var>-nodes organised in
 * 𝑘-dimensions.
 *
 * The tree is implemented as a binary tree that alternates the dimensions 
 * [[scala.math.Ordering]]s.   See
 * [[http://en.wikipedia.org/wiki/K-d_tree k-d-tree at Wikipedia]] for a more 
 * detailed description.
 *
 * <p><a href="http://commons.wikimedia.org/wiki/File:3dtree.png#mediaviewer/File:3dtree.png"><img alt="3dtree.png" width=25% src="http://upload.wikimedia.org/wikipedia/commons/b/b6/3dtree.png"></a><br>"<a href="http://commons.wikimedia.org/wiki/File:3dtree.png#mediaviewer/File:3dtree.png">3dtree</a>". Licensed under <a title="GNU General Public License" href="http://www.gnu.org/licenses/gpl.html">GPL</a> via <a href="//commons.wikimedia.org/wiki/">Wikimedia Commons</a>.</p>
 *
 *
 * @example We can organise a set of points in 3d-space in a 
 * [[net.kaspervandenberg.akkaRdf.kdtree.KDTree]].
 * Import the namespace: {{{
 * scala> import net.kaspervandenberg.akkaRdf.kdtree._
 * }}}
 * If you like to view the tree's in Graphviz Dot format, then import: {{{
 * scala> import net.kaspervandenberg.akkaRdf.kdtree.DotOutput._
 * }}}
 * Simple class for holding the points: {{{
 * scala> case class Point3D(name: String, x: Int, y: Int, z: Int)
 * }}}
 * Define a [[net.kaspervandenberg.akkaRdf.kdtree.DimensionList]] containing 
 * all dimensions of `Point3D`. There are two alternatives, either directly use 
 * [[scala.math.Ordering]]: {{{
 * scala> val xdim = Ordering.by[Point3D, Int](p => p.z)
 * scala> val ydim = Ordering.by[Point3D, Int](p => p.y)
 * scala> val zdim = Ordering.by[Point3D, Int](p => p.x)
 * scala> val pointDimensions = DimensionList(xdim :: ydim :: zdim :: Nil)
 * }}}
 * or use [[net.kaspervandenberg.akkaRdf.kdtree.DotOutput.NamedDimension]] 
 * which can output Graphviz Dot edges: {{{
 * scala> val xdim = new NamedDimension[Point3D, Int]("\uD835\uDC65", p => p.x) 
 * scala> val ydim = new NamedDimension[Point3D, Int]("\uD835\uDC66", p => p.y)
 * scala> val zdim = new NamedDimension[Point3D, Int]("\uD835\uDC67", p => p.z)
 * scala> val pointDimensions = DimensionList(xdim :: ydim :: zdim :: Nil)
 * }}}
 * Use `pointDimensions` to create an 
 * [[net.kaspervandenberg.akkaRdf.kdtree.EmptyKDTree]]: {{{
 * scala> val emptyTree = pointDimensions.createEmptyTree
 * }}}
 * Add some random points to the tree: {{{
 * scala> val rng = new scala.util.Random()
 * scala> def createRandomPoint(name: String): Point3D =
 *      | Point3D(name, rng.nextInt(10), rng.nextInt(10), rng.nextInt(10))
 * scala> val t2 = emptyTree.
 *      | add( createRandomPoint("p1") ).
 *      | add( createRandomPoint("p2") ).
 *      | add( createRandomPoint("p3") ).
 *      | add( createRandomPoint("p4") ).
 *      | add( createRandomPoint("p5") ).
 *      | add( createRandomPoint("p6") ).
 *      | add( createRandomPoint("p7") )
 * }}}
 * If you have dot and display installed (from respectively Graphviz and 
 * ImageMagic), you can display a picture of the tree: {{{
 * scala> import scala.sys.process._
 * scala> import net.kaspervandenberg.akkaRdf.kdtree.DotOutput._
 * scala> val output = DotOutput.toDotGraph("G", t2)
 * scala> val is = new java.io.ByteArrayInputStream(output.getBytes)
 * scala> val proc = "dot -T png" #| "display" #< is
 * scala> proc.!
 * }}}
 */
abstract class KDTree[T, N <: DimensionList.Dimensionality[T]](
		dimensions: DimensionList[T, N])
{
	def add(newValue: T): KDTree[T, N];

	def dimension(): Ordering[T] = dimensions.currentDimension;
}


case class EmptyKDTree[T, N <: DimensionList.Dimensionality[T]]
		(dimensions: DimensionList[T, N])
		extends KDTree[T, N](dimensions)
{
	override def add(newValue: T): KDTreeNode[T, N] =
		new KDTreeNode[T, N](dimensions, newValue,
			new EmptyKDTree[T, N](dimensions.rotate),
			new EmptyKDTree[T, N](dimensions.rotate))
}



case class KDTreeNode[T, N <: DimensionList.Dimensionality[T]](
		dimensions: DimensionList[T, N],
		val value: T,
		val left: KDTree[T, N],
		val right: KDTree[T, N])
	extends KDTree[T, N](dimensions)
{
	def add(newValue: T): KDTree[T, N] =
		dimensions.tryCompare(newValue, value) match {
			case Some(r) if r <=0 =>
				new KDTreeNode(dimensions, value, left.add(newValue), right)
			case _ =>
				new KDTreeNode(dimensions, value, left, right.add(newValue))
		}
}

class DimensionList[T, S <: DimensionList.Dimensionality[T]]
		(d: List[Ordering[T]])(implicit ev: S)
		extends Ordering[T]
{
	def currentDimension(): Ordering[T] = d.head

	def rotate(): DimensionList[T, S] =
		new DimensionList(d.tail :+ d.head)(ev)

	def createEmptyTree(): EmptyKDTree[T, S] = new EmptyKDTree[T, S](this)

	override def compare(x: T, y: T): Int = d.head.compare(x, y)
}

object DimensionList
{
	def apply[T](d: List[Ordering[T]]) =
		new DimensionList(d)(getDimensionality(d))

	trait Dimensionality[T]
	class UnitDimensionality[T] extends Dimensionality[T]
	class RecursiveDimesionallity[T, S <: Dimensionality[T]]
			(implicit ev: S)
			extends Dimensionality[T]

	def getDimensionality[T](d: List[Ordering[T]]):
			Dimensionality[T] = d match {
		case h :: tail =>
			new RecursiveDimesionallity()(getDimensionality(tail))
		case Nil =>
			new UnitDimensionality[T]
	}
}


object DotOutput {

trait DotOutputing {
	def toDotString() : String;
	def getDotNode(): String;
}

case class NamedDimension[T, S]
		(val name: String, val f: T => S)
		(implicit ord: Ordering[S])
	extends Ordering[T]
{
	override def compare(x: T, y: T) = ord.compare(f(x), f(y))
	def edgeLabel(relation: String, v: T): String =
		s"$relation ${f(v)} ($name)"
}
		

implicit class EmptyKDTreeDotOutputting
		[T, N <: DimensionList.Dimensionality[T]]
		(delegate: EmptyKDTree[T, N])
		extends DotOutputing {
	override def getDotNode(): String = s"n_${System.identityHashCode(this)}"
	def toDotString(): String = s"""${this.getDotNode} [label="«empty»"];"""
}

def toDotGraph[T, N <: DimensionList.Dimensionality[T]]
		(graphName: String, tree: KDTree[T, N]): String =
	s"digraph $graphName {\n" +
	toDotOutputting(tree).toDotString +
	s"}\n"

def toDotOutputting[T, N <: DimensionList.Dimensionality[T]]
		(tree: KDTree[T, N])
		: DotOutputing =
	tree match {
		case e: EmptyKDTree[T, N] => new EmptyKDTreeDotOutputting(e);
		case n: KDTreeNode[T, N] => new KDTreeNodeDotOutputting(n);
	}

implicit class KDTreeNodeDotOutputting[T, N <: DimensionList.Dimensionality[T]]
		(delegate: KDTreeNode[T, N])
		extends DotOutputing
{
	override def getDotNode(): String =
		s"n_${System.identityHashCode(delegate)}"

	def toDotString(): String =
		s"""\t${toDotOutputting(delegate.left).toDotString}\n""" +
		s"""\t${toDotOutputting(delegate.right).toDotString}\n""" +
		s"""\t${this.getDotNode} [label="${delegate.value}"];\n""" +
		leftEdge() +
		rightEdge()

	def leftEdge(): String = edge(toDotOutputting(delegate.left), "≤")

	def rightEdge(): String = edge(toDotOutputting(delegate.right), ">, ≠")


	def edge(subtree: DotOutputing, relation: String): String = 
		s"""\t${this.getDotNode} -> ${subtree.getDotNode} """ +
		s"""[label="${edgeLabel(relation)}"];\n"""

	def edgeLabel(relation: String): String = delegate.dimension match {
		case n: NamedDimension[_, _] =>
			n.edgeLabel(relation, delegate.value)
		case _ => relation
	}
}

}
