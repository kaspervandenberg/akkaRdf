// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.rdf;

import java.net.URI;
import scala.language.implicitConversions;
import scala.collection.mutable;
import scala.xml.NamespaceBinding;
import scala.xml.TopScope;
import java.util.NoSuchElementException;


/**
 * DSL to construct [[Rdf.Triple]]s, [[Rdf.Quadruple]]s, graphs, and 
 * [[QuadruplePattern]]s.
 *
 * = Useage =
 *
 * Define an object that extends `DSL`:
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
 * scala> object dslDemo extends DSL {
 *      | }
 * }}}
 * Within the scope of this object (`dslDemo` in this example) you can:
 *  - define prefixes;
 *  - construct [[Rdf.NamedResource]]s;
 *  - construct [[Rdf.BNode]]s; and
 *  - build rdf graphs (i.e. [[scala.Seq sequences]] of
 *    [[Rdf.Quadruple quadruples]]).
 *
 * == Defining an
 * [[http://docs.oracle.com/javase/7/docs/api/java/net/URI.html URI]]-prefix ==
 * Use `definePrefix` to define a `scala.Symbol` mapped to an `URI`.  For 
 * example to assign the prefix 'rdf' to 
 * 'http://www.w3.org/1999/02/22-rdf-syntax-ns#' use:
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
 * scala> object dslDemo extends DSL {
 *      | definePrefix('rdf) := "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 *      | }
 * }}}
 * Alternatively, you can assign `URI`s to prefix-symbols:
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
 * scala> import java.net.URI
 * scala> object dslDemo extends DSL {
 *      | definePrefix('rdf) := new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
 *      | }
 * }}}
 *
 * == Combine prefix and fragment into [[Rdf.NamedResource]] ==
 * After having defined a prefix, you can use it to create `NamedResource`s via 
 * `prefix :: fragment`.
 * 
 * The following example define `NamedResource`s for 
 * [[http://www.w3.org/1999/02/22-rdf-syntax-ns#type 'type']] in the 
 * [[http://www.w3.org/1999/02/22-rdf-syntax-ns# rdf-namespace]], 
 * [[http://www.w3.org/1999/02/22-rdf-syntax-ns#Property]] (also in the 
 * rdf-namespace), and [[http://dbpedia.org/ontology/Film Film]] in the 
 * [[http://dbpedia.org dbpedia]] ontology.
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
 * scala> object dslDemo extends DSL {
 *      | definePrefix('rdf) := "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 *      | definePrefix('dbpedia) := "http://dbpedia.org/ontology/"
 *      |
 *      | val resType = 'rdf :: "type"
 *      | val resProperty = 'rdf :: "Property"
 *      | val resFilm = 'dbpedia :: "Film"
 *      | }
 * }}}
 * Inspecting the values in `dslDemo` yields:
 * {{{
 * scala> dslDemo.resType
 * res0: net.kaspervandenberg.akkaRdf.rdf.Rdf.NamedResource = NamedResource(http://www.w3.org/1999/02/type)
 * 
 * scala> dslDemo.resType
 * res1: net.kaspervandenberg.akkaRdf.rdf.Rdf.NamedResource = NamedResource(http://www.w3.org/1999/02/type)
 * 
 * scala> dslDemo.resProperty
 * res2: net.kaspervandenberg.akkaRdf.rdf.Rdf.NamedResource = NamedResource(http://www.w3.org/1999/02/Property)
 * 
 * scala> dslDemo.resFilm
 * res3: net.kaspervandenberg.akkaRdf.rdf.Rdf.NamedResource = NamedResource(http://dbpedia.org/ontology/Film)
 * }}}
 *
 * === NOTE: ===
 * You '''must''' define the prefix before using it otherwise an 
 * [[DSL.UndefinedPrefixException]] is thrown.
 *
 * === NOTE: ===
 * The `::`-operator uses
 * [[http://docs.oracle.com/javase/7/docs/api/java/net/URI.html#resolve%28java.lang.String%29 URI.resolve(String)]]
 * to combine the fragment with the defined URI prefix.  Not ending the prefix 
 * with '/' or '#' can produce unexpected results:''
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
 * scala> object BROKEN_dslDemo extends DSL {
 *      | definePrefix('notTerminated) := "http://test.org/rootDir/unclosedPath"
 *      |
 *      | val resSurprise1 = 'notTerminated :: "fragment"
 *      | val resSurprise2 = 'notTerminated :: "/slashFragment"
 *      | val resHashesWork = 'notTerminated :: "#hashFragment"
 *      | }
 * }}}
 *
 * Results in:
 * {{{
 * scala> BROKEN_dslDemo.resSurprise1
 * res0: net.kaspervandenberg.akkaRdf.rdf.Rdf.NamedResource = NamedResource(http://test.org/rootDir/fragment)
 * }}}
 * 'unclosedPath' (which actually counted as a file and as a path)' is replaced 
 * with 'fragment'
 *
 * {{{
 * scala>BROKEN_ dslDemo.resSurprise2
 * res1: net.kaspervandenberg.akkaRdf.rdf.Rdf.NamedResource = NamedResource(http://test.org/slashFragment)
 * }}}
 * '/slashFragment' starts from the root replaces the complete hierarchical 
 * part; '/slashFragment' would also replace the hierarchical part if it had 
 * ended in '/' or '#'.
 *
 * {{{
 * scala> BROKEN_dslDemo.resHashesWork
 * res2: net.kaspervandenberg.akkaRdf.rdf.Rdf.NamedResource = NamedResource(http://test.org/rootDir/unclosedPath#hashFragment)
 * }}}
 * '#hashFragment' are appended after the file part of '\'unclosedPath' as 
 * expected.
 *
 * To avoid these surprise terminate the defined uri prefixes with '/' or '#'.
 *
 * == Annonimous (or blank) nodes ==
 * [[Rdf.BNode Blank Rdf nodes]] indicate that a resource exists (or is 
 * supposed to exist) but that its identity is unknwon.  [[Rdf.BNode]] provides 
 * an exmample to clarify this.
 *
 * The `DSL` provides two ways to define a blank node:
 *  - `_blank_ :: "{bnodeId}"`; and
 *  - `∃ ('{bnodeId})`.
 *
 *  (In the end, only one of these options will remain.  The first provides 
 *  similarity with defining named resources and comes close to the turtle/N# 
 *  syntax.  The second is closer to the mathematical concept of blank nodes; 
 *  however using unicode (here u+2203) in program code is frowned upon.)
 *
 * In the first syntax:
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
 * scala> object dslBNodeDemo1 extends DSL {
 *      | val annon = _blank_ :: "something"
 *      | }
 * }}}
 *
 * In the alternative syntax:
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
 * scala> object dslBNodeDemo2 extends DSL {
 *      | val annon = ∃ ('something)
 *      | }
 * }}}
 *
 * Using the same BNode id multiple times within an `DSL`-instance will result 
 * in the same [[Rdf.BNode]]:
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
 * scala> object dslDemo extends DSL {
 *      | val annonDef1 = _blank_ :: "something"
 *      | val annonDef2 = _blank_ :: "something"
 *      | val annonDef3 = \u2203 ('something)
 *      | }
 * }}}
 * Results in (with probably an other UUID):
 * {{{
 * scala> dslDemo.annonDef1
 * res0: net.kaspervandenberg.akkaRdf.rdf.Rdf.BNode = BNode(bd9b6821-c0b0-4ab2-b095-ffbcf3495ad3)
 * 
 * scala> dslDemo.annonDef2
 * res1: net.kaspervandenberg.akkaRdf.rdf.Rdf.BNode = BNode(bd9b6821-c0b0-4ab2-b095-ffbcf3495ad3)
 * 
 * scala> dslDemo.annonDef3
 * res2: net.kaspervandenberg.akkaRdf.rdf.Rdf.BNode = BNode(bd9b6821-c0b0-4ab2-b095-ffbcf3495ad3)
 * }}}
 * The nodes' contents is the the same: a BNode with UUID 
 *  "95f6cb7f-8a50-474d-95bf-4159fac5d3cc".
 *
 * == Graphs ==
 * 
 * Use the following to define a graph:
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
 * scala> object dslDemo extends DSL {
 *      | definePrefix('p) := "http://test.org/rdf/"
 *      | object graph extends Graph('p :: "graph") {
 *      | addTriple( 'p :: "subject" ==> 'p :: "predicate" ==> 'p :: "object" )
 *      | addTriple( 'p :: "subject" ==> 'p :: "predicate" ==> ∃('blank1) )
 *      | addTriple( _blank_ :: "blank1" ==> 'p :: "otherRelation" ==> 'p :: "object" )
 *      | }
 *      | }
 * scala> val quadruples = dslDemo.graph()
 * }}}
 *
 * = Examples =
 * Using the DSL to model the examples from 
 * [[http://www.w3.org/TR/rdf11-primer/ RDF 1.1 Primer]] §3.5. Leads to a 
 * simpeler and easier to understand model compared to the syntax of [[Rdf]].
 * To recapitulatei, the following image depicts the graph <img 
 *  src="http://www.w3.org/TR/2014/NOTE-rdf11-primer-20140225/example-graph.jpg" 
 *  />
 * Modelling it in the DSL:
 * {{{
 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
 * scala> import net.kaspervandenberg.akkaRdf.rdf.Rdf
 * scala> object dslDemo extends DSL {
 *      | definePrefix('ex) := "http://example.org/"
 *      | definePrefix('rdf) := "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 *      | definePrefix('foaf) := "http://xml.ns.com/foaf/0.1/"
 *      | definePrefix('xsd) := "http://www.w3.org/2001/XMLSchema#"
 *      | definePrefix('wikiData) := "http://www.wikidata.org/wiki/"
 *      | definePrefix('auxRdf) := "http://example.org/rdf/"
 *      |
 *      | val work = 'wikiData :: "Property:P800"
 *      | val isBornOn = 'wikiData :: "Property:P569"
 *      | val monaLisa = 'wikiData :: "Q12418"
 *      | val leonardoDaVinci = 'wikiData :: "Q762"
 *      |
 *      | object graph1 extends Graph('ex :: "bobInfo") {
 *      |   addTriple('ex :: "bob" ==> 'foaf :: "knows" ==> 'ex :: "alice")
 *      |   addTriple('ex :: "bob" ==> 'rdf :: "type" ==> 'foaf :: "Person")
 *      |   addTriple('ex :: "bob" ==> isBornOn ==>
 *      |             Rdf.Literal("1990-07-24", 'xsd ::"date"))
 *      |   addTriple('ex :: "bob" ==> 'auxRdf :: "isIntrestedIn" ==> monaLisa)
 *      | }
 *      |
 *      | object graph2 extends Graph('wikiData :: "Special:EntityData/Q12418") {
 *      |   addTriple(leonardoDaVinci ==> work ==> monaLisa)
 *      |   addTriple('ex :: "videoLaJoconde&#x00C0;Washington" ==>
 *      |             'auxRdf :: "about" ==> monaLisa)
 *      | }
 *      | }
 * }}}
 * In the example starts with defining the prefixes for the URIs that it uses.  
 * The example defines values for the elements from `wikiData` that it uses, 
 * since `monaLisa` is more descriptive than `"Q12418"`; the identifiers in the 
 * other namespaces are sufficiently descriptive therefore the example uses 
 * `'foaf :: "Person"` and `'ex :: "bob"` derectely in `addTriple(…)`.
 * The `::`-operator, that combines a prefix `Symbol` with a ''lastPart'' 
 * `String`, is only 
 */
