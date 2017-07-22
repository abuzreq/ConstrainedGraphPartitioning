package voronoi_test;

/* Fortune's algorithm for computing the voronoi diagram
 * Useful when all PVectors are known at the start
 * (fastest known algorithm yet)  
 * Greatly inspired by the c++ implementation of Matt Brubeck
 * http://www.cs.hmc.edu/~mbrubeck/voronoi.html
 */
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.jgrapht.graph.SimpleGraph;

import processing.core.PApplet;
import processing.core.PVector;
import search.basic.BasicGraphGenerator;
import search.basic.Border;
import search.basic.Node;
import voronoi_test.VoronoiBuilder.Map.Face;
public class VoronoiBuilder implements BasicGraphGenerator
{

	/**
	 * Given the generated Voronoi diagram,finds the dual graph.
	 * @return
	 */
	public SimpleGraph<Node, Border> getGraph() 
	{
		SimpleGraph<Node, Border> graph = new SimpleGraph<Node, Border>(Border.class);
		for (int i = 0; i < map.facesList.size(); i++)
		{
			if(createIsland)
			{
				if(map.facesList.get(i).water)
				{
					map.facesList.get(i).color = oceanColor;
				}
				else
					graph.addVertex(new FaceNode(map.facesList.get(i), i));
			}
			else
				graph.addVertex(new FaceNode(map.facesList.get(i), i));

		}
		
		Set<Node> set = graph.vertexSet();
		Iterator<Node> it = set.iterator();
		while (it.hasNext()) {
			FaceNode fn = (FaceNode) it.next();

				Set<Node> others = graph.vertexSet();
				Iterator<Node> othersIt = others.iterator();
				while (othersIt.hasNext()) {
					FaceNode otherFN = (FaceNode) othersIt.next();
					if (fn.face.neighbours.contains(otherFN.face)) {
						Border b = new Border(fn, otherFN);
						graph.addEdge((FaceNode) b.getN1(), (FaceNode) b.getN2(), b);
					}
				}
			
		}

		return graph;
	}

	public class FaceNode extends Node
	{
		public Face face;

		public FaceNode(Face face, int value) {
			super(value);
			this.face = face;
			
		}
		public FaceNode(int value,ArrayList<Node> cluster) {
			super(value,cluster);			
		}

		@Override
		public FaceNode clone()
		{
			FaceNode fn = new FaceNode(face, getValue());
			if(!getCluster().isEmpty())
			{
				fn.getCluster().addAll(getCluster());
			}
			return  fn;
		}
	}

	@Override
	public SimpleGraph<Node, Border> generate(int size,Random rand) 
	{
		seed =rand.nextInt(Integer.MAX_VALUE);
		return generateOffline(size, true, false, 500,500,randomDrops,droppingSeed,createIsland,seed,noisyEdges,showText);
	}
	
	
	/***********************************************************/
	
	
	

	static Color oceanColor = color(68, 68, 122);
	static Color defaultOceanColor = color(68, 68, 122);
	static int numPoints;
	public static int width = 600;
	public static int height = 600;
	static boolean drawBorders = false;
	static boolean createIsland = false, noisyEdges = false;
	static boolean showText = true;
	static int seed;
	public synchronized SimpleGraph<Node, Border> generateOffline(int numPoints,boolean randomDrops,int droppingSeed,boolean createIsland,int seed,boolean noisyEdges,boolean showText) {
		return generateOffline(numPoints, false, false, 1, 1,randomDrops,droppingSeed,createIsland,seed,noisyEdges,showText);
	}

	public static VoronoiBuilder builderInstance;
	static boolean randomDrops;
	static private int droppingSeed;
	Drawer currentDrawer;
	public synchronized SimpleGraph<Node, Border> generateOffline(int numPoints, boolean drawMap, boolean drawBorders,
			int width, int height,boolean randomDrops,int droppingSeed,boolean createIsland,int seed,boolean noisyEdges,boolean showText) 
	{
		//System.out.println("Island Seed " +islandSeed);
		VoronoiBuilder.randomDrops = randomDrops;
		VoronoiBuilder.droppingSeed =  droppingSeed;
		VoronoiBuilder.width = width;
		VoronoiBuilder.height = height;
		builderInstance = this;
		
	
		VoronoiBuilder.drawBorders = drawBorders;
		VoronoiBuilder.createIsland = createIsland;
		VoronoiBuilder.noisyEdges = noisyEdges;
		VoronoiBuilder.showText = showText;
		VoronoiBuilder.seed = seed;
		if (!drawMap) 
		{
			currentDrawer = null;
			map = new Map(currentDrawer);
			map.nbPoints = numPoints;
			map.toBeDrawn = drawMap;
			map.createMap(currentDrawer, seed);
			map.createPoints(currentDrawer);
			map.createGraph(currentDrawer);
			return getGraph();
		} else
		{
			PApplet.main("voronoi_test.Drawer");
			currentDrawer = Drawer.instance;
			// main("CSP.VoronoiBuilder");
			map = new Map(currentDrawer);
			map.nbPoints = numPoints;
			map.toBeDrawn = drawMap;
			launchCreation(currentDrawer, seed);

			return getGraph();
		}

	}
	static void buildNoisyEdges(PApplet ap)
	{
	  final float f = 0.5f; 
	  for(VoronoiBuilder.Map.Edge e : map.edgesList) {
	    if(e.f2 == null) continue;
	    
	    PVector m = lerpVector(e.p1.pos, e.p2.pos, f);  // midpoint
	    PVector t = lerpVector(e.p1.pos, e.f1.pos, f);
	    PVector q = lerpVector(e.p1.pos, e.f2.pos, f);
	    PVector r = lerpVector(e.p2.pos, e.f1.pos, f);
	    PVector s = lerpVector(e.p2.pos, e.f2.pos, f);
	    
	    int minLength = 4;
	    if(e.f1.ocean && e.f2.ocean) minLength = 100;
	    if(e.f1.ocean != e.f2.ocean) minLength = 2;  // coast
	    if(e.river > 0) minLength = 3;
	    
	    e.noisy1 = buildNoisyLineSegments(ap,e.p1.pos, t, m, q, minLength);
	    e.noisy2 = buildNoisyLineSegments(ap,e.p2.pos, s, m, r, minLength);
	  }
	}
	static PVector lerpVector(PVector v1, PVector v2, float amt)
	{
	  return new PVector(v1.x + (v2.x - v1.x) * amt, v1.y + (v2.y - v1.y) * amt);
	}

	static void subdivideNoisyLineSegment(PApplet ap,ArrayList<PVector> pts, 
	  PVector A, PVector B, PVector C, PVector D, int minLength)
	{
	  if(PVector.sub(A, C).mag() < minLength || PVector.sub(B, D).mag() < minLength)
	    return;
	    
	  float p = ap.random(0.2f, 0.8f);
	  float q = ap.random(0.2f, 0.8f);
	  
	  PVector E = lerpVector(A, D, p);
	  PVector F = lerpVector(B, C, p);
	  PVector G = lerpVector(A, B, q);
	  PVector I = lerpVector(D, C, q);
	  
	  PVector H = lerpVector(E, F, q);
	  
	  subdivideNoisyLineSegment(ap,pts, A, G, H, E, minLength);
	  pts.add(H);
	  subdivideNoisyLineSegment(ap,pts, H, F, C, I, minLength);
	}

