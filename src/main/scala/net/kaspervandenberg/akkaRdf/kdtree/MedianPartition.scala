package net.kaspervandenberg.akkaRdf.kdtree

class PartialPartition[T](
		val lowParts: List[List[T]],
		val highParts: List[List[T]],
		val toPartition: List[T],
		val fOrder: PartialOrdering[T]) {
	val nBefore	= lowParts.map(e => e.size).reduce(_+_);
	val nAfter	= highParts.map(e => e.size).reduce(_+_);

	private def baseCaseN0: Partition[T] = {
		require(
			nBefore == 0 && nAfter == 0 && toPartition.size == 0,
			"This case only holds for 𝑛=0");
		return new Partition[T](Nil, Nil, None);
	}
	
	private def baseCaseN1(single: T): Partition[T] = {
		require(
			toPartition.size == 1,
			"This case only holds when there is one element left to partition.");
		return new Partition[T](
			lowParts.flatten, highParts.flatten, Some(single));
	}

	class Pivot {
		val iPivot = PartialPartition.rng.nextInt(toPartition.size);
		val pivot = toPartition.toSeq(iPivot);
		val beforePivot = toPartition.slice(0, iPivot);
		val afterPivot = toPartition.slice(iPivot, toPartition.size);

		val lt = beforePivot.filter(e => fOrder.lt(e, pivot)) ++
			afterPivot.filter(e => fOrder.lt(e, pivot));
		val gt = beforePivot.filter(e => fOrder.gt(e, pivot)) ++
			afterPivot.filter(e => fOrder.gt(e, pivot));
		val equiv = beforePivot.filter(e => fOrder.equiv(e, pivot)) ++
			afterPivot.filter(e => fOrder.equiv(e, pivot));

		val nLt = lowParts.size + lt.size;
		val nGt = highParts.size + gt.size;
		val nEq = equiv.size;
		val acceptableSizeDifference = nEq + 1;

		def findMedian(): Partition[T] = {
			if (nLt + acceptableSizeDifference < nGt) {
				return new PartialPartition[T](
					(pivot :: equiv) :: lt :: lowParts,
					highParts,
					gt,
					fOrder).partition();
			} else if ((nLt + acceptableSizeDifference >= nGt) &&
					(nLt <= nGt +acceptableSizeDifference)) {
				return createPartition();
			} else {
				require(nLt > nGt + acceptableSizeDifference);
				return new PartialPartition[T](
					lowParts,
					(pivot :: equiv) :: gt :: highParts,
					lt,
					fOrder).partition();
			}
		}

		def createPartition(): Partition[T] = {
			require(nEq + 1 >= Math.abs(nLt - nGt));

			var less = lowParts.flatten ++ lt;
			var more = highParts.flatten ++ gt;

			equiv.foreach(el => {
				if (less.size < more.size) {
					less = el :: less;
				} else {
					more = el :: more;
				}
			});

			assert ((less.size == more.size) || (less.size == more.size -1));

			return new Partition[T](less, more, Some(pivot));
		}
	}

	def partition(): Partition[T] = toPartition match {
		case Nil => return baseCaseN0;
		case h :: Nil => return baseCaseN1(h);
		case _ => return new Pivot().findMedian()
	}
	
}

object PartialPartition {
	val rng = util.Random;
}

class Partition[T](
		val lowPart: Traversable[T],
		val highPart: Traversable[T],
		val median: Option[T]) {
}

object Partition {
	def apply[T](items: Traversable[T], fIsLower: PartialOrdering[T]): 
		Partition[T] = {
		throw new NotImplementedError;
	}
}

/* vim:set fo=cqtwa2 textwidth=80 shiftwidth=2 tabstop=2 :*/


