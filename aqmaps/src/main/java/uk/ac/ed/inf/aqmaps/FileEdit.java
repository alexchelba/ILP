package uk.ac.ed.inf.aqmaps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mapbox.geojson.FeatureCollection;

public class FileEdit extends App {
	private String filename;
	FileEdit(String filename) {
		this.filename = filename;
	}
	protected void createFile() {
		// takes a string as input and checks if a file with that name exists.
		// if not, it creates one.
		// throws error if it can't create the file
		
		try {
			
			var myObj = new File(filename);
			if (myObj.createNewFile()) {
				
				System.out.println("File created: " + myObj.getName());
			} else {
				
				System.out.println("File already exists.");
			}
		} catch (IOException e) {
			
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	
	protected void writejsonFile(FeatureCollection fc) {
		// writes a geojson-formatted string representation of the FeatureCollection object
		// in the file with name filename.
		// throws error if it can't write in the file
		
		try {
			var myWriter = new FileWriter(filename);
			var gson = new GsonBuilder().setPrettyPrinting().create();
			var js = JsonParser.parseString(fc.toJson()).getAsJsonObject();
			myWriter.write(gson.toJson(js));
			myWriter.close();
			System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	
	protected void writetxtFile(String text) {
		// writes a given string "text" in filename
		// throws error if it can't write in the file
		
		try {
			
			var myWriter = new FileWriter(filename);
			myWriter.write(text);
			myWriter.close();
			System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
}