class DSL {
	case class Graph(val graphName:Rdf.NamedResource) {
		/**
		 * Convert a [[Rdf.Resource]] into the subject-part of a triple 
		 * expression.
		 */
		implicit class TripleUnderconstruction_S(
				val subj: Rdf.Resource) {
			/**
			 * Add a predicate to this subject part of a triple expression 
			 * resulting in a
			 * [[TripleUnderconstruction_SP subject–predicate-part]] of a 
			 * triple expression.
			 *
			 * ''Note: this operator intentionally starts with '=' so that it 
			 * has lower precedence than '::'; the alternative (i.e.  the 
			 * '->'-operator) has higher precedence than '::' and the 
			 * expression `'prefix1 :: subject -> 'prefix2 :: predicate` would 
			 * be interpreted as 
			 * `predicate.::((subject.->(prefix2)).::(prefix1))` and not as 
			 * `(subject.::(prefix1)).->(predicate.::(prefix2))` as is 
			 * intented.''
			 */
			def ==>(pred: Rdf.NamedResource): TripleUnderconstruction_SP =
				new TripleUnderconstruction_SP(subj, pred)
		}

		/**
		 * The subject–predicate-part of a triple explession.  Defines the 
		 * '==>'-operator to build a [[Rdf.Triple]].
		 */
		class TripleUnderconstruction_SP(
				val subj: Rdf.Resource,
				val pred: Rdf.NamedResource)
		{
			/**
			 * Add an rdf-object to this subject–predicate partial triple 
			 * expression, returning a [[Rdf.Triple]].
			 *
			 * ''Note: the operator intentionally starts with '=', since the 
			 * precedence of '=' is lower than that of ':'; see 
			 * [[TripleUnderconstruction_S]] for more information.''
			 */
			def ==>(obj: Rdf.Value): Rdf.Triple =
				Rdf.Triple(subj, pred, obj)
		}

		/**
		 * Add a [[Rdf.Triple]] to this graph.
		 * Use `addTriple(subject ==> predicate ==> object)` to add a triple to 
		 * this graph.
		 */
		def addTriple(triple: Rdf.Triple): Unit =
		{
			triples += Rdf.Quadruple(
						graphName,
						triple.subject, triple.predicate, triple.rdfObject)
		}

		/**
		 * Return the [[scala.Seq collection]] of [[Rdf.Quadruple]]s that 
		 * consitute this [[Graph]].
		 */ 
		def apply(): Seq[Rdf.Quadruple] = triples.toSeq

		private var triples: mutable.Set[Rdf.Quadruple] =
				mutable.Set.empty;
	}

