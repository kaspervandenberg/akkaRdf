package net.kaspervandenberg.akkaRdf.kdtree

object DotOutput {

def writeNodes[T](tree: KDTree[T, _]): String = tree match {
	case t: KDTreeNode[_, _]  ⇒ {
		s"""\t${nodeId(t)} [label="${t.value}"];\n""" +
		writeNodes(t.left) +
		writeNodes(t.right);
	}
	case e: EmptyKDTree[_, _] ⇒
		s"""\t${nodeId(e)} [label="«empty»"];\n"""
}

def writeEdges(tree: KDTree[_, _]): String = tree match {
	case t: KDTreeNode[_, _] ⇒ {
		val edge = s"""\t${nodeId(t)} -> ${nodeId(t.left)}"""
		val labelStart = """label=["≤"""
		val labelClose = t.dimension match {
			case NamedDimension(n, f) ⇒ s""" ${f(t.value)} ($n)];\n"""
			case _ ⇒ s"""];\n"""
		}
		return edge + labelStart + labelClose +
			writeEdges(t.left) +
			writeEdges(t.right)
	}
	case e: EmptyKDTree[_, _] ⇒ ""
}

def nodeId(tree: KDTree[_, _]): String = s"n_${System.identityHashCode(tree)}"

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

