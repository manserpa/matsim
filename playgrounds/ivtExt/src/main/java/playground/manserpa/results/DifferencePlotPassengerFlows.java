package playground.manserpa.results;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import playground.manserpa.spatialData.CSVUtils;

public class DifferencePlotPassengerFlows {
	public static void main(String[] args) throws IOException {
		
		HashMap<String, Double> volumeOld = new HashMap<String, Double>();
		HashMap<String, Double> volumeAV = new HashMap<String, Double>();
		
		HashMap<String, Double> volumeDiff = new HashMap<String, Double>();
	    
		String csvFileMeanVolume = "DifferencePassengerFlows.csv";
	    FileWriter writerMeanVolume = new FileWriter(csvFileMeanVolume);
	    
	    CSVUtils.writeLine(writerMeanVolume, Arrays.asList("LinkId", "Volume"), ';');
	    
	    // First Argument must be the smaller number of AVs
	    
        String line = "";
        
	    if(args[0] != null)	{
		     try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
		       	int k = 0;
		        while ((line = br.readLine()) != null) {
		           	String[] stats = line.split(";");
		           	if (k != 0)	{
		           		volumeOld.put(stats[0], Double.parseDouble(stats[1]));
		           	}
		           	k++;
		         }
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	    if(args[1] != null)	{
		     try (BufferedReader br = new BufferedReader(new FileReader(args[1]))) {
		       	int k = 0;
		        while ((line = br.readLine()) != null) {
		           	String[] stats = line.split(";");
		           	if (k != 0)	{
		           		volumeAV.put(stats[0], Double.parseDouble(stats[1]));
		           	}
		           	k++;
		         }
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	    for(String e : volumeOld.keySet())	{
	    	if(volumeAV.containsKey(e))
	    		volumeDiff.put(e, (volumeOld.get(e) - volumeAV.get(e)) / volumeOld.get(e));
	    	else
	    		volumeDiff.put(e, 1.0);
	    }
        /*
	    for(String p : volume.keySet())	{
	    	if(volume.get(p) < 0)	
	    		volume.remove(p);
	    }
		*/
		List<Map.Entry<String, Double>> volumeSorted =
                new LinkedList<Map.Entry<String, Double>>(volumeDiff.entrySet());
		
		Collections.sort(volumeSorted, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
		
		for(Entry<String, Double> linkId: volumeSorted)	{
			if (linkId.getValue() >= 0)	{
				try {
					CSVUtils.writeLine(writerMeanVolume, Arrays.asList(linkId.getKey(), Double.toString(linkId.getValue())), ';');
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else	{
				try {
					CSVUtils.writeLine(writerMeanVolume, Arrays.asList(linkId.getKey(), "0.0"), ';');
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		writerMeanVolume.flush();
		writerMeanVolume.close();
		
	}
}
