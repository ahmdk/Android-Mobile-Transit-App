package ca.ubc.cs.cpsc210.mindthegap.parsers;


import ca.ubc.cs.cpsc210.mindthegap.util.LatLon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parser for route strings in TfL line data
 */
public class BranchStringParser {

    /**
     * Parse a branch string obtained from TFL
     *
     * @param branch  branch string
     * @return       list of lat/lon points parsed from branch string
     */
    public static List<LatLon> parseBranch(String branch) {
        //list of lat/lon objects that store location data
		//input string similar to [[[0.093493,51.6037],[0.091015,51.5956],[0.088596,51.5857],...]]
		List<LatLon> listOfLocations = new ArrayList<LatLon>();
        String[] listOfStrings;
        double lat = 0;
        double lon = 0;

        if (branch != ""){
            listOfStrings = branch.split("\\],");

            for (int i = 0; i < listOfStrings.length; i++){
                //remove brackets from input Tfl branch string 
				String noBrackets = listOfStrings[i].replaceAll("\\[|\\]", "");
                listOfStrings[i].trim();
				
				//seperate latitude and longitude data for each station
                String[] latLonPointsString =  noBrackets.split(",");
                for (int j = 0; j < latLonPointsString.length; j++){
                    if (j % 2 == 0){
                        //convert string value to a double
						lon = Double.parseDouble(latLonPointsString[j]);
                    }
                    else
                        lat = Double.parseDouble(latLonPointsString[j]);
                }
				
				//create a new LatLon object that contains 				
                LatLon branchPoint = new LatLon(lat, lon);
                listOfLocations.add(branchPoint);
            }

        }

        return listOfLocations;
    }
}