	/**
	 * When attempting to use (`::`) a prefix that is not defined with 
	 * `definePrefix`, `UndefinedPrefixException` is thrown.
	 *
	 * Since Scala does not create objects until these objects are used the 
	 * `UndefinedPrefixException` is not thrown when using the undefined prefix 
	 * within the [[DSL]]-object; it is thrown when using the `DSL`-object.
	 *
	 * The following would trigger an `UndefinedPrefixException` upon 
	 * instantiation of the `dslUndefinedPrefixDemo`-object:
	 * {{{
	 * scala> import net.kaspervandenberg.akkaRdf.rdf.DSL
	 * scala> object dslUndefinedPrefixDemo extends DSL {
	 *      | val undefined = 'none :: "foo"
	 *      | }
	 * }}}
	 *
	 * @param	prefix	the referenced prefix
	 * @param	fragment	the fragment where the prefix was referenced (if
	 *		available).
	 */
	class UndefinedPrefixException(
			val prefix: Symbol,
			val fragment: Option[String])
	extends NoSuchElementException(
			UndefinedPrefixException.buildErrorMessage(
				prefix, fragment, DSL.this))

	/**
	 * Helper object that constructs an error message for the 
	 * `UndefinedPrefixException` class.
	 */
	private object UndefinedPrefixException
	{
		private def buildErrorMessage(
				prefix: Symbol,
				fragment: Option[String],
				dslInstance: DSL) =
		{
			buildSummaryPart(prefix) + "; " +
			buildOptionalInExpressionPart(prefix, fragment) +
			buildInDslInstancePart(dslInstance)
		}

