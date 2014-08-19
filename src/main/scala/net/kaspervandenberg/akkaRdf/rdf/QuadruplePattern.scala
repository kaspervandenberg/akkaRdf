// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.rdf;

import net.kaspervandenberg.akkaRdf.rdf.Rdf._;

/**
 * Captures the indexing of
 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple Quadruples]].
 *
 * A `Quadruple` consists of four fields: `graph`, `subject`, `predicate`, and 
 * `rdfObject`.  Any combination of these can be the key on which to index 
 * `Quadruple`s.  This leads to 16 [[Pattern]]s; named: 
 * `"Pattern"('G'|'_')('S'|'_')('P'|'_')('O|'_')`.  For each field there are 
 * two `traits`: `"Fixed"\${FieldName}` and `"Wildcard"\${FieldName}`.
 *
 * == [[QuadruplePattern.Pattern Patterns]] versus 
 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.BNode BNodes]] ==
 *
 * Both a `Pattern` can represent unknown variables, but within a `Pattern` the 
 * variables can not be connected.  The second example from 
 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf]]:
 *
 *     ''Someone is a friend of Alice. That someone is interested in
 *       something.  The video "La Joconde à Washington" is about that
 *       something.''
 *
 * Which is represented using `BNodes` as follows:
 * {{{
 * scala> import java.net.URI
 * scala> import net.kaspervandenberg.akkaRdf.rdf.Rdf._
 *
 * scala> val alice = NamedResource(new URI("http://example.org/alice"))
 * scala> val isFriend = NamedResource(
 *		new URI("http://xmlns.com/foaf/0.1/knows"))
 * scala> val isIntrestedIn = NamedResource(
 *		new URI("http://example.org/rdf/isIntrestedIn"))
 * scala> val videoLaJocondeAWashington = NamedResource(
 *		new URI("http://example.org/videoLaJoconde&#x00C0;Washington"))
 * scala val isAbout = NamedResource(new URI("http://example.org/rdf/about"))
 * scala> val graph3 = NamedResource(new URI("http://example.org/graph3"))
 * scala> val blank1 = BNode()
 * scala> val blank2 = BNode()
 *
 * scala> Seq(
 *		Quadruple(graph3, blank1, isFriend, alice),
 *		Quadruple(graph3, blank1, isIntrestedIn, blank2),
 *		Quadruple(graph3, videoLaJocondeAWashington, isAbout, blank2)
 * )}}}
 *
 * A '''broken''' attempt to represent this with `Patterns`:
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern._
 * scala> val graph4 = NamedResource(new UIR("http://example.org/graph4"))
 * 
 * scala> Seq(
 *		Pattern(Some(graph4), None, Some(isFriend), Some(alice)),
 *		Pattern(Some(graph4), None, Some(isIntrestedIn), None),
 *		Pattern(Some(graph4),
 *						Some(videoLaJocondeAWashington), Some(isAbout), None)
 * )}}}
 *
 * In `graph4` there is no connection among the "friend of Alice", the entity 
 * "interested in" something, and the thing the video "La Joconde a Washington" 
 * is about.
 */
object QuadruplePattern {
	/**
	 * Base of the 16 quadruple patterns.
	 */
	sealed abstract class Pattern {
		 def graph: Option[NamedResource];
		 def subject: Option[Resource];
		 def predicate: Option[NamedResource];
		 def rdfObject: Option[Value];
	}

	/**
	 * Factory method to create any of the 16 subclasses of [[Pattern]].
	 */
	object Pattern {
		/**
		 * Construct any of the 16 subclasses depending on which [[scala.Option]]s 
		 * are filled.
		 */
		def apply(
			g: Option[NamedResource],
			s: Option[Resource],
			p: Option[NamedResource],
			o: Option[Value]) : Pattern = {
				g match {
					case Some(graph) => {
						createFixedGraph(graph, s, p, o);
					}
					case None => {
						createWildcardGraph(s, p, o);
					}
				}
			}

		/**
		 * Create one of
		 *  - [[PatternGSPO]], [[PatternGSP_]], [[PatternGS_O]],
		 *    [[PatternGS__]]; or
		 *  - [[PatternG_PO]], [[PatternG_P_]], [[PatternG__O]],
		 *    [[PatternG___]].
		 *
		 * Depending on `s == Some[Resource]` or `s == None`
		 */
		private def createFixedGraph(
					graph: NamedResource,
					s: Option[Resource],
					p: Option[NamedResource],
					o: Option[Value]) : Pattern = {
			s match {
				case Some(subject) =>
					return createFixedSubjectG(graph, subject, p, o);
				case None =>
					return createWildcardSubjectG(graph, p, o);
			}
		}

