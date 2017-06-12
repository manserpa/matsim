package playground.manserpa.results;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import playground.manserpa.spatialData.CSVUtils;

public class VehicleTypeStatistics {
	public static void main(String[] args) throws IOException {
		
		HashMap<String, Integer> vehicleType2Number = new HashMap<String, Integer>();
		HashMap<String, Integer> vehicleType2Pax = new HashMap<String, Integer>();
	      
		ArrayList<Integer> departureTimes = new ArrayList<Integer>();
		HashMap<String, ArrayList<Integer>> vehicleType2Headway = new HashMap<String, ArrayList<Integer>>();
		
	    String csvFileOccupancy = "VehicleTypeStats.csv";
	    FileWriter writerOccupancy = new FileWriter(csvFileOccupancy);
	    
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList("Parameter", "Value"), ';');
	    
        String line = "";
        
	    if(args[0] != null)	{
	        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
	        	int k = 0;
	        	while ((line = br.readLine()) != null) {
		           	String[] stats = line.split("\t");
		           	if (k != 0)	{
		           		if(Integer.parseInt(stats[0]) == 500 && !stats[4].equals("====="))	{
		           			vehicleType2Number.put(stats[6], vehicleType2Number.getOrDefault(stats[6], 0) + Integer.parseInt(stats[7]));
		           			vehicleType2Pax.put(stats[6], vehicleType2Pax.getOrDefault(stats[6], 0) + Integer.parseInt(stats[11]));
		           		}
		           	}
		       	k++;
	            }
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
		try {
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			
			DefaultHandler handler = new DefaultHandler()	{
				
				boolean isParatransit = false;
				String mode;
				
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException	{
					
				    if(qName.equalsIgnoreCase("transitLine"))	{
						String transitLine = attributes.getValue("id");
						if(transitLine.contains("para"))
							isParatransit = true;
						else
							isParatransit = false;
					}
				    
				    if(qName.equalsIgnoreCase("departure") && isParatransit)	{
						String departureTime = attributes.getValue("departureTime");
						String[] times = departureTime.split(":");
						
						int thisDepartureTime = Integer.parseInt(times[0]) * 3600 + Integer.parseInt(times[1]) * 60 + Integer.parseInt(times[2]);
						
						departureTimes.add(thisDepartureTime);
						
						if(attributes.getValue("vehicleRefId").contains("Gelenkbus"))	{
							mode = "Gelenkbus";
						}
						else if(attributes.getValue("vehicleRefId").contains("Minibus"))	{
							mode = "Minibus";
						}
						else if(attributes.getValue("vehicleRefId").contains("Standardbus"))	{
							mode = "Standardbus";
						}
						else	{
							mode = "something else";
						}
				    }	
				}
				
				public void endElement(String uri, String localName, String qName)
			            throws SAXException {
					
					if(qName.equalsIgnoreCase("departures") && departureTimes.size() > 1)	{
						Collections.sort(departureTimes);
						int headway = departureTimes.get(1) - departureTimes.get(0);
						departureTimes.clear();
						
						ArrayList<Integer> departures = new ArrayList<>();
						
						if(vehicleType2Headway.containsKey(mode))	{
							vehicleType2Headway.get(mode).add(headway);	
						} else	{
							vehicleType2Headway.put(mode, departures);
							vehicleType2Headway.get(mode).add(headway);	
						}
					}
			    }
			};
			
			saxParser.parse(args[1], handler);
			
		} catch (Exception e)	{
			e.printStackTrace();
		}
	    
	    int totalVehicles = 0;
	    int totalPax = 0;
	    
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList("Number of vehicles " + "Minibus" + " []", Integer.toString(vehicleType2Number.getOrDefault("Minibus", 0))), ';');
	    totalVehicles += vehicleType2Number.getOrDefault("Minibus", 0);
	    
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList("Number of vehicles " + "Standardbus" + " []", Integer.toString(vehicleType2Number.getOrDefault("Standardbus", 0))), ';');
	    totalVehicles += vehicleType2Number.getOrDefault("Standardbus", 0);
	    
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList("Number of vehicles " + "Gelenkbus" + " []", Integer.toString(vehicleType2Number.getOrDefault("Gelenkbus", 0))), ';');
	    totalVehicles += vehicleType2Number.getOrDefault("Gelenkbus", 0);
	    
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList("Total number of vehicles []", Integer.toString(totalVehicles)), ';');
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList(" ", " "), ';');
	    
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList("Number of Passengers " + "Minibus" + " []", Integer.toString(vehicleType2Pax.getOrDefault("Minibus", 0))), ';');
	    totalPax += vehicleType2Pax.getOrDefault("Minibus", 0);
	    
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList("Number of Passengers " + "Standardbus" + " []", Integer.toString(vehicleType2Pax.getOrDefault("Standardbus", 0))), ';');
	    totalPax += vehicleType2Pax.getOrDefault("Standardbus", 0);
	    
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList("Number of Passengers " + "Gelenkbus" + " []", Integer.toString(vehicleType2Pax.getOrDefault("Gelenkbus", 0))), ';');
	    totalPax += vehicleType2Pax.getOrDefault("Gelenkbus", 0);
	    
	    System.out.println("In total: " + totalPax + " Pax");
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList("Total number of PAX []", Integer.toString(totalPax)), ';');
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList(" ", " "), ';');
	    
	    int totalHeadway = 0;
	    int totHeadway = 0;
	    int totRoutes = 0;
	    
	    totalHeadway = 0;
	    if (vehicleType2Headway.containsKey("Minibus"))	{
		    for ( Integer i : vehicleType2Headway.get("Minibus"))	{
		    	totalHeadway += i;	
		    	totHeadway += i;
		    }
		    
		    totRoutes += vehicleType2Headway.get("Minibus").size();	
		    int numberOfRoutes = vehicleType2Headway.get("Minibus").size();	
	    	double avgHeadway = ((double) totalHeadway) / ((double) numberOfRoutes);
	    	CSVUtils.writeLine(writerOccupancy, Arrays.asList("Avg. headway " + "Minibus" + " [s]: ", Double.toString(avgHeadway)), ';');
	    } 
	    else	{
	    	totalHeadway = 0;	
	    	totHeadway = 0;
	    	
	    	CSVUtils.writeLine(writerOccupancy, Arrays.asList("Avg. headway " + "Minibus" + " [s]: ", "0"), ';');
	    }
		    	
	    totalHeadway = 0;
	    if (vehicleType2Headway.containsKey("Standardbus"))	{
		    for ( Integer i : vehicleType2Headway.get("Standardbus"))	{
		    	totalHeadway += i;	
		    	totHeadway += i;
		    }
		    
		    totRoutes += vehicleType2Headway.get("Standardbus").size();	
		    int numberOfRoutes = vehicleType2Headway.get("Standardbus").size();	
	    	double avgHeadway = ((double) totalHeadway) / ((double) numberOfRoutes);
	    	CSVUtils.writeLine(writerOccupancy, Arrays.asList("Avg. headway " + "Standardbus" + " [s]: ", Double.toString(avgHeadway)), ';');
	    } 
	    else	{
	    	totalHeadway = 0;	
	    	totHeadway = 0;
	    	
	    	CSVUtils.writeLine(writerOccupancy, Arrays.asList("Avg. headway " + "Standardbus" + " [s]: ", "0"), ';');
	    }
	    
	    totalHeadway = 0;
	    if (vehicleType2Headway.containsKey("Gelenkbus"))	{
		    for ( Integer i : vehicleType2Headway.get("Gelenkbus"))	{
		    	totalHeadway += i;	
		    	totHeadway += i;
		    }
		    
		    totRoutes += vehicleType2Headway.get("Gelenkbus").size();	
		    int numberOfRoutes = vehicleType2Headway.get("Gelenkbus").size();	
	    	double avgHeadway = ((double) totalHeadway) / ((double) numberOfRoutes);
	    	CSVUtils.writeLine(writerOccupancy, Arrays.asList("Avg. headway " + "Gelenkbus" + " [s]: ", Double.toString(avgHeadway)), ';');
	    } 
	    else	{
	    	totalHeadway = 0;	
	    	totHeadway = 0;
	    	
	    	CSVUtils.writeLine(writerOccupancy, Arrays.asList("Avg. headway " + "Gelenkbus" + " [s]: ", "0"), ';');
	    }	
    		
    		
	    
	    double avgHeadway2 = ((double) totHeadway) / ((double) totRoutes);
	    System.out.println("Avg. Headway : " + avgHeadway2 + " seconds");	
	    CSVUtils.writeLine(writerOccupancy, Arrays.asList("Avg. headway overall [s]: ", Double.toString(avgHeadway2)), ';');
	    
		writerOccupancy.flush();
		writerOccupancy.close();
	}
}
