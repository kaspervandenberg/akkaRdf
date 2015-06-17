package net.kaspervandenberg.akkaRdf.kdtree

import scala.collection.mutable.Builder
import scala.collection.generic.Growable
import scala.collection.Seq
import scala.collection.immutable.List
import scala.collection.parallel.mutable.ParArray
import scala.collection.parallel.immutable.ParVector
import scala.collection.GenSeqLike
import scala.collection.parallel.ParSeq
import scala.math.PartialOrdering

class KDTreeDirectBuilder[T, N <: DimensionList.Dimensionality[T]]
	(d: DimensionList[T, N])
	extends Builder[T, KDTree[T, N]]
{
	var root: KDTree[T,N] = d.createEmptyTree

	def +=(elem: T): KDTreeDirectBuilder.this.type = {
		root = root.add(elem);
		return this;
	}

	def clear(): Unit = { root = d.createEmptyTree; }

	def result(): KDTree[T, N] = root
}


class KDTreeBalancingBuilder[T, N <: DimensionList.Dimensionality[T]]
	(d: DimensionList[T, N])
	extends Builder[T, KDTree[T, N]]
{
	var elements: Seq[T] = Seq.empty

	def +=(elem: T): KDTreeBalancingBuilder.this.type = {
		elements = elements :+ elem;
		return this;
	}

	def clear(): Unit = { elements = Seq.empty; }

	def result(): KDTree[T, N] = {
		throw new NotImplementedError()
	}

	private def buildSubTree(
		 subTreeItems: Traversable[T], dimensions: DimensionList[T, N]) :
		KDTree[T, N] = subTreeItems match {
			case (Nil) => return new EmptyKDTree[T,N](dimensions);
/*			case (value :: Nil) => return new KDTreeNode[T, N](
				dimensions, value,
				new EmptyKDTree[T,N](dimensions),
				new EmptyKDTree[T, N](dimensions));
*/
			case _ => {
				val partition = Partition(subTreeItems, dimensions);
				partition.median match {
					case Some(value) => return new KDTreeNode[T,N](
						dimensions,
						value,
						buildSubTree(partition.lowPart, dimensions.rotate()),
						buildSubTree(partition.highPart, dimensions.rotate()));
					case None => return new EmptyKDTree[T,N](dimensions);
				}
			}
		}
}


/* vim:set fo=cqtwa2 textwidth=80 shiftwidth=2 tabstop=2 :*/