	static ArrayList<PVector> buildNoisyLineSegments(PApplet ap,PVector A, PVector B, PVector C, PVector D, int minLength)
	{
	  ArrayList<PVector> pts = new ArrayList<PVector>();
	  
	  pts.add(A.get());
	  subdivideNoisyLineSegment(ap,pts, A, B, C, D, minLength);
	  pts.add(C.get());
	  return pts;
	}
	public synchronized void draw(PApplet ap)
	{		
		if (builderInstance.map != null) 
		{
			builderInstance.map.draw(ap);		
		}
	}
	

	
	
	boolean startDrawing = false;
	boolean sorted = false;
	
	ArrayList<PVector> coastPoints;
	static Map map;

	void launchCreation(PApplet ap, int seed) {
		map.createMap(ap, seed);
		map.fullCreation(ap);
	}

	LinkedList<Point> toLinkedList(ArrayList<Map.Point> points) {
		LinkedList<Point> newPoints = new LinkedList<Point>();
		for (int i = 0; i < points.size(); i++) {
			newPoints.add(new Point(points.get(i).pos.x, points.get(i).pos.y));
		}
		return newPoints;
	}

	public class TriangleVectorComparator implements Comparator<PVector> {
		private PVector M;

		public TriangleVectorComparator(PVector origin) {
			M = origin;
		}

		public int compare(PVector o1, PVector o2) {
			double angle1 = Math.atan2(o1.y - M.y, o1.x - M.x);
			double angle2 = Math.atan2(o2.y - M.y, o2.x - M.x);

			// For counter-clockwise, just reverse the signs of the return
			// values
			if (angle1 < angle2)
				return 1;
			else if (angle2 < angle1)
				return -1;
			return 0;
		}

	}

	public void keyPressed(PApplet ap) {
		switch (ap.keyCode) {
		// Create another map
		case ' ':
			int seed = ap.millis();
			PApplet.println(seed);
			launchCreation(ap, seed);
			break;

		case 'N':
			map.createNoisyEdges = !map.createNoisyEdges;

			for (Map.Edge e : map.edgesList) {
				e.noisy1 = null;
				e.noisy2 = null;
			}

			break;

		}
	}

	static class Edge<Point> {

		Point first, second;

		public Edge(Point first, Point second) {
			this.first = first;
			this.second = second;
			
		}

		public void set(Point first, Point second) {
			this.first = first;
			this.second = second;
		}

		public Point getFirst() {
			return first;
		}

		public void setFirst(Point first) {
			this.first = first;
		}

		public Point getSecond() {
			return second;
		}

		public void setSecond(Point second) {
			this.second = second;
		}
		public String toString()
		{
			return "["+first + " : " + second+"]" ;
		}
	}

	 public static class Point {
		protected float x, y;

		public Point(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public Point set(float x, float y) {
			this.x = x;
			this.y = y;
			return this;
		}

		@Override
		public String toString() {
			return "(" + x + "," + y + ")";
		}

		@Override
		public boolean equals(Object other) {
			if (other != null && other instanceof Point) {
				Point p = (Point) other;
				return (x == p.x) && (y == p.y);
			} else
				return false;
		}

		@Override
		public int hashCode() {
			return (x + "" + y).hashCode();
		}
	}

	 public boolean isAdjacent(LinkedList<Point> poly1, LinkedList<Point> poly2)
{
		if (poly1 == null || poly2 == null)
			return false;
		if (poly1.size() <= 2 || poly2.size() <= 2)
			return false;
		int numOfVerticesInBoth = 0;
		for (int i = 0; i < poly1.size(); i++) {
			if (poly2.contains(poly1.get(i)))
				numOfVerticesInBoth++;
		}
		return numOfVerticesInBoth >= 2;
}

	static Color color(double r, double g, double b) {
		return new Color(PApplet.map((float) r, 0, 255, 0, 1), PApplet.map((float) g, 0, 255, 0, 1), PApplet.map(
				(float) b, 0, 255, 0, 1));
	}
	private static Random alternateRand = new Random();
	static Color randomColor(PApplet ap)
	{
		if(ap != null)
			return new Color(ap.random(1), ap.random(1), ap.random(1));
		else
		{
			return new Color(alternateRand.nextFloat(),alternateRand.nextFloat(), alternateRand.nextFloat());
		}

	}
	static Color color(double c) {
		float nc = PApplet.map((float) c, 0, 255, 0, 1);
		return new Color(nc, nc, nc);
	}
	
	static int colorToInt(Color color) {
		return color.getRGB();
	}

	static class Voronoi {
		final float epsilon = 0.01f;
		boolean ok;

		class Event {
			float x;
			PVector p;
			Arc a;
			boolean valid;

			Event(float x, PVector p, Arc a) {
				this.x = x;
				this.p = p;
				this.a = a;
				valid = true;
			}
		}

		class Site {
			PVector pos;
			LinkedList<PVector> points;

			Site(PVector p) {
				pos = p;
				points = new LinkedList<PVector>();
			}

			boolean addSegment(Segment s) {
				if (points.size() == 0) {
					points.add(s.getStart(this));
					points.add(s.getEnd(this));
					return true;
				} else {
					if (points.getLast() == s.getStart(this)) {
						points.addLast(s.getEnd(this));
						return true;
					} else if (points.getFirst() == s.getEnd(this)) {
						points.addFirst(s.getStart(this));
						return true;
					} else
						return false;
				}
			}
		}

		class Arc {
			Site site;
			Arc prev, next; // For the time being, a linked list
			Event e;
			Segment s0, s1;

			Arc(Site s, Arc a, Arc b) {
				this.site = s;
				prev = a;
				next = b;
			}
		}

		class Segment {
			PVector p0, p1;
			Site s0, s1;
			boolean done;

			Segment(PVector p, Site s0, Site s1) {
				p0 = pointsLookup.getPoint(p);
				done = false;
				this.s0 = s0;
				this.s1 = s1;
				segments.add(this);
			}

			void finish(PVector p) {
				if (done)
					return;
				p1 = pointsLookup.getPoint(p);
				done = true;

				// s0 must be on the "left" of this segment
				PVector v0 = PVector.sub(p0, s0.pos), v1 = PVector.sub(p1, p0);
				if (v0.x * v1.y - v0.y * v1.x < 0) { // z coordinate of the
														// cross
														// product
					Site tmp = s0;
					s0 = s1;
					s1 = tmp;
				}
			}

			PVector getStart(Site s) {
				if (s == s0)
					return p0;
				else
					return p1;
			}

