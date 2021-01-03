package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;


public class App 
{
	protected static final int MAX_MOVES = 150;
	protected static final double MOVE_LENGTH = 0.0003;
	protected static final double RANGE_WITHIN_SENSOR = 0.0002;
	protected static final double MIN_LNG = -3.192473;
	protected static final double MAX_LAT = 55.946233;
	protected static final double MAX_LNG = -3.184319;
	protected static final double MIN_LAT = 55.942617;
	protected static final double UNIT = 1000000;

	private static ArrayList<Polygon> getNoFlyZones(String port) {
		// takes input one string, representing the server's port number
		// Outputs a list of polygons, each corresponding to a no-fly zone
		
		var link = "http://localhost:" + port + "/buildings/no-fly-zones.geojson";
		var no_fly = Sensor.parseData(link);
    	var fc = FeatureCollection.fromJson(no_fly);
		var feats = fc.features();
		var polys = new ArrayList<Polygon>();
		for(var feat : feats) {
			var poly = (Polygon) feat.geometry();
			polys.add(poly);
		}
		return polys;
	}
	
	private static ArrayList<Feature> getSensorFeatures(ArrayList<Sensor> sensors) {
		// Takes input one list of sensors
		// Returns a list of features, each feature corresponding to each sensor
		
		var featureList = new ArrayList<Feature>();
		for(var sensor : sensors) {

			var sns = sensor.getSensorFeature();
			featureList.add(sns);
		}
		return featureList;
	}
	
	private static ArrayList<Sensor> getSensors(String port, String date) {
		// Takes input 2 strings: the server's port number and
		// the date for which data is to be collected
		// Returns a list of the sensors found in the file corresponding to the required date
		var link = "http://localhost:"+port+"/maps/"+date+"/air-quality-data.json";
		var quality = Sensor.parseData(link);
    	var listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
    	ArrayList<Sensor> sensors = new Gson().fromJson(quality, listType);
		int k=1;
    	for(var sensor : sensors) {
    		sensor.setSensor(port,k);
			k++;
    	}
    	return sensors;
	}
	
    public static void main( String[] args )
    {
    	// RETRIEVE DATE OF COLLECTION, STARTING POINT'S COORDINATES AND SERVER'S PORT
    	var DD = args[0];
    	var MM = args[1];
    	var YYYY = args[2];
    	var START_LAT = Double.parseDouble(args[3]);
    	var START_LNG = Double.parseDouble(args[4]);
    	var port = args[6];
    	
    	// RETRIEVE DATA FROM SERVER
    	var no_fly_polygons = getNoFlyZones(port);
    	var date = YYYY + "/" + MM + "/" + DD;
    	var sensors = getSensors(port, date);
    	
        // NAVIGATION ALGORITHM   
    	var start_p = new Drone(Point.fromLngLat(START_LNG, START_LAT));
    	var nav = new Navigation(start_p, no_fly_polygons, sensors);
    	nav.main();
    	
    	// PREPARE DATA FOR OUTPUT
    	var featureList = nav.getFeatureList();
    	var sensor_features = getSensorFeatures(sensors);
    	featureList.addAll(sensor_features);
    	var final_fc = FeatureCollection.fromFeatures(featureList);
    	
    	System.out.println(nav.getCount_sensors() + " " +nav.getMade_moves());
    	
    	// CREATE THE REQUIRED FILES AND WRITE TO THEM
    	var flightpath = "flightpath-" + DD + "-" + MM + "-" + YYYY + ".txt";
    	var fp = new FileEdit(flightpath);
    	fp.createFile();
    	fp.writetxtFile(nav.getAns_flightpath());
    	var readings = "readings-" + DD + "-" + MM + "-" + YYYY + ".geojson";
    	var rd = new FileEdit(readings);
    	rd.createFile();
    	rd.writejsonFile(final_fc);
    }
}
