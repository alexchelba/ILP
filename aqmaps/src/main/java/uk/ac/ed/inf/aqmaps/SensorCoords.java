package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class SensorCoords extends App {
	private Coordinates coordinates;
	
	private static class Coordinates {
		private double lng, lat;
	}
	
	protected Point getCoordinates() {
		return Point.fromLngLat(coordinates.lng, coordinates.lat);
	}
}