			PVector getEnd(Site s) {
				if (s == s0)
					return p1;
				else
					return p0;
			}
		}

		class SegmentPair {
			Segment s0, s1;

			SegmentPair(Segment s0, Segment s1) {
				this.s0 = s0;
				this.s1 = s1;
			}

			void fusion() {
				s0.p0 = s1.p1;
				segments.remove(s1);
			}
		}

		class SiteComparator implements Comparator<Site> {
			public int compare(Site p1, Site p2) {
				return (p1.pos.x < p2.pos.x ? -1 : (p1.pos.x > p2.pos.x ? 1 : 0));
			}
		}

		class EventComparator implements Comparator<Event> {
			public int compare(Event e1, Event e2) {
				return (e1.x < e2.x ? -1 : (e1.x > e2.x ? 1 : 0));
			}
		}

		PriorityQueue<Site> sitesQueue;
		PriorityQueue<Event> events;
		Vector<Segment> segments;
		Vector<Site> sites;
		Vector<SegmentPair> joignableSegments;
		public PointsLookup pointsLookup;
		Arc root;
		float xmin, xmax, ymin, ymax;
		PVector tl, tr, bl, br;

		void setBoundaries(float left, float top, float right, float bottom) {
			xmin = left;
			xmax = right;
			ymin = top;
			ymax = bottom;
			tl = new PVector(left, top);
			tr = new PVector(right, top);
			bl = new PVector(left, bottom);
			br = new PVector(right, bottom);
		}

		void computeVoronoi(Vector<PVector> pts) {
			ok = true;
			root = null;
			sitesQueue = new PriorityQueue<Site>(pts.size(), new SiteComparator());
			events = new PriorityQueue<Event>(pts.size(), new EventComparator());
			segments = new Vector<Segment>();
			sites = new Vector<Site>();
			joignableSegments = new Vector<SegmentPair>();
			pointsLookup = new PointsLookup(xmin, ymin, xmax, ymax, 10, epsilon);

			for (PVector p : pts) {
				Site s = new Site(p);
				sites.add(s);
				sitesQueue.offer(s);
			}

			// Select the next event or PVector with smaller x value
			while (!sitesQueue.isEmpty()) {
				if (!events.isEmpty() && events.peek().x <= sitesQueue.peek().pos.x)
					processEvent();
				else
					processSite();
			}

			// Only circle events remaining
			while (!events.isEmpty())
				processEvent();

			// Cut dangling half lines
			finishEdges();

			// Join half-segments into whole segments
			joinSegments();

			// Remove segments outside boundaries and cut to the boundaries
			cleanSegments();

			// Use the segments to create polygons around sites
			createPolygons();
		}

		void processSite() {
			// We will add a new arc to the beach front, using the next site
			Site s = sitesQueue.poll();
			if (root == null) {
				root = new Arc(s, null, null);
				return;
			}

			// Find the arc at the same height of p
			for (Arc i = root; i != null; i = i.next) {
				PVector pt = new PVector();
				if (intersectionParabolaArc(s.pos, i, pt)) {
					Site s2 = i.site;
					// New parabola intersects arc i, we my have to duplicate it
					if (i.next != null && !intersectionParabolaArc(s.pos, i.next, null)) {
						i.next.prev = new Arc(i.site, i, i.next);
						i.next = i.next.prev;
					} else
						i.next = new Arc(i.site, i, null);
					i.next.s1 = i.s1;

					// Place the arc from p between i and i.next
					i.next.prev = new Arc(s, i, i.next);
					i.next = i.next.prev;

					i = i.next;

					// Start new half lines
					i.prev.s1 = i.s0 = new Segment(pt, i.site, s2);
					i.next.s0 = i.s1 = new Segment(pt, i.site, s2);
					joignableSegments.add(new SegmentPair(i.s0, i.s1));

					// Create new events if necessary
					checkCircleEvent(i, s.pos.x);
					checkCircleEvent(i.prev, s.pos.x);
					checkCircleEvent(i.next, s.pos.x);

					return;
				}
			}

			// If there are no intersections, put the new arc at the end
			Arc i;
			for (i = root; i.next != null; i = i.next)
				;
			i.next = new Arc(s, i, null);
			// New segment between p and i
			PVector start = new PVector(xmin, (i.next.site.pos.y + i.site.pos.y) / 2);
			i.s1 = i.next.s0 = new Segment(start, s, null);
		}

		void processEvent() {
			Event e = events.poll();
			if (!e.valid)
				return;

			Segment s = new Segment(e.p, e.a.next.site, e.a.prev.site);

			// Remove corresponding arc
			Arc a = e.a;
			if (a.prev != null) {
				a.prev.next = a.next;
				a.prev.s1 = s;
			}
			if (a.next != null) {
				a.next.prev = a.prev;
				a.next.s0 = s;
			}

			// Finish segments
			if (a.s0 != null)
				a.s0.finish(e.p);
			if (a.s1 != null)
				a.s1.finish(e.p);

			// Check for events
			if (a.prev != null)
				checkCircleEvent(a.prev, e.x);
			if (a.next != null)
				checkCircleEvent(a.next, e.x);
		}

		boolean intersectionParabolaArc(PVector p, Arc a, PVector res) {
			if (a.site.pos.x == p.x)
				return false;

			double i0 = 0, i1 = 0;
			if (a.prev != null) // Intersection of i.prev, i
				i0 = intersectionParabolas(a.prev.site.pos, a.site.pos, p.x).y;
			if (a.next != null) // Intersection of i, i.next
				i1 = intersectionParabolas(a.site.pos, a.next.site.pos, p.x).y;

			if ((a.prev == null || i0 <= p.y) && (a.next == null || i1 >= p.y)) {
				if (res != null) {
					res.y = p.y;
					res.x = (a.site.pos.x * a.site.pos.x + (a.site.pos.y - res.y) * (a.site.pos.y - res.y) - p.x * p.x)
							/ (2 * a.site.pos.x - 2 * p.x);
				}
				return true;
			}

			return false;
		}

		PVector intersectionParabolas(PVector p0, PVector p1, float l) {
			PVector res = new PVector(), p = p0;
			if (p0.x == p1.x)
				res.y = (p0.y + p1.y) / 2;
			else if (p1.x == l)
				res.y = p1.y;
			else if (p0.x == l) {
				res.y = p0.y;
				p = p1;
			} else {
				float s0 = 2 * (p0.x - l), s1 = 2 * (p1.x - l);
				float a = 1 / s0 - 1 / s1;
				float b = -2 * (p0.y / s0 - p1.y / s1);
				float c = (p0.y * p0.y + p0.x * p0.x - l * l) / s0 - (p1.y * p1.y + p1.x * p1.x - l * l) / s1;
				res.y = (-b - PApplet.sqrt(b * b - 4 * a * c)) / (2 * a);
			}
			res.x = (p.x * p.x + (p.y - res.y) * (p.y - res.y) - l * l) / (2 * p.x - 2 * l);
			return res;
		}

