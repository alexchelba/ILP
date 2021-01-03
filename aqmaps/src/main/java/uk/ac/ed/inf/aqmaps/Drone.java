package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.turf.TurfJoins;

public class Drone extends App {
	private Point p;
	private int label;
	private boolean visited;
	private ArrayList<Drone> neighbors;
	
	Drone(Point p) {
		this.p = p;
		this.label = -2;
		this.visited = false;
		this.neighbors = new ArrayList<Drone>();
	}
	
	Drone(Point p, int label) {
		this.p = p;
		this.label = label;
		this.visited = false;
		this.neighbors = new ArrayList<Drone>();
	}
	
	protected Point getP() {
		return p;
	}
	
	protected int getLabel() {
		return label;
	}
	
	protected void setLabel(int label) {
		this.label = label;
	}
	
	protected boolean getVisited() {
		return visited;
	}
	
	protected void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	protected ArrayList<Drone> getNeighbors() {
		return neighbors;
	}
	
	protected void setNeighbors(ArrayList<Point> path, int angle) {
		// Takes input a list of points, corresponding to the path traversed by the drone
		// and an integer, corresponding to the angle at which the drone traveled last time
		// Sets the neighbor's list, marking as visited the neighbors which are on the path
		// or opposite the drone's direction of traveling,
		// at an angle varying 20 degrees left and right
		
		for(var i=0;i<360;i+=10) {
			
			var rad = i * Math.PI/180;
			var new_lng = p.longitude() + (MOVE_LENGTH*Math.cos(rad));
			var new_lat = p.latitude() + (MOVE_LENGTH*Math.sin(rad));
			var p1 = Point.fromLngLat(new_lng, new_lat);
			
			var neighbor = new Drone(p1);
			
			if(path.contains(p1))
				neighbor.setVisited(true);	
			else
			if( (0<=angle&&angle<=150) && (i>=angle+90+70&&i<=270-70+angle) )
				neighbor.setVisited(true);
			else
			if( (150<angle&&angle<=180) && (i<=angle-90-70||i>=70+90+angle) )
				neighbor.setVisited(true);
			else
			if( (180<angle&&angle<=190) && (i<=angle-90-70 || i>=70+90+angle) )
				neighbor.setVisited(true);
			else
			if((190<angle&&angle<=330) && (i<=angle-90-70 && i>=70+90+angle-360))
				neighbor.setVisited(true);
			else
			if( (330<angle&&angle<360) && (i<=angle-90-70&&i>=angle-270-70) )
				neighbor.setVisited(true);
			
			neighbors.add(neighbor);
		}
	}
	
	
	protected double getDistance(Point y) {
		// Takes input a point
		// Outputs the Euclidean distance of current object's position to that point
		
		var x1 = p.longitude();
		var y1 = p.latitude();
		var x2 = y.longitude();
		var y2 = y.latitude();
		var dist = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
		return dist;
	}
	
	protected Drone getClosestSensor(ArrayList<Sensor> sensors) {
		// Takes input a list of sensors
		// Returns the Drone object of the closest sensor to the current object
		// If all sensors have been visited, returns an object at a location out of the map
		
		var closest_sensor=sensors.get(0);
		var minn = UNIT;
		for(var sensor : sensors) {
			if(!sensor.getVisited()) {
				var num = getDistance(sensor.getP());
				if(num<minn) {
					minn = num;
					closest_sensor = sensor;
				}
			}
		}
		if(minn==UNIT) return new Drone(Point.fromLngLat(-1, -1));
		return new Drone(closest_sensor.getP(), closest_sensor.getLabel());
	}
	
	protected int decide_label(ArrayList<Sensor> sensors, ArrayList<Polygon> nfz) {
		// Takes input a list of sensors and a list of polygons, representing the no-fly zones
		// Returns the label of the current object:
		//      * -1, if the object is outside the confinement area or in a no-fly zone
		//      * label of a sensor, if the object is within the required range of that sensor
		//      * 0, otherwise
		
		if(p.longitude()<MIN_LNG||p.latitude()<MIN_LAT ||
				p.longitude()>MAX_LNG || p.latitude()>MAX_LAT) {
			label=-1;
			return -1;
		}
		for(var poly : nfz) {
			if(TurfJoins.inside(p, poly)) {
				label=-1;
				return label;
			}
		}
		if(label==-2) {
			for(var sensor : sensors) {
				var num = getDistance(sensor.getP());
				if(num < RANGE_WITHIN_SENSOR) {
					label = sensor.getLabel();
					return label;
				}
			}
			label=0;
			return 0;
		}
		return label;
	}
	
	protected boolean clearFlightPath(LineString li, ArrayList<Polygon> nfz) {
		// Takes input a LineString, bounded by the drone and a neighbor
		// and a list of polygons, each representing a no-fly zone
		// Returns true if the LineString does not cross any of the no-fly zones
		
		for(var poly : nfz) {
			var ls = poly.outer();
			for (var i = 0; i <= li.coordinates().size()-2; ++i) {
				for (var j = 0; j <= ls.coordinates().size()-2; ++j) {
					var a1x = li.coordinates().get(i).longitude();
					var a1y = li.coordinates().get(i).latitude();
					var a2x = li.coordinates().get(i+1).longitude();
					var a2y = li.coordinates().get(i+1).latitude();
					var b1x = ls.coordinates().get(j).longitude();
					var b1y = ls.coordinates().get(j).latitude();
					var b2x = ls.coordinates().get(j+1).longitude();
					var b2y = ls.coordinates().get(j+1).latitude();
					var ua_t = (b2x - b1x) * (a1y - b1y) - (b2y - b1y) * (a1x - b1x);
					var ub_t = (a2x - a1x) * (a1y - b1y) - (a2y - a1y) * (a1x - b1x);
					var u_b = (b2y - b1y) * (a2x - a1x) - (b2x - b1x) * (a2y - a1y);
					if (u_b != 0) {
						var ua = ua_t / u_b;
						var ub = ub_t / u_b;
						if (0 <= ua && ua <= 1 && 0 <= ub && ub <= 1) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
}