		/**
		 * Create one of
		 *  - [[Pattern_SPO]], [[Pattern_SP_]], [[Pattern_S_O]],
		 *    [[Pattern_S__]]; or
		 *  - [[Pattern__PO]], [[Pattern__P_]], [[Pattern___O]],
		 *    [[Pattern____]].
		 *
		 * Depending on `s == Some[Resource]` or `s == None`
		 */
		private def createWildcardGraph(
					s: Option[Resource],
					p: Option[NamedResource],
					o: Option[Value]) : Pattern = {
			s match {
				case Some(subject) =>
					return createFixedSubject_(subject, p, o);
				case None =>
					return createWildcardSubject_(p, o);
			}
		}

		/**
		 * Create one of
		 *  - [[PatternGSPO]], [[PatternGSP_]]; or
		 *  - [[PatternGS_O]], [[PatternGS__]].
		 *
		 * Depending on `p == Some[NamedResource]` or `p == None`
		 */
		private def createFixedSubjectG(
					graph: NamedResource,
					subject: Resource,
					p: Option[NamedResource],
					o: Option[Value]) : Pattern = {
			p match {
				case Some(predicate) =>
					return createFixedPredicateGS(graph, subject, predicate, o);
				case None =>
					return createWildcardPredicateGS(graph, subject, o);
			}
		}

		/**
		 * Create one of
		 *  - [[PatternG_PO]], [[PatternG_P_]]; or
		 *  - [[PatternG__O]], [[PatternG___]].
		 *
		 * Depending on `p == Some[NamedResource]` or `p == None`
		 */
		private def createWildcardSubjectG(
					graph: NamedResource,
					p: Option[NamedResource],
					o: Option[Value]) : Pattern = {
			p match {
				case Some(predicate) =>
					return createFixedPredicateG_(graph, predicate, o);
				case None =>
					return createWildcardPredicateG_(graph, o);
			}
		}

		/**
		 * Create one of
		 *  - [[Pattern_SPO]], [[Pattern_SP_]]; or
		 *  - [[Pattern_S_O]], [[Pattern_S__]].
		 *
		 * Depending on `p == Some[NamedResource]` or `p == None`
		 */
		private def createFixedSubject_(
					subject: Resource,
					p: Option[NamedResource],
					o: Option[Value]) : Pattern = {
			p match {
				case Some(predicate) =>
					return createFixedPredicate_S(subject, predicate, o);
				case None =>
					return createWildcardPredicate_S(subject, o);
			}
		}

		/**
		 * Create one of
		 *  - [[Pattern__PO]], [[Pattern__P_]]; or
		 *  - [[Pattern___O]], [[Pattern____]].
		 *
		 * Depending on `p == Some[NamedResource]` or `p == None`
		 */
		private def createWildcardSubject_(
					p: Option[NamedResource],
					o: Option[Value]) : Pattern = {
			p match {
				case Some(predicate) =>
					return createFixedPredicate__(predicate, o);
				case None =>
					return createWildcardPredicate__(o);
			}
		}

		private def createFixedPredicateGS(
					graph: NamedResource,
					subject: Resource,
					predicate: NamedResource,
					o: Option[Value]) : Pattern = {
			o match {
				case Some(rdfObject) =>
					return new PatternGSPO(graph, subject, predicate, rdfObject);
				case None =>
					return new PatternGSP_(graph, subject, predicate);
			}
		}

		private def createWildcardPredicateGS(
					graph: NamedResource,
					subject: Resource,
					o: Option[Value]) : Pattern = {
			o match {
				case Some(rdfObject) =>
					return new PatternGS_O(graph, subject, rdfObject);
				case None =>
					return new PatternGS__(graph, subject);
			}
		}

		private def createFixedPredicateG_(
					graph: NamedResource,
					predicate: NamedResource,
					o: Option[Value]) : Pattern = {
			o match {
				case Some(rdfObject) =>
					return new PatternG_PO(graph, predicate, rdfObject);
				case None =>
					return new PatternG_P_(graph, predicate);
			}
		}

		private def createWildcardPredicateG_(
					graph: NamedResource,
					o: Option[Value]) : Pattern = {
			o match {
				case Some(rdfObject) =>
					return new PatternG__O(graph, rdfObject);
				case None =>
					return new PatternG___(graph);
			}
		}

		private def createFixedPredicate_S(
					subject: Resource,
					predicate: NamedResource,
					o: Option[Value]) : Pattern = {
			o match {
				case Some(rdfObject) =>
					return new Pattern_SPO(subject, predicate, rdfObject);
				case None =>
					return new Pattern_SP_(subject, predicate);
			}
		}