		void checkCircleEvent(Arc a, float x0) {
			// Invalidate events
			if (a.e != null && a.e.x != x0)
				a.e.valid = false;
			a.e = null;
			if (a.prev == null || a.next == null)
				return;

			CircleResponse r = computeCircle(a.prev.site.pos, a.site.pos, a.next.site.pos);
			if (r.b) {
				a.e = new Event(r.x, r.p, a);
				events.offer(a.e);
			}
		}

		class CircleResponse {
			PVector p;
			float x;
			boolean b;

			CircleResponse(boolean b) {
				p = new PVector();
				this.b = b;
			}
		}

		CircleResponse computeCircle(PVector a, PVector b, PVector c) {
			// BC must be a right turn from AB
			if ((b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y) > 0)
				return new CircleResponse(false);

			// Algorithm from O'Rourke 2ed p. 189.
			float A = b.x - a.x, B = b.y - a.y, C = c.x - a.x, D = c.y - a.y, E = A * (a.x + b.x) + B * (a.y + b.y), F = C
					* (a.x + c.x) + D * (a.y + c.y), G = 2 * (A * (c.y - b.y) - B * (c.x - b.x));

			// co-linear
			if (G == 0)
				return new CircleResponse(false);

			// p is the center
			CircleResponse r = new CircleResponse(true);
			r.p.x = (D * E - B * F) / G;
			r.p.y = (A * F - C * E) / G;

			// max x coordinate
			r.x = r.p.x + PApplet.sqrt(PApplet.pow(a.x - r.p.x, 2) + PApplet.pow(a.y - r.p.y, 2));
			return r;
		}

		void finishEdges() {
			float l = xmax + (xmax - xmin) + (ymax - ymin);
			for (Arc i = root; i.next != null; i = i.next)
				if (i.s1 != null)
					i.s1.finish(intersectionParabolas(i.site.pos, i.next.site.pos, l * 2));
		}

		void joinSegments() {
			for (SegmentPair sp : joignableSegments)
				sp.fusion();
			joignableSegments.clear();
		}

		void cleanSegments() {
			Iterator<Segment> iter = segments.iterator();
			while (iter.hasNext()) {
				Segment s = iter.next();
				if (s.p0 == null || s.p1 == null || s.s1 == null) {
					iter.remove();
					continue;
				}

				// Testing if a PVector lies outside the boundaries
				boolean o0 = false, o1 = false;
				if (s.p0.x < xmin || s.p0.x > xmax || s.p0.y < ymin || s.p0.y > ymax)
					o0 = true;
				if (s.p1.x < xmin || s.p1.x > xmax || s.p1.y < ymin || s.p1.y > ymax)
					o1 = true;

				// 2 PVectors outside
				if (o0 && o1) {
					iter.remove();
					continue;
				}

				if (o0) {
					PVector t = pointsLookup.getPoint(intersectionSegments(s, tl, tr));
					if (t != null) {
						s.p0 = t;
						continue;
					}
					t = intersectionSegments(s, tr, br);
					if (t != null) {
						s.p0 = t;
						continue;
					}
					t = intersectionSegments(s, br, bl);
					if (t != null) {
						s.p0 = t;
						continue;
					}
					t = intersectionSegments(s, bl, tl);
					if (t != null) {
						s.p0 = t;
						continue;
					}
				}

				if (o1) {
					PVector t = pointsLookup.getPoint(intersectionSegments(s, tl, tr));
					if (t != null) {
						s.p1 = t;
						continue;
					}
					t = intersectionSegments(s, tr, br);
					if (t != null) {
						s.p1 = t;
						continue;
					}
					t = intersectionSegments(s, br, bl);
					if (t != null) {
						s.p1 = t;
						continue;
					}
					t = intersectionSegments(s, bl, tl);
					if (t != null) {
						s.p1 = t;
						continue;
					}
				}
			}
		}

		PVector intersectionSegments(Segment s, PVector p1, PVector p2) {
			final float eps = 1e-10f;

			PVector p3 = s.p0, p4 = s.p1;
			float den = (p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.y - p1.y);
			float numa = (p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x);
			float numb = (p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x);

			if (PApplet.abs(numa) < eps && PApplet.abs(numb) < eps && PApplet.abs(den) < eps) // Coincident
				return PVector.add(PVector.mult(p1, 0.5f), PVector.mult(p2, 0.5f));

			if (PApplet.abs(den) < eps)
				return null; // Parallel

			float mua = numa / den, mub = numb / den;
			if (mua < 0 || mua > 1 || mub < 0 || mub > 1)
				return null; // Outside of the segments

			return PVector.add(PVector.mult(p1, 1 - mua), PVector.mult(p2, mua));
		}

		class SegmentSitePair {
			Segment seg;
			Site site;

			SegmentSitePair(Segment se, Site si) {
				seg = se;
				site = si;
			}
		}

		void createPolygons() {
			// Register segments to corresponding sites
			LinkedList<SegmentSitePair> queue = new LinkedList<SegmentSitePair>();
			for (Segment s : segments) {
				if (s.p0 == s.p1)
					continue; // 0 length
				if (!s.s0.addSegment(s))
					queue.offer(new SegmentSitePair(s, s.s0));
				if (!s.s1.addSegment(s))
					queue.offer(new SegmentSitePair(s, s.s1));
			}

			for (int i = 0; !queue.isEmpty() && i < 10; i++) {
				Iterator<SegmentSitePair> iter = queue.iterator();
				while (iter.hasNext()) {
					SegmentSitePair s = iter.next();
					if (s.site.addSegment(s.seg))
						iter.remove();
				}
			}

			if (queue.size() > 0) {
				ok = false;
				PApplet.println("Queue non empty : " + queue.size());
				for (SegmentSitePair s : queue) {
					// println("(" + s.seg.p0.x + ":" + s.seg.p0.y + ") (" +
					// s.seg.p1.x + ":" + s.seg.p1.y + ") " + s.site);
					PApplet.println(s.seg.p0 + " " + s.seg.p1 + " " + s.site);
				}
			}

			// Removing empty sites (it happens)
			/*
			 * Iterator<Site> it = sites.iterator(); int i=0;
			 * while(it.hasNext()) { Site s = it.next(); if(s.points.size() ==
			 * 0) { println("Removed site : " + i + " (" + s.pos.x + ":" +
			 * s.pos.y + ")"); it.remove(); } i++; }
			 */
			// Closing holes (at the boundaries)
			for (Site s : sites) {
				if (s.points.isEmpty())
					continue;
				if (s.points.getFirst() == s.points.getLast())
					continue;
				int test = 0;
				PVector p = s.points.getFirst();
				if (p.x < xmin + epsilon)
					test += 1;
				else if (p.x > xmax - epsilon)
					test += 2;
				if (p.y < ymin + epsilon)
					test += 4;
				else if (p.y > ymax - epsilon)
					test += 8;

				p = s.points.getLast();
				if (p.x < xmin + epsilon)
					test += 1;
				else if (p.x > xmax - epsilon)
					test += 2;
				if (p.y < ymin + epsilon)
					test += 4;
				else if (p.y > ymax - epsilon)
					test += 8;

				switch (test) {
				case 5:
					s.points.addFirst(tl);
					s.points.addLast(tl);
					break;
				case 6:
					s.points.addFirst(tr);
					s.points.addLast(tr);
					break;
				case 9:
					s.points.addFirst(bl);
					s.points.addLast(bl);
					break;
				case 10:
					s.points.addFirst(br);
					s.points.addLast(br);
					break;
				default:
					s.points.addLast(s.points.getFirst());
				}
			}

			// Make sure all points are within the boundaries
			for (PVector p : pointsLookup.getPoints()) {
				p.x = PApplet.constrain(p.x, xmin, xmax);
				p.y = PApplet.constrain(p.y, ymin, ymax);
			}
		}
	}

