package playground.manserpa.results;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import playground.manserpa.spatialData.CSVUtils;

/**
 * 
 * Purpose: Spatially locating the A-Taxi activities. Returns a csv with the pick up and drop off locations
 * 
 * Input: shape file, network.xml, events.xml
 * Output: csv pick up and drop off locations (coordinates)
 * 
 * @author manserpa
 * 
 */

public class AVActivities {
	private Geometry include;
	private Geometry exclude;
	private final GeometryFactory factory;
	
	public static void main(String[] args) throws IOException	{
		AVActivities cs = new AVActivities(args[0]);
		
		cs.run(args[1],args[2]);
		
	}
	
	private AVActivities(String shpFile)	{
		this.factory = new GeometryFactory();
		
		readShapeFile(shpFile);
	}
	
	public void readShapeFile(String shpFile) {
		
		Collection<SimpleFeature> features = new ShapeFileReader().readFileAndInitialize(shpFile);
		Collection<Geometry> include = new ArrayList<>();
		Collection<Geometry> exclude = new ArrayList<>();
		
		for(SimpleFeature f: features){
			boolean incl = true;
			Geometry g = null;
			for(Object o: f.getAttributes()){
				if(o instanceof Polygon){
					g = (Geometry) o;
				}else if (o instanceof MultiPolygon){
					g = (Geometry) o;
				}
				else if (o instanceof String){
					incl = Boolean.parseBoolean((String) o);
				}
			}
			if(! (g == null)){
				if(incl){
					include.add(g);
				}else{
					exclude.add(g);
				}
			}
		}
		
		this.include = this.factory.createGeometryCollection(include.toArray(new Geometry[include.size()])).buffer(0);
		this.exclude = this.factory.createGeometryCollection(exclude.toArray(new Geometry[exclude.size()])).buffer(0);
	}
		
	
	public void run(String networkFile, String eventFile)  throws IOException {
		
	
		String csvFile = "ActivitiesAVs.csv";
        FileWriter writer = new FileWriter(csvFile);

        CSVUtils.writeLine(writer, Arrays.asList("type", "x", "y"), ';');
        
		try {
			List<String> nodeList = new ArrayList<>(); 
			HashMap<String, Coordes> node2Coords = new HashMap<String, Coordes>();
			HashMap<String, Coordes> link2Coords = new HashMap<String, Coordes>();
			List<String> linkList = new ArrayList<>(); 
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			 
			DefaultHandler handler = new DefaultHandler()	{
				
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException	{
					
					if(qName.equalsIgnoreCase("node"))	{
						
						if(nodeInServiceArea(Double.parseDouble(attributes.getValue("x")),Double.parseDouble(attributes.getValue("y"))))	{
							
							nodeList.add(attributes.getValue("id"));
							Coordes thisNode = new Coordes(Double.parseDouble(attributes.getValue("x")),Double.parseDouble(attributes.getValue("y")));
							node2Coords.put(attributes.getValue("id"), thisNode);
							
						}
					}
					
					if(qName.equalsIgnoreCase("link"))	{
						
						if(nodeList.contains(attributes.getValue("from")) && nodeList.contains(attributes.getValue("to")))	{
							
							Coordes thisLink = new Coordes((node2Coords.get(attributes.getValue("from")).x
									+ node2Coords.get(attributes.getValue("to")).x)/2, (node2Coords.get(attributes.getValue("from")).y
											+ node2Coords.get(attributes.getValue("to")).y)/2);
							link2Coords.put(attributes.getValue("id"), thisLink);
							linkList.add(attributes.getValue("id"));
						
						}
					}				
				}
			};
			
			DefaultHandler handler2 = new DefaultHandler()	{
				
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException	{
					
					if(qName.equalsIgnoreCase("event"))	{
						
						if(attributes.getValue("type").equals("actstart"))	{
							
							if(attributes.getValue("actType").equals("AVDropoff") || attributes.getValue("actType").equals("AVPickup")) {
								
								if(link2Coords.containsKey(attributes.getValue("link")))	{
									try {
										CSVUtils.writeLine(writer, Arrays.asList(attributes.getValue("actType"), 
												Double.toString(link2Coords.get(attributes.getValue("link")).x), 
												Double.toString(link2Coords.get(attributes.getValue("link")).y)), ';');
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}		
				}
			};
			
			saxParser.parse(networkFile, handler);
			saxParser.parse(eventFile, handler2);
			
			writer.flush();
	        writer.close();
			
		} catch (Exception e)	{
			e.printStackTrace();
		}
			
	}
	
	private boolean nodeInServiceArea(double x, double y) {
		Coordinate coord = new Coordinate(x, y);
		Point p = factory.createPoint(coord);
		if(this.include.contains(p)){
			if(exclude.contains(p)){
				return false;
			}
			return true;
		}
		return false;
	}

}

class Coordes	{
	double x;
	double y;
	
	public Coordes(double x, double y)	{
		this.x = x;
		this.y = y;
	}
}