		private def buildSummaryPart(prefix: Symbol) =
		{
			s"Prefix ${prefix} is not defined"
		}

		private def buildOptionalInExpressionPart(
				prefix: Symbol,
				fragment: Option[String]) =
		{
			fragment.fold("")(
					(frag: String) =>
						"in expression «%s :: %s» " format(prefix, frag) )
		}

		private def buildInDslInstancePart(dslInstance: DSL) =
		{
			s"in DSL instance ${dslInstance}"
		}
	}

	/**
	 * Helper class that provides the `:=`-operation to allow assigniing an 
	 * [[http://docs.oracle.com/javase/7/docs/api/java/net/URI.html URI]] to a 
	 * prefix symbol.
	 *
	 * Use `definePrefix` from within an object that extends DSL to create a 
	 * `PrefixAssignment`.
	 */
	protected case class PrefixAssignment(val prefix: Symbol)
	{
		def := (uri: URI) : Unit =
		{
			val parent = registeredNamespaceBindings;
			registeredNamespaceBindings = NamespaceBinding(
				prefix.name, uri.toString(), parent)
		}

		def := (strUri: String) : Unit =
			this := new URI(strUri)
	}

	/**
	 * Helper class that provides the `::`-operator to Strings.
	 *  - If the argument before `::` isa `Symbol` identifying a
	 *    [[PrefixBinding]] ∈  registeredNamespaceBindings, the `PrefixBinding` 
	 *    `prefix :: lastPart` is resolved to a [[Rdf.NamedResource]];
	 *  - if the argument is [[_blank_]], `_blank_ :: lastPart` is resolved to
	 *    a [[Rdf.BNode]]
	 *
	 * ''Note: `::`-operator binds right-to-left, thus `prefix :: lastPart` is 
	 * interpreted as lastPart.::(prefix)`; Therefore it is defined in an 
	 * implicit `String`-class (`lastPart` is a `String`) and not in an 
	 * implicit `Symbol`-class (`prefix` is a `Symbol`).''
	 */
	protected implicit class UriLastPart(val lastPart: String)
	{
		def :: (arg: _blank_.type): Rdf.BNode = _blank_ resolve lastPart;
		def :: (prefix: Symbol): Rdf.NamedResource =
		{	findPrefix(prefix).fold(
				throw new UndefinedPrefixException(prefix, Some(lastPart)))
				{ _ resolve lastPart };
		}
	}