	// Because I am messy when creating polygons, I have to look for similar
	// points
	static class PointsLookup {
		PointsLookup(float xmin, float ymin, float xmax, float ymax, int cellSize, float tolerance) {
			_xmin = xmin;
			_xmax = xmax;
			_ymin = ymin;
			_ymax = ymax;
			_cs = cellSize;
			_tol = tolerance;
			_tol2 = tolerance * tolerance;
			_data = new Vector<Vector<PVector>>();
			float fw = xmax - xmin, fh = ymax - ymin;
			_w = PApplet.ceil(fw / (float) _cs);
			_h = PApplet.ceil(fh / (float) _cs); // grid of 10x10 pixels
			int s = _w * _h;
			for (int i = 0; i < s; i++)
				_data.add(new Vector<PVector>());
			_points = new Vector<PVector>();
		}

		PVector getPoint(float x, float y) {
			// if outside boundaries
			if ((x < _xmin - _tol || x > _xmax + _tol) && (y < _ymin - _tol || y > _ymax + _tol))
				return new PVector(x, y); // don't save that point for lookup

			int px = PApplet.constrain(PApplet.floor((x - _xmin) / _cs), 0, _w - 1);
			int py = PApplet.constrain(PApplet.floor((y - _ymin) / _cs), 0, _h - 1);

			// first try the most plausible location
			Vector<PVector> pts = _data.get(py * _w + px);
			for (PVector p : pts) {
				float dx = x - p.x, dy = y - p.y;
				if (dx * dx + dy * dy <= _tol2)
					return p;
			}

			// then check the neighbours
			for (int gy = PApplet.max(py - 1, 0); gy <= PApplet.min(py + 1, _h - 1); gy++) {
				for (int gx = PApplet.max(px - 1, 0); gx <= PApplet.min(px + 1, _w - 1); gx++) {
					if (gx == px && gy == py) // already tested
						continue;
					pts = _data.get(gy * _w + gx);
					for (PVector p : pts) {
						float dx = x - p.x, dy = y - p.y;
						if (dx * dx + dy * dy <= _tol2)
							return p;
					}
				}
			}

			PVector pt = new PVector(x, y);
			_points.add(pt);
			_data.get(py * _w + px).add(pt);
			return pt;
		}

		PVector getPoint(PVector p) {
			if (p == null)
				return null;
			return getPoint(p.x, p.y);
		}

		Vector<PVector> getPoints() {
			return _points;
		}

		private float _tol, _tol2;
		private int _w, _h, _cs;
		private float _xmin, _xmax, _ymin, _ymax;
		private Vector<PVector> _points;
		private Vector<Vector<PVector>> _data;
	}

	static class PoissonDistribution {
		/**
		 * From "Fast Poisson Disk Sampling in Arbitrary Dimensions" by Robert
		 * Bridson
		 * http://www.cs.ubc.ca/~rbridson/docs/bridson-siggraph07-poissondisk
		 * .pdf
		 **/

		PoissonDistribution() {
			_points = new Vector<PVector>();
		}

		Vector<PVector> getPoints() {
			return _points;
		}
		private static float alternateRandomRange(float min , float max)
		{			
			return min + alternateRand.nextFloat()*(max-min);
		}
		Vector<PVector> generate(PApplet ap, float xmin, float ymin, float xmax, float ymax, float minDist,
				int rejectionLimit) {
			_xmin = xmin;
			_xmax = xmax;
			_ymin = ymin;
			_ymax = ymax;
			_cellSize = minDist / PApplet.sqrt(2);
			_gridWidth = PApplet.ceil((xmax - xmin) / _cellSize);
			_gridHeight = PApplet.ceil((ymax - ymin) / _cellSize);
			int s = _gridWidth * _gridHeight;
			_grid = new ArrayList<Vector<PVector>>();
			for (int i = 0; i < s; i++)
				_grid.add(new Vector<PVector>());

			_points.clear();
			LinkedList<PVector> processList = new LinkedList<PVector>();
			PVector p;
			if(ap != null)
			{
				 p = new PVector(ap.random(_xmin, _xmax), ap.random(_ymin, _ymax));
			}
			else
			{
				 p = new PVector(alternateRandomRange(_xmin, _xmax),alternateRandomRange(_ymin, _ymax));
			}
			processList.add(p);
			_points.add(p);
			addToGrid(p);

			while (processList.size() > 0) {
				float r;
				if(ap != null)
				 r = ap.random(processList.size());
				else
					r = alternateRandomRange(0, processList.size());
				
				int i = PApplet.floor(r);
				p = processList.get(i);
				processList.remove(i);
				for (i = 0; i < rejectionLimit; i++) {
					PVector n = createRandomPointAround(ap, p, minDist, minDist * 2);
					if (insideBoundaries(n) && testGrid(n, minDist)) {
						processList.add(n);
						_points.add(n);
						addToGrid(n);
					}
				}
			}

			return _points;
		}

		boolean insideBoundaries(PVector p) {
			return (p.x >= _xmin && p.x < _xmax && p.y >= _ymin && p.y < _ymax);
		}

		PVector createRandomPointAround(PApplet ap, PVector p, float minDist, float maxDist) 
		{
			float a,r;
			if(ap != null)
			{
				 a = ap.random(2 * PApplet.PI);
				 r = ap.random(minDist, maxDist);
			}
			else
			{
				 a = alternateRandomRange(0,2 * PApplet.PI);
				 r = alternateRandomRange(minDist, maxDist);
			}
			return new PVector(p.x + r * PApplet.cos(a), p.y + r * PApplet.sin(a));
		}

		// return true if there are no points inside the circle of minDist
		// radius around p
		boolean testGrid(PVector p, float minDist) {
			int minX = PApplet.floor(PApplet.max(0, (p.x - minDist - _xmin) / _cellSize));
			int maxX = PApplet.ceil(PApplet.min(_gridWidth - 1, (p.x + minDist - _xmin) / _cellSize));
			int minY = PApplet.floor(PApplet.max(0, (p.y - minDist - _ymin) / _cellSize));
			int maxY = PApplet.ceil(PApplet.min(_gridHeight - 1, (p.y + minDist - _ymin) / _cellSize));

			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					Vector<PVector> cell = _grid.get(y * _gridWidth + x);
					for (PVector t : cell)
						if (PApplet.dist(p.x, p.y, t.x, t.y) <= minDist)
							return false;
				}
			}

