object TryOut {

/*
class Pattern[G, S, P, O] {
};

object Pattern {
	def emptyPattern = new Pattern[
			NotSpecified,
			NotSpecified,
			NotSpecified,
			NotSpecified];
};

implicit class UnspecifiedGraphPattern[S, P, O](
		pat: Pattern[NotSpecified, S, P, O]) {
	def wildcardGraph =
		new Pattern[Wildcard, S, P, O]();

	def fixedGraph(str: String) =
		new Pattern[Fixed, S, P, O]();
};
*/

class PatternPart[T, U](val value: T, val tail: U) { };

class NotSpecified;
class Wildcard;
class Fixed[T](val value: T);

val NotSpecified: NotSpecified = new NotSpecified;
val Wildcard : Wildcard = new Wildcard;

implicit class UnspecifiedPatternPart[U](
		pat: PatternPart[_ <: NotSpecified, U]) {
	def wildcard =
			new PatternPart[Wildcard, U](Wildcard, pat.tail);
	def fixed[T](value: T) =
			new PatternPart[Fixed[T], U](new Fixed(value), pat.tail);
};

import scala.language.higherKinds;

trait HavingPart[T[_]] {
	def prt[T[_]]: T[_];
}

trait HavingRdfObjectPart[O] {
	def rdfObject: PatternPart[O, Unit];
}

trait RecursivelyHavingRdfObjectPart[T, O, U <: HavingRdfObjectPart[O]]
		extends PatternPart[T, U]
		with HavingRdfObjectPart[O] {
	override def rdfObject = tail.rdfObject;
}

class RdfObjectPart[O](override val value: O)
		extends PatternPart[O, Unit](value, ())
		with HavingRdfObjectPart[O] {
	override def rdfObject = this;
}



//trait HavingRdfObjectPart[P, Inner <: PatternPart[_]] extends
//		PatternPart[P, Inner] {
//	def rdfObject = { return Inner#rdfObject; };
//};
/*
trait HavingRdfObjectPart[O] {
	def rdfObject: PatternObjectPart[O];
}


class PatternObjectPart[O](override val part: O) extends
		PatternPart[O, Unit](part, Unit) with
		HavingRdfObjectPart[O] {
	override def rdfObject: PatternObjectPart[O] = {
		return this;
	}
}

trait PartHavingRdfObjectPart[T, O, U <: HavingRdfObjectPart[O]] extends
		PatternPart[T, U] with
		HavingRdfObjectPart[O] {
	override def rdfObject: PatternObjectPart[O] = {
		return tail.rdfObject;
	}
}

trait HavingPredicatePart[P, U] {
	def predicate: PatternPredicatePart[P, U];
}

class PatternPredicatePart[P, U](override val part: P, inner: U) extends
		PatternPart[P, U](part, inner) with
		HavingPredicatePart[P, U]{
	override def predicate: PatternPredicatePart[P, U] = {
		return this;
	}
}

trait PartHavingPredicatePart[T, P, U, V <: HavingPredicatePart[P, U]] extends
		PatternPart[T, V] with
		HavingPredicatePart[P, U] {
	override def predicate: PartHavingPredicatePart[P, U] = {
		return tail.predicate;
	}
}



//class PatternGraphPart[G, S, P, O] extends

type Pattern2[G, S, P, O] =
		PatternPart[G,
		PatternPart[S,
		PatternPart[P,
		PatternPart[O, Unit]]]];


object Pattern2 {
	private type UPP[T]= PatternPart[NotSpecified, T];
	private val empty1 = new UPP[Unit](NotSpecified, Unit);
	private val empty2 = new UPP[UPP[Unit]](NotSpecified, empty1);
	private val empty3 = new UPP[UPP[UPP[Unit]]](NotSpecified, empty2);
	private val empty4 = new UPP[UPP[UPP[UPP[Unit]]]](NotSpecified, empty3);

	def emptyPattern2: Pattern2[
			NotSpecified, NotSpecified, NotSpecified, NotSpecified] = {
		return empty4;
	};
}
*/
}

// import TryOut._