		private def createWildcardPredicate_S(
					subject: Resource,
					o: Option[Value]) : Pattern = {
			o match {
				case Some(rdfObject) =>
					return new Pattern_S_O(subject, rdfObject);
				case None =>
					return new Pattern_S__(subject);
			}
		}

		private def createFixedPredicate__(
					predicate: NamedResource,
					o: Option[Value]) : Pattern = {
			o match {
				case Some(rdfObject) =>
					return new Pattern__PO(predicate, rdfObject);
				case None =>
					return new Pattern__P_(predicate);
			}
		}

		private def createWildcardPredicate__(
					o: Option[Value]) : Pattern = {
			o match {
				case Some(rdfObject) =>
					return new Pattern___O(rdfObject);
				case None =>
					return Pattern____;
			}
		}


	}

	/**
	 * [[Pattern]]s either
	 * contain a `graph` —[[FixedGraph]]— or
	 * match any `graph` —[[WildcardGraph]]—.
	 */
	sealed trait FixedGraph extends Pattern {
		val actualGraph: NamedResource;
		override def graph = Some(actualGraph);
	}

	object FixedGraph {
		def unapply(pat: FixedGraph): Option[NamedResource] =
			return Some(pat.actualGraph);
	}

	/**
	 * [[Pattern]]s either
	 * contain a `graph` —[[FixedGraph]]— or
	 * match any `graph` —[[WildcardGraph]]—.
	 */
	sealed trait WildcardGraph extends Pattern {
		override def graph = None;
	}

	/**
	 * [[Pattern]]s either
	 * contain a `subject` —[[FixedSubject]]— or
	 * match any `subject` —[[WildcardSubject]]—.
	 */
	sealed trait FixedSubject extends Pattern {
		val actualSubject: Resource;
		override def subject = Some(actualSubject);
	}

	object FixedSubject {
		def unapply(pat: FixedSubject): Option[Resource] =
			return Some(pat.actualSubject);
	}

	sealed trait WildcardSubject extends Pattern {
		override def subject = None;
	}

	/**
	 * [[Pattern]]s either
	 * contain a `predicate` —[[FixedPredicate]]— or
	 * match any `predicate` —[[WildcardPredicate]]—.
	 */
	sealed trait FixedPredicate extends Pattern {
		val actualPredicate: NamedResource;
		override def predicate = Some(actualPredicate);
	}

	object FixedPredicate {
		def unapply(pat: FixedPredicate): Option[NamedResource] =
			return Some(pat.actualPredicate);
	}

	/**
	 * [[Pattern]]s either
	 * contain a `predicate` —[[FixedPredicate]]— or
	 * match any `predicate` —[[WildcardPredicate]]—.
	 */
	sealed trait WildcardPredicate extends Pattern {
		override def predicate = None;
	}

	/**
	 * [[Pattern]]s either
	 * contain a `rdfObject` —[[FixedRdfObject]]— or
	 * match any `rdfObject` —[[WildcardRdfObject]]—.
	 */
	sealed trait FixedRdfObject extends Pattern {
		val actualRdfObject: Value;
		override def rdfObject = Some(actualRdfObject);
	}

	object FixedRdfObject {
		def unapply(pat: FixedRdfObject): Option[Value] =
			return Some(pat.actualRdfObject);
	}

	/**
	 * [[Pattern]]s either
	 * contain a `rdfObject` —[[FixedRdfObject]]— or
	 * match any `rdfObject` —[[WildcardRdfObject]]—.
	 */
	sealed trait WildcardRdfObject extends Pattern {
		override def rdfObject = None;
	}

	case class PatternGSPO(
		actualGraph: NamedResource,
		actualSubject: Resource,
		actualPredicate: NamedResource,
		actualRdfObject: Value)
	extends Pattern
			with FixedGraph
			with FixedSubject
			with FixedPredicate
			with FixedRdfObject;

