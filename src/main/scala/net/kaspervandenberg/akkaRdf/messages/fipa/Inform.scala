// © Kasper van den Berg, 2014
package net.kaspervandenberg.akkaRdf.messages.fipa

/**
 * The sender informs the receiver that `content` is true (that is the sender 
 * believes `content` to be true).
 *
 * From the
 * [[http://www.fipa.org/specs/fipa00037/index.html SC00037J FIPA standard]]:
 *
 * ''"`Inform` indicates that the sending agent:''
 *  - ''holds `content` for true;''
 *  - ''intents that the receiving agent also comes to believe that `content`
 *    is true; and''
 *  - ''does not already believe that the receiver has any knowledge of the
 *    truth of `content`."''
 *
 * @param content		The RDF statement (or graph) that is believed and to
 *		inform the receiver about.
 *
 * @tparam T	the class of the RDF statement; the possible types are a subset
 *		of the types of [[Performative]]:
 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Triple]],
 *		  - [[net.kaspervandenberg.akkaRdf.rdf.Rdf.Quadruple]],
 *		  - a [[scala.Seq]] of the above.
 *
 *		`Inform` message containing a 
 *		[[net.kaspervandenberg.akkaRdf.rdf.QuadruplePattern.Pattern]] are 
 *		''not'' supported.  Use `Triples` containing 
 *		[[net.kaspervandenberg.akkaRdf.rdf.Rdf.BNode]] to inform about graphs 
 *		with unidentified parts.
 */
case class Inform[+T](override val content: T) extends Performative[T](content);


