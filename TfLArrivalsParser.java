package ca.ubc.cs.cpsc210.mindthegap.parsers;

import ca.ubc.cs.cpsc210.mindthegap.model.Arrival;
import ca.ubc.cs.cpsc210.mindthegap.model.Line;
import ca.ubc.cs.cpsc210.mindthegap.model.Station;
import ca.ubc.cs.cpsc210.mindthegap.parsers.exception.TfLArrivalsDataMissingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * A parser for the data returned by the TfL station arrivals query
 */
public class TfLArrivalsParser extends TfLAbstractParser {

    /**
     * Parse arrivals from JSON response produced by TfL query.  All parsed arrivals are
     * added to given station assuming that corresponding JSON object as all of:
     * timeToStation, platformName, lineID and one of destinationName or towards.  If
     * any of the aforementioned elements is missing, the arrival is not added to the station.
     *
     * @param stn             station to which parsed arrivals are to be added
     * @param jsonResponse    the JSON response produced by TfL
     * @throws JSONException  when JSON response does not have expected format
     * @throws TfLArrivalsDataMissingException  when all arrivals are missing at least one of the following:
     * <ul>
     *     <li>timeToStation</li>
     *     <li>platformName</li>
     *     <li>lineId</li>
     *     <li>destinationName and towards</li>
     * </ul>
     */
    public static void parseArrivals(Station stn, String jsonResponse)
            throws JSONException, TfLArrivalsDataMissingException {

        JSONArray arrivalsArray = new JSONArray(jsonResponse);
        int timeToStation;
        String destination;
        String platformName;
        String lineId;
        Arrival arrival;
        int arrivalsAdded = 0;

        for (int i = 0; i < arrivalsArray.length(); i++){
            if (!(arrivalsArray.getJSONObject(i).has("timeToStation")))
                continue;
            else
                timeToStation = arrivalsArray.getJSONObject(i).getInt("timeToStation");

            if (!(arrivalsArray.getJSONObject(i).has("platformName")))
                continue;
            else
                platformName = arrivalsArray.getJSONObject(i).getString("platformName");

            if (!(arrivalsArray.getJSONObject(i).has("lineId")))
                continue;
            else
                lineId = arrivalsArray.getJSONObject(i).getString("lineId");

            if (!(arrivalsArray.getJSONObject(i).has("destinationName")) && !(arrivalsArray.getJSONObject(i).has("towards")))
                continue;
            else{
                if (arrivalsArray.getJSONObject(i).has("destinationName"))
                    destination = TfLAbstractParser.parseName(arrivalsArray.getJSONObject(i).getString("destinationName"));
                else
                    destination = arrivalsArray.getJSONObject(i).getString("towards");
            }

            arrival = new Arrival(timeToStation, destination, platformName);

            for (Line l : stn.getLines()){
                if (l.getId().equals(lineId)){
                    stn.addArrival(l, arrival);
                    arrivalsAdded++;
                }
            }
        }

        if (arrivalsAdded == 0)
            throw new TfLArrivalsDataMissingException();
    }
}