	/**
	 * Helper class that contains a `Symobl` bound to an `URI` prefix.  It 
	 * provides the `resolve`-method used in [[UriLastPart UriLastPart.::]].
	 */
	protected case class PrefixBinding(val prefix: Symbol, val uri: URI)
	{
		def resolve(lastPart: String) : Rdf.NamedResource =
		{
			val fullUri = uri.resolve(lastPart);
			usedNamedResources.getOrElseUpdate(
					fullUri,
					{ Rdf.NamedResource(fullUri) });
		}
	}

	/**
	 * Pseudo prefix for [[Rdf.BNode blank nodes]].  The expression: `_blank_ 
	 * :: 'bnodeId` refers to the `BNode` identified by "bnodeId".
	 */
	object _blank_ {
		/**
		 * Resolve `lastPart` into a [[Rdf.BNode]].
		 *
		 *  - If the node was referenced previously within this
		 *    [[DSL]]-instance, the same `BNode` is returned.
		 *  - If `bnodeId` was not used before, a fresh `BNode` is created and
		 *    returned.
		 *
		 * ''Note: Both `_blank_ :: "nodeId"` and `∃('nodeId)` refer to the 
		 * same `BNode`.''
		 *
		 * @param bnodeId	identifier that identifies the `BNode` within the
		 * 		context of this `DSL`.
		 */
		def resolve(bnodeId: String) : Rdf.BNode =
			usedBnodes.getOrElseUpdate( bnodeId, { Rdf.BNode() });
	}


	/**
	 * Refer to the [[Rdf.BNode blank node]] identified by `byNodeId.name`
	 *
	 *  - If the node was referenced previously within this [[DSL]]-instance,
	 *    the same `BNode` is returned.
	 *  - If `bnodeId` was not used before, a fresh `BNode` is created and
	 *    returned.
	 *
	 * ''Note: Both `_blank_ :: "nodeId"` and `∃('nodeId)` refer to the same 
	 * `BNode`.''
	 *
	 * @param bnodeId	identifier that identifies the `BNode` within the
	 * 		context of this `DSL`
	 */
	def ∃ (bnodeId: Symbol): Rdf.BNode = _blank_ resolve bnodeId.name

	/**
	 * Start a prefix definition.
	 *
	 * Within a `DSL`-object do:
	 * {{{
	 * scala> dslDemo extends DSL {
	 *      | definePrefix('rdf) := "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	 * }}}
	 * To define the prefix for the rdf namespace.
	 */
	def definePrefix (prefix: Symbol) : PrefixAssignment =
		PrefixAssignment(prefix)

	private var registeredNamespaceBindings: NamespaceBinding = TopScope;
	private val usedNamedResources: mutable.Map[URI, Rdf.NamedResource] = 
			mutable.Map.empty;
	private val usedBnodes: mutable.Map[String, Rdf.BNode] =
			mutable.Map.empty;

	private def findPrefix(prefix: Symbol) : Option[PrefixBinding] =
	{
		registeredNamespaceBindings getURI prefix.name match
		{
			case (uri: String) => Some(PrefixBinding(
					prefix,
					new URI(uri)))
			case _ => None;
		}
	}



}