	object PatternGSPO
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): PatternGSPO =
			PatternGSPO(
					quadruple.graph,
					quadruple.subject,
					quadruple.predicate,
					quadruple.rdfObject)
	}


	case class PatternGSP_(
		actualGraph: NamedResource,
		actualSubject: Resource,
		actualPredicate: NamedResource)
	extends Pattern
			with FixedGraph
			with FixedSubject
			with FixedPredicate
			with WildcardRdfObject;

	object PatternGSP_
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): PatternGSP_ =
			PatternGSP_(
					quadruple.graph,
					quadruple.subject,
					quadruple.predicate)
	}


	case class PatternGS_O(
		actualGraph: NamedResource,
		actualSubject: Resource,
		actualRdfObject: Value)
	extends Pattern
			with FixedGraph
			with FixedSubject
			with WildcardPredicate
			with FixedRdfObject;

	object PatternGS_O
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): PatternGS_O =
			PatternGS_O(
					quadruple.graph,
					quadruple.subject,
					quadruple.rdfObject)
	}


	case class PatternGS__(
		actualGraph: NamedResource,
		actualSubject: Resource)
	extends Pattern
			with FixedGraph
			with FixedSubject
			with WildcardPredicate
			with WildcardRdfObject;

	object PatternGS__
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): PatternGS__ =
			PatternGS__(
					quadruple.graph,
					quadruple.subject)
	}


	case class PatternG_PO(
		actualGraph: NamedResource,
		actualPredicate: NamedResource,
		actualRdfObject: Value)
	extends Pattern
			with FixedGraph
			with WildcardSubject
			with FixedPredicate
			with FixedRdfObject;

	object PatternG_PO
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): PatternG_PO =
			PatternG_PO(
					quadruple.graph,
					quadruple.predicate,
					quadruple.rdfObject)
	}


	case class PatternG_P_(
		actualGraph: NamedResource,
		actualPredicate: NamedResource)
	extends Pattern
			with FixedGraph
			with WildcardSubject
			with FixedPredicate
			with WildcardRdfObject;

	object PatternG_P_
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): PatternG_P_ =
			PatternG_P_(
					quadruple.graph,
					quadruple.predicate)
	}


	case class PatternG__O(
		actualGraph: NamedResource,
		actualRdfObject: Value)
	extends Pattern
			with FixedGraph
			with WildcardSubject
			with WildcardPredicate
			with FixedRdfObject;

	object PatternG__O
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): PatternG__O =
			PatternG__O(
					quadruple.graph,
					quadruple.rdfObject)
	}


	case class PatternG___(
		actualGraph: NamedResource)
	extends Pattern
			with FixedGraph
			with WildcardSubject
			with WildcardPredicate
			with WildcardRdfObject;

	object PatternG___
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): PatternG___ =
			PatternG___(
					quadruple.graph)
	}


	case class Pattern_SPO(
		actualSubject: Resource,
		actualPredicate: NamedResource,
		actualRdfObject: Value)
	extends Pattern
			with WildcardGraph
			with FixedSubject
			with FixedPredicate
			with FixedRdfObject;

	object Pattern_SPO
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): Pattern_SPO =
			Pattern_SPO(
					quadruple.subject,
					quadruple.predicate,
					quadruple.rdfObject)
	}


	case class Pattern_SP_(
		actualSubject: Resource,
		actualPredicate: NamedResource)
	extends Pattern
			with WildcardGraph
			with FixedSubject
			with FixedPredicate
			with WildcardRdfObject;

	object Pattern_SP_
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): Pattern_SP_ =
			Pattern_SP_(
					quadruple.subject,
					quadruple.predicate)
	}


	case class Pattern_S_O(
		actualSubject: Resource,
		actualRdfObject: Value)
	extends Pattern
			with WildcardGraph
			with FixedSubject
			with WildcardPredicate
			with FixedRdfObject;

	object Pattern_S_O
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): Pattern_S_O =
			Pattern_S_O(
					quadruple.subject,
					quadruple.rdfObject)
	}


	case class Pattern_S__(
		actualSubject: Resource)
	extends Pattern
			with WildcardGraph
			with FixedSubject
			with WildcardPredicate
			with WildcardRdfObject;

	object Pattern_S__
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): Pattern_S__ =
			Pattern_S__(
					quadruple.subject)
	}


	case class Pattern__PO(
		actualPredicate: NamedResource,
		actualRdfObject: Value)
	extends Pattern
			with WildcardGraph
			with WildcardSubject
			with FixedPredicate
			with FixedRdfObject;

	object Pattern__PO
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): Pattern__PO =
			Pattern__PO(
					quadruple.predicate,
					quadruple.rdfObject)
	}


	case class Pattern__P_(
		actualPredicate: NamedResource)
	extends Pattern
			with WildcardGraph
			with WildcardSubject
			with FixedPredicate
			with WildcardRdfObject;

	object Pattern__P_
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): Pattern__P_ =
			Pattern__P_(
					quadruple.predicate)
	}


	case class Pattern___O(
		actualRdfObject: Value)
	extends Pattern
			with WildcardGraph
			with WildcardSubject
			with WildcardPredicate
			with FixedRdfObject;

	object Pattern___O
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): Pattern___O =
			Pattern___O(
					quadruple.rdfObject)
	}


	case object Pattern____
	extends Pattern
			with WildcardGraph
			with WildcardSubject
			with WildcardPredicate
			with WildcardRdfObject
	{
		/**
		 * Create a [[Pattern]] from a
		 * [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]]
		 */
		def apply(quadruple: Quadruple): Pattern____.type =
			Pattern____
	}


}
