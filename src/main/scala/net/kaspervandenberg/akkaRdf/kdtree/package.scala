// © Kasper van den Berg, 2015
package net.kaspervandenberg.akkaRdf

/**
 * A 𝑘-dimensional tree: store <var>T</var>-nodes organised in
 * 𝑘-dimensions.
 *
 * The tree is implemented as a binary tree that alternates the dimensions 
 * [[scala.math.Ordering]]s.   See
 * [[http://en.wikipedia.org/wiki/K-d_tree k-d-tree at Wikipedia]] for a more 
 * detailed description.
 *
 * <p><a href="http://commons.wikimedia.org/wiki/File:3dtree.png#mediaviewer/File:3dtree.png">
 * <img alt="3dtree.png" width=25% src="http://upload.wikimedia.org/wikipedia/commons/b/b6/3dtree.png">
 * </a><br>"<a   
 * href="http://commons.wikimedia.org/wiki/File:3dtree.png#mediaviewer/File:3dtree.png">3dtree</a>".  
 * Licensed under <a title="GNU General Public License" 
 * href="http://www.gnu.org/licenses/gpl.html">GPL</a> via <a 
 * href="//commons.wikimedia.org/wiki/">Wikimedia Commons</a>.</p>
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
package object kdtree { }


