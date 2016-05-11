package ca.ubc.cs.cpsc210.mindthegap.parsers;

import ca.ubc.cs.cpsc210.mindthegap.model.*;
import ca.ubc.cs.cpsc210.mindthegap.parsers.exception.TfLLineDataMissingException;
import ca.ubc.cs.cpsc210.mindthegap.util.LatLon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A parser for the data returned by TFL line route query
 */
public class TfLLineParser extends TfLAbstractParser {

    /**
     * Parse line from JSON response produced by TfL.  No stations added to line if TfLLineDataMissingException
     * is thrown.
     *
     * @param lmd              line meta-data
     * @return                 line parsed from TfL data
     * @throws JSONException   when JSON data does not have expected format
     * @throws TfLLineDataMissingException when
     * <ul>
     *  <li> JSON data is missing lineName, lineId or stopPointSequences elements </li>
     *  <li> for a given sequence: </li>
     *    <ul>
     *      <li> the stopPoint array is missing </li>
     *      <li> all station elements are missing one of name, lat, lon or stationId elements </li>
     *    </ul>
     * </ul>
     */
    public static Line parseLine(LineResourceData lmd, String jsonResponse)
            throws JSONException, TfLLineDataMissingException {

        // construct JSONObject from given string
        JSONObject lineObject = new JSONObject(jsonResponse);

        //check if lineObject has keys corresponding to line name, id and stopsequences
        if (!lineObject.has("lineId") || !lineObject.has("lineName") || !lineObject.has("stopPointSequences"))
            throw new TfLLineDataMissingException("line id, name or stop sequences key entries missing");

        // parse line id and name fields
        String lineId = lineObject.getString("lineId");
        String lineName = lineObject.getString("lineName");
        // construct line
        Line lineToParse = new Line(lmd, lineId, lineName);
        // parse lineStrings JSONArray to obtain branch information
        JSONArray lineBranches = lineObject.getJSONArray("lineStrings");
        parseBranches(lineToParse, lineBranches);
        // get stop point sequences data from the line
        JSONArray stopSeq = lineObject.getJSONArray("stopPointSequences");
        // add stations to the line
        stationParser(lineToParse,stopSeq);

        return lineToParse;
    }
    
	//helper method to parse branch data
    public static void parseBranches(Line line, JSONArray branchArray) throws JSONException {
        Branch branch;
        for (int i = 0; i < branchArray.length(); i++){
            branch = new Branch(branchArray.getString(i));
            line.addBranch(branch);
        }
    }
	
	////helper method to parse station data
    public static void stationParser(Line line, JSONArray stopSeq) throws JSONException, TfLLineDataMissingException {

        JSONArray stopPointArray;
        JSONObject stopPoint;
        Station station;
        double lat;
        double lon;
        String stationName;
        String stationId;
        int numOfStationsAdded = 0;

        for (int i = 0; i < stopSeq.length(); i++){

            if (!(stopSeq.getJSONObject(i).has("stopPoint")))
                throw new TfLLineDataMissingException("A stop point array is missing from stopPointSequences");

            stopPointArray = stopSeq.getJSONObject(i).getJSONArray("stopPoint");

            for (int j = 0; j < stopPointArray.length(); j++){
                stopPoint = stopPointArray.getJSONObject(j);

                if (!stopPoint.has("lat"))
                    continue;
                else
                    lat = stopPoint.getDouble("lat");

                if (!stopPoint.has("lon"))
                    continue;
                else
                    lon = stopPoint.getDouble("lon");

                if (!stopPoint.has("name"))
                    continue;
                else
                    stationName = TfLAbstractParser.parseName(stopPoint.getString("name"));

                if (!stopPoint.has("stationId"))
                    continue;
                else
                    stationId = stopPoint.getString("stationId");

                if (StationManager.getInstance().getStationWithId(stationId) == null){
                    station = new Station(stationId, stationName, new LatLon(lat,lon));
                }
                else
                    station = StationManager.getInstance().getStationWithId(stationId);

                line.addStation(station);
                StationManager.getInstance().addStationsOnLine(line);
                numOfStationsAdded++;
            }

            if (numOfStationsAdded == 0)
                throw new TfLLineDataMissingException();
            // reset the number before parsing the next stopPoint sequence
            numOfStationsAdded = 0;
        }
    }
}
