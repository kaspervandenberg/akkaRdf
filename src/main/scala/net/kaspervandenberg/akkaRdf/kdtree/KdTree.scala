// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.kdtree

import scala.collection.immutable.List;
import scala.math.PartialOrdering;

/**
 * Organise `T`-objects in 𝑘-dimensions. Base class for
 *  - [[net.kaspervandenberg.akkaRdf.kdtree.EmptyKDTree]]; and
 *  - [[net.kaspervandenberg.akkaRdf.kdtree.KDTreeNode]].
 *
 * @tparam	T	type of elements this `KDTree` contains
 * @tparam	N the dimensions that this `KDTree` spans; a `KDTree`that organises
 *		T`-objects into 2 dimensions is of a different type than one that 
 *		organises `T`-objects into 3 dimensions.
 *
 * @constructor	''(abstract)'' initialises the dimensions of this `KDTree`.
 *	Create `KDTree`s via [[net.kaspervandenberg.akkaRdf.kdtree.DimensionList]].
 * @param	dimensions	List of [[scala.math.PartialOrdering]]s that this
 *		`KDTree` uses to organise the `T`-objects that it contains.
 */
sealed abstract class KDTree[T, N <: DimensionList.Dimensionality[T]](
		dimensions: DimensionList[T, N])
		extends Traversable[T]
{
	/**
	 * Returns a `KDTree` that contains `newValue` and all `T`-objects that `this 
	 * KDTree` contains.  Inserts `newValue` at the correct location.  `This 
	 * KDTree` is not modified, instead a new tree is returned.  All unmodified 
	 * nodes from `this KDTree` are shared with the returned `KDTree`.
	 *
	 * @param newValue	element to add to `this KDTree`
	 * @return a [[net.kaspervandenberg.akkaRdf.kdtree.KDTree]] with `newValue`
	 */
	def add(newValue: T): KDTree[T, N];

	/**
	 * Returns the dimension ([[scala.math.PartialOrdering]]) that this `KDTree` uses to 
	 * separate `T`-objects firstly.
	 *
	 * @return	the top level [[scala.math.PartialOrdering]]
	 */
	def dimension(): PartialOrdering[T] = dimensions.currentDimension;
}


/**
 * Placeholder `KDTree` without any value of subnodes.  Use the `add()`-method 
 * to insert values into this tree.  Recommended: use 
 * [[net.kaspervandenberg.akkaRdf.kdtree.DimensionList]]::`createEmptyTree()` 
 * to construct `EmptyKDTree`s.
 *
 * @constructor	Constructs an empty `KDTree` having `dimensions`.
 * @param	dimensions	List of [[scala.math.PartialOrdering]]s that this
 * 		`KDTree` uses to organise the `T`-objects it contains.
 */
case class EmptyKDTree[T, N <: DimensionList.Dimensionality[T]]
		(dimensions: DimensionList[T, N])
		extends KDTree[T, N](dimensions)
{
	/**
	 * Returns a `KDTree`-that contains `newValue`.  The returned `KDTree` has 
	 * the same `dimensions` as this `EmptyKDTree`.
	 */
	override def add(newValue: T): KDTreeNode[T, N] =
		new KDTreeNode[T, N](dimensions, newValue,
			new EmptyKDTree[T, N](dimensions.rotate),
			new EmptyKDTree[T, N](dimensions.rotate))

	/**
	 * No-opp, function `f` is not called.
	 */
	override def foreach[U](f: T ⇒ U) = ()
}



/**
 * Node of a `KDTree` containing a `T`-object, `value`, and a left- and right- 
 * sub-`KDTree`.  `T`-objects are organised according to 
 * `dimensions.currentDimension` (see 
 * [[net.kaspervandenberg.akkaRdf.kdtree.DimensionList]]): the `left` subtree 
 * contains `T`-objects ≤ `value`, the right subtree contains `T`-objects 
 * >`value` and not comparable with `value`.
 */
case class KDTreeNode[T, N <: DimensionList.Dimensionality[T]](
		dimensions: DimensionList[T, N],
		val value: T,
		val left: KDTree[T, N],
		val right: KDTree[T, N])
	extends KDTree[T, N](dimensions)
{
	override def add(newValue: T): KDTreeNode[T, N] =
		if(dimensions.lteq(newValue, value))
			new KDTreeNode(dimensions, value, left.add(newValue), right)
		else
			new KDTreeNode(dimensions, value, left, right.add(newValue))

	/**
	 * Call `f` for each `T`-object in this `KDTreeNode`.  `foreach` visits the 
	 * `T`-objects depth-first infix.
	 */
	override def foreach[U](f: T ⇒ U) =
	{
		left foreach f;
		f(value);
		right foreach f;
	}
}

class DimensionList[T, S <: DimensionList.Dimensionality[T]]
		(d: List[PartialOrdering[T]])(implicit ev: S)
		extends PartialOrdering[T]
{
	def currentDimension(): PartialOrdering[T] = d.head

	def rotate(): DimensionList[T, S] =
		new DimensionList(d.tail :+ d.head)(ev)

	def createEmptyTree(): EmptyKDTree[T, S] = new EmptyKDTree[T, S](this)

	override def lteq(x: T, y: T): Boolean = d.head.lteq(x, y)
	override def tryCompare(x: T, y: T): Option[Int] = d.head.tryCompare(x, y)
}

object DimensionList
{
	def apply[T](d: List[PartialOrdering[T]]) =
		new DimensionList(d)(getDimensionality(d))

	trait Dimensionality[T]
	class UnitDimensionality[T] extends Dimensionality[T]
	class RecursiveDimesionallity[T, S <: Dimensionality[T]]
			(implicit ev: S)
			extends Dimensionality[T]

	def getDimensionality[T](d: List[PartialOrdering[T]]):
			Dimensionality[T] = d match {
		case h :: tail =>
			new RecursiveDimesionallity()(getDimensionality(tail))
		case Nil =>
			new UnitDimensionality[T]
	}
}

/* vim:set fo=cqtwa2 textwidth=80 shiftwidth=2 tabstop=2 :*/