			return true;
		}

		void addToGrid(PVector p) {
			_grid.get(index(p.x, p.y)).add(p);
		}

		protected int index(float x, float y) {
			int gx = PApplet.floor((x - _xmin) / _cellSize);
			int gy = PApplet.floor((y - _ymin) / _cellSize);
			return gy * _gridWidth + gx;
		}

		private ArrayList<Vector<PVector>> _grid;
		private float _cellSize;
		private int _gridWidth, _gridHeight;
		private float _xmin, _xmax, _ymin, _ymax;
		private Vector<PVector> _points;
	}

	static class Map {
		int nbPoints = 1000;
		float relaxEdgesAmount = 0.5f; // uniformization of the edges length
										// (for
										// polygonal maps)
		float noiseScale = 3; // higher means more detailled features (if enough
								// points to represent them)
		float landMassFractionMin = 0.3f; // [0;1] fraction of points required
											// to
											// be land
		float landMassFractionMax = 0.7f;
		float riversFraction = 0.15f; // [0;1] influence the number of rivers
		float lakeThreshold = 0.3f; // [0;1] fraction of water points required
									// for a face to become a lake
		boolean createNoisyEdges = true; // straight or noisy edges ?
		final static float epsilon = 0.1f;

		static Color color(double r, double g, double b) {
			return new Color(PApplet.map((float) r, 0, 255, 0, 1), PApplet.map((float) g, 0, 255, 0, 1), PApplet.map(
					(float) b, 0, 255, 0, 1));
		}

		static Color[] biomesColor = { color(0, 0, 0), // void
				oceanColor, // Ocean
				color(47, 102, 102), // Marsh
				color(153, 255, 255), // Ice
				color(51, 102, 153), // Lake
				color(160, 144, 119), // Beach
				color(255, 255, 255), // Snow
				color(187, 187, 170), // Tundra
				color(136, 136, 136), // Bare
				color(85, 85, 85), // Scorched
				color(153, 170, 119), // Taiga
				color(136, 153, 119), // Shrubland
				color(201, 210, 155), // Temperate desert
				color(68, 136, 85), // Temperate rain forest
				color(103, 148, 89), // Temperate deciduous forest
				color(136, 170, 85), // Grassland
				color(51, 119, 85), // Tropical rain forest
				color(85, 153, 68), // Tropical seasonal forest
				color(210, 185, 139), // Subtropical desert
		};

		final Color colorRiver = color(34, 85, 136);
		final Color colorCoast = color(51, 51, 90);

		public static class Point {
			int index;
			PVector pos;
			boolean water, ocean, coast, border;
			float elevation, moisture;
			int river;
			Point downslope;

			ArrayList<Face> faces;
			ArrayList<Edge> edges;
			ArrayList<Point> neighbours;

			Point(PVector tpos) {
				pos = tpos.get();
				faces = new ArrayList<Face>();
				edges = new ArrayList<Edge>();
				neighbours = new ArrayList<Point>();

				water = ocean = coast = false;
				border = (pos.x <= epsilon || pos.y <= epsilon || pos.x >= width - epsilon || pos.y >= height - epsilon);
				elevation = moisture = 0.0f;
				river = 0;
			}
			public String toString()
			{
				return pos.toString();
			}
		}

		static class Edge {
			int index;
			Face f1, f2;
			Point p1, p2;
			float strokeWeight = 0.15f;
			boolean coast, shore;
			ArrayList<PVector> noisy1, noisy2; // 2 noisy half edges
			int river;

			Edge() {
				river = 0;
				coast = shore = false;
			}

			boolean contains(Point p) {
				return (p1 == p || p2 == p);
			}

			public String toString() {
				return "["+p1.pos.x + " : " + p1.pos.y + " : " + p2.pos.x + " : " + p2.pos.y+"]("+strokeWeight+")";
			}
		}

		public static class Face {
			public Color color;
			int index;
			PVector pos;
			boolean water, ocean, coast, border;

			ArrayList<Face> neighbours;
			ArrayList<Edge> edges;
			ArrayList<Point> points;

			Face() {
				neighbours = new ArrayList<Face>();
				edges = new ArrayList<Edge>();
				points = new ArrayList<Point>();
				water = ocean = coast = border = false;
			}

			void drawSimple(PApplet ap) {
				ap.beginShape();
				for (Point p : points)
					ap.vertex(p.pos.x, p.pos.y);
				ap.endShape();
			}
			float t = 0 ;
			void drawNoisy(PApplet ap) 
			{
				if(this.color != Color.WHITE)
				{
					//System.out.println(this.edges);
					ap.beginShape();
					for (Edge e : edges) 
					{
						ap.stroke(colorToInt(biomesColor[5]));		
						ap.strokeWeight(e.strokeWeight);
						t += 0.01f;
						if (e.f1 == this) 
						{
							if (e.noisy1 == null || e.noisy2 == null) {
								ap.vertex(e.p1.pos.x, e.p1.pos.y);
								ap.vertex(e.p2.pos.x, e.p2.pos.y);
							} else {
								drawPathForwards(ap, e.noisy1);
								drawPathBackwards(ap, e.noisy2);
							}
						} else { // reverse order
							if (e.noisy1 == null || e.noisy2 == null) {
								ap.vertex(e.p2.pos.x, e.p2.pos.y);
								ap.vertex(e.p1.pos.x, e.p1.pos.y);
							} else {
								drawPathForwards(ap, e.noisy2);
								drawPathBackwards(ap, e.noisy1);
							}
						}
					}
					ap.endShape(ap.CLOSE);
				}
			}

			float getArea() {
				float area = 0;
				int nb = points.size();
				PVector p2 = points.get(nb - 1).pos;
				for (Point p : points) {
					area += p.pos.x * p2.y - p2.x * p.pos.y; // (cross product)
					p2 = p.pos;
				}
				return area;
			}

			PVector getCentroid() {
				PVector centroid = new PVector();
				float area = 0;
				int nb = points.size();
				PVector p2 = points.get(nb - 1).pos;
				for (Point p : points) {
					float a = p.pos.x * p2.y - p2.x * p.pos.y; // (cross
																// product)
					area += a;
					centroid.add(PVector.mult(PVector.add(p.pos, p2), a));
					p2 = p.pos;
				}
				centroid.mult(1 / (3 * area));
				return centroid;
			}

			void flipVertexOrder() {
				ArrayList<Point> old = points;
				points = new ArrayList<Point>();
				for (int i = old.size() - 1; i >= 0; i--)
					points.add(old.get(i));
			}
		}

		static void drawPathForwards(PApplet ap, ArrayList<PVector> pts) {
			for (PVector p : pts)
				ap.vertex(p.x, p.y);
		}

		static void drawPathBackwards(PApplet ap, ArrayList<PVector> pts) {
			for (int i = pts.size() - 1; i >= 0; i--) {
				PVector p = pts.get(i);
				ap.vertex(p.x, p.y);
			}
		}

		ArrayList<Point> pointsList;
		ArrayList<Edge> edgesList;
		ArrayList<Face> facesList;
		Vector<PVector> voronoiPoints; // used before the creation of the graph
		boolean created = false;
		int creationStep = 0;
		int mapSeed;
		float landMassFraction = 0.5f;
		int nbRivers;
		boolean toBeDrawn = false;

		Map(PApplet ap) {
			if(ap != null)
				ap.noiseDetail(8);
		}

		void createPoints(PApplet ap) {
			PoissonDistribution distrib = new PoissonDistribution();
			float d = (float) Math.sqrt(width * height * 0.61f / nbPoints);
			voronoiPoints = distrib.generate(ap, 0, 0, width, height, d, 20);
		}
		void createPointsGrid(PApplet ap)
		{
			
			Vector<PVector> pnts =  new Vector<PVector>();
			int dw = width /nbPoints;
			int dh = height /nbPoints;
			int xoffset = dw,yoffset = dh;
			for(int i = 1 ; i*dw < width - dw ;i++)
			{
				for(int j = 1 ; j*dh< height - dh ;j++)
				{				
					pnts.add(new PVector(xoffset + i*dw,yoffset+j*dh));
				}
				
			}
			System.out.println(pnts);
			voronoiPoints = pnts;
		}
		
		void addToList(ArrayList list, Object obj) {
			if (obj != null && !list.contains(obj))
				list.add(obj);
		}

		Edge getEdge(Point pt1, Point pt2) {
			for (Edge e : pt1.edges) {
				if (e.contains(pt2))
					return e;
			}

			for (Edge e : pt2.edges) {
				if (e.contains(pt1))
					return e;
			}

			Edge e = new Edge();
			e.index = edgesList.size();
			e.p1 = pt1;
			e.p2 = pt2;
			edgesList.add(e);
			pt1.edges.add(e);
			pt1.neighbours.add(pt2);
			pt2.edges.add(e);
			pt2.neighbours.add(pt1);
			return e;
		}

		void createGraph(PApplet ap) {
			pointsList = new ArrayList<Point>();
			edgesList = new ArrayList<Edge>();
			facesList = new ArrayList<Face>();

			Voronoi voronoi = new Voronoi();
			voronoi.setBoundaries(0, 0, width, height);
			voronoi.computeVoronoi(voronoiPoints);
			if (!voronoi.ok) { // Rare bug I have yet to correct
				PApplet.println("Error, stoping creation");
				return;
			}

			HashMap<PVector, Point> pointsLookup = new HashMap<PVector, Point>();
			Random r = new Random(droppingSeed);
			for (Voronoi.Site site : voronoi.sites) 
			{
				if(randomDrops)
				{
					if(r.nextFloat()< 0.2)
						continue;
				}
				if (site.points.size() < 3)
					continue;
				Face f = new Face();
				f.index = facesList.size();
				facesList.add(f);
				f.color = randomColor(ap);// TODO
				for (PVector p : site.points) {
					Point pt = pointsLookup.get(p);
					if (pt == null) {
						pt = new Point(p);
						pointsList.add(pt);
						pointsLookup.put(p, pt);
					}
					addToList(f.points, pt);
					addToList(pt.faces, f);
				}

				if (f.getArea() < 0)
					f.flipVertexOrder();
				f.pos = f.getCentroid();

				int nb = f.points.size();
				for (int j = 0; j < nb; j++) {
					Point pt1 = f.points.get(j);
					Point pt2 = f.points.get((j + 1) % nb);
					Edge e = getEdge(pt1, pt2);
					if (e.f1 == null)
						e.f1 = f;
					else if (e.f2 == null) {
						e.f2 = f;
						f.neighbours.add(e.f1);
						e.f1.neighbours.add(f);
					}
					f.edges.add(e);
				}
			}

			// voronoiPoints = null;
		}

		// TODO : useless with changes in Processing 2.0
		PVector lerpVector(PVector v1, PVector v2, float amt) {
			return new PVector(v1.x + (v2.x - v1.x) * amt, v1.y + (v2.y - v1.y) * amt);
		}

		// Moving close points apart
		void relaxEdges() {
			ArrayList<PVector> tempPos = new ArrayList<PVector>();
			for (int i = 0; i < pointsList.size(); i++) {
				Point pt = pointsList.get(i);
				if (pt.border)
					tempPos.add(pt.pos);
				else {
					PVector p = new PVector(0, 0);
					for (Face f : pt.faces)
						p.add(f.pos);
					p.mult(1.0f / pt.faces.size());
					tempPos.add(p);
				}
			}

			for (int i = 0; i < tempPos.size(); i++)
				pointsList.get(i).pos = lerpVector(pointsList.get(i).pos, tempPos.get(i), relaxEdgesAmount);
		}

		float getNoiseValue(PApplet ap, PVector v) {
			float tx = 2 * v.x / width - 1, ty = 2 * v.y / height - 1;
			float d = tx * tx + ty * ty;
			if (d < 0.5)
				d = 0;
			else
				d = PApplet.pow(d * 2 - 1, 2);
			return ap.noise(tx * noiseScale + 1000, ty * noiseScale + 1000) - 0.5f * d;
		}

		void createIslandForm(PApplet ap) {
			// I compute a noise threshold so that there always is a certain
			// fration of land mass in the map
			ArrayList<Float> noiseValues = new ArrayList<Float>();
			for (Point p : pointsList)
				noiseValues.add(getNoiseValue(ap, p.pos));

			Collections.sort(noiseValues, Collections.reverseOrder());
			int nb = noiseValues.size();
			int thresholdIndex = PApplet.constrain(PApplet.floor(nb * landMassFraction), 0, nb - 1);
			float noiseThreshold = noiseValues.isEmpty() ? 0 : noiseValues.get(thresholdIndex);

			for (Point p : pointsList)
			{
				p.water = (getNoiseValue(ap, p.pos) < noiseThreshold);
			}
			
		}

		void assignWater() {
			Queue<Face> queue = new LinkedList<Face>();
			// Borders
			for (Point p : pointsList) {
				if (p.border) {
					p.water = true;
					for (Face f : p.faces) {
						f.border = true;
						f.ocean = true;
						f.water = true;
						queue.offer(f);
					}
				}
			}

			// Determining water and land
			for (Face f : facesList) {
				if (f.ocean)
					continue;
				int nbWater = 0;
				for (Point p : f.points) {
					if (p.water)
						nbWater++;
				}

				f.water = (nbWater >= f.points.size() * lakeThreshold);
			}

			// Propagation of oceans
			while (queue.peek() != null) {
				Face f = queue.poll();
				for (Face n : f.neighbours) {
					if (n.water && !n.ocean) {
						n.ocean = true;
						queue.offer(n);
					}
				}
			}

			// Coast
			for (Face f : facesList) {
				int nbOcean = 0;
				int nbLand = 0;
				for (Face n : f.neighbours) {
					if (n.ocean)
						nbOcean++;
					if (!n.water)
						nbLand++;
				}
				f.coast = (nbOcean > 0) && (nbLand > 0);
			}

			// Points attributes
			for (Point p : pointsList) {
				int nbOcean = 0;
				int nbLand = 0;
				for (Face f : p.faces) {
					if (f.ocean)
						nbOcean++;
					if (!f.water)
						nbLand++;
				}
				p.ocean = (nbOcean == p.faces.size());
				p.coast = (nbOcean > 0) && (nbLand > 0);
				p.water = p.border || (!p.coast && nbLand < p.faces.size());
			}

			// Edges attributes
			for (Edge e : edgesList) {
				if (e.f2 == null)
					continue;
				e.coast = (e.f1.ocean != e.f2.ocean);
				e.shore = (e.f1.water != e.f2.water);
			}
		}

		void mapGenerationStep(PApplet ap) {
			createPoints(ap);
			
			createGraph(ap);
			relaxEdges();

			createIslandForm(ap);
			assignWater();
			
			if(noisyEdges)
				buildNoisyEdges(ap);
			created = true;

		}

		void fullCreation(PApplet ap) {
			while (!created)
				mapGenerationStep(ap);
		}

		void createMap(PApplet ap, int seed) {
			mapSeed = seed;
			if(ap != null)
			{
				ap.randomSeed(seed);
				ap.noiseSeed(seed);
				landMassFraction = ap.random(landMassFractionMin, landMassFractionMax);
			}
			created = false;
			creationStep = 0;

		}

		void lineStrip(PApplet ap, ArrayList<PVector> pts) {
			ap.beginShape();
			for (PVector p : pts)
				ap.vertex(p.x, p.y);
			ap.endShape();
		}

		synchronized void draw(PApplet ap) {
			ap.smooth();
			ap.background(colorToInt(this.color(0, 0, 0)));

			if (created) {
				ap.background(colorToInt(biomesColor[1]));
				//System.out.println(" map "+pointsList);
				ap.smooth();
				/*
				for (Edge e : edgesList) {
					if(e.strokeWeight > 0.15f)
						System.out.println(e.strokeWeight);

					if (e.f2 == null)
						continue; // border
					ap.strokeWeight(e.strokeWeight);
					//remove ?
					if (e.coast) {
						ap.strokeWeight(3.5f);
						ap.stroke(colorToInt(biomesColor[5])); // coast
					} else if (e.f1.water || e.f2.water) // inside a lake
						continue;
					else if (e.river > 0) {
						ap.strokeWeight(ap.sqrt(e.river));
						ap.stroke(colorToInt(colorRiver));
					} else
						continue;
					//
					if (e.noisy1 != null && e.noisy2 != null) {
						lineStrip(ap, e.noisy1);
						lineStrip(ap, e.noisy2);
					} else
						ap.line(e.p1.pos.x, e.p1.pos.y, e.p2.pos.x, e.p2.pos.y);
				}*/

				for (int i = 0; i < voronoiPoints.size(); i++)
					ap.point(voronoiPoints.get(i).x, voronoiPoints.get(i).y);

				for (int i = 0; i < facesList.size(); i++) 
				{

					ap.fill(colorToInt(facesList.get(i).color));
				//	facesList.get(i).drawSimple(ap);
					facesList.get(i).drawNoisy(ap);

					//polygon(ap, facesList.get(i));
				}

				ap.fill(0);
				ap.stroke(0, 90);
				if(showText)
				{
					ap.textSize(14);
					for (int i = 0; i < facesList.size(); i++) 
					{
						ap.text(i + "", facesList.get(i).pos.x, facesList.get(i).pos.y);
					}
				}				
				ap.stroke(50, 150, 50,120);
				
				//ap.stroke(255, 255, 250,255);
				if(drawBorders)
				{
					for (int i = 0; i < facesList.size(); i++) 
					{
						
						ArrayList<Face> neighbors = facesList.get(i).neighbours;
						for (int j = 0; j < neighbors.size(); j++) {
							ap.line(facesList.get(i).pos.x, facesList.get(i).pos.y, neighbors.get(j).pos.x,
									neighbors.get(j).pos.y);
						}
					}
				}

			}

		}
		private void drawBackup(PApplet ap) {
			ap.smooth();
			ap.background(colorToInt(this.color(0, 0, 0)));

			if (created) {
				ap.background(colorToInt(biomesColor[1]));

				ap.smooth();
				for (Edge e : edgesList) {
					if (e.f2 == null)
						continue; // border
					ap.strokeWeight(0.1f);
					if (e.coast) {
						ap.strokeWeight(3.5f);
						ap.stroke(colorToInt(biomesColor[5])); // coast
					} else if (e.f1.water || e.f2.water) // inside a lake
						continue;
					else if (e.river > 0) {
						ap.strokeWeight(ap.sqrt(e.river));
						ap.stroke(colorToInt(colorRiver));
					} else
						continue;

					if (e.noisy1 != null && e.noisy2 != null) {
						lineStrip(ap, e.noisy1);
						lineStrip(ap, e.noisy2);
					} else
						ap.line(e.p1.pos.x, e.p1.pos.y, e.p2.pos.x, e.p2.pos.y);
				}

				for (int i = 0; i < voronoiPoints.size(); i++)
					ap.point(voronoiPoints.get(i).x, voronoiPoints.get(i).y);

				for (int i = 0; i < facesList.size(); i++) 
				{
					ap.fill(colorToInt(facesList.get(i).color));
					facesList.get(i).drawNoisy(ap);
					//polygon(ap, facesList.get(i));
				}

				ap.fill(0);
				ap.stroke(0, 90);
				if(showText)
				{
					ap.textSize(14);
					for (int i = 0; i < facesList.size(); i++) 
					{
						ap.text(i + "", facesList.get(i).pos.x, facesList.get(i).pos.y);
					}
				}
				ap.stroke(50, 150, 50,45);
				if(drawBorders)
				{
					for (int i = 0; i < facesList.size(); i++) {
	
						ArrayList<Face> neighbors = facesList.get(i).neighbours;
						for (int j = 0; j < neighbors.size(); j++) {
							ap.line(facesList.get(i).pos.x, facesList.get(i).pos.y, neighbors.get(j).pos.x,
									neighbors.get(j).pos.y);
						}
					}
				}

			}

		}
		void polygon(PApplet ap, Face face) {
			ap.fill(colorToInt(face.color));
			ap.beginShape();
			for (int i = 0; i < face.points.size(); i++) {
				ap.vertex(face.points.get(i).pos.x, face.points.get(i).pos.y);
			}
			ap.endShape(ap.CLOSE);
		}
	}


	



	
	
	
}