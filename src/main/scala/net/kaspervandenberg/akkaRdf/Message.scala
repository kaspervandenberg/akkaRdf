package net.kaspervandenberg.akkaRdf

import net.kaspervandenberg.akkaRdf.rdf.Rdf._


object Message {
	abstract class MessageRoot

	case class Put(t: Quadruple) extends MessageRoot

	abstract class Get extends MessageRoot;

}


/* vim:set shiftwidth=4 tabstop=4 autoindent fo=cqtwa2 : */ 

