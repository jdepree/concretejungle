package org.concretejungle.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.concretejungle.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.queries.GeoHashBoundingBoxQuery;
import ch.hsr.geohash.queries.GeoHashCircleQuery;

public class MapUtils {
    public static final int DEFAULT_MAP_RADIUS = 50000;

    private static final String LOG_TAG = "MapUtils";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String GEOCODE_API_BASE = "https://maps.googleapis.com/maps/api/geocode";
    private static final String DIRECTIONS_API_BASE = "https://maps.googleapis.com/maps/api/directions";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "/details";
    private static final String OUT_JSON = "/json";

    private static final String STATIC_MAP_BASE = "http://maps.googleapis.com/maps/api/staticmap";

    public static final String getStaticMapUrl(LatLng start, LatLng end, LatLng currentPos, String encodedRoute, String pathColor, int pathWidth, int width, int height) {
        String urlEncodedRoute = "";
        try {
            urlEncodedRoute = URLEncoder.encode(encodedRoute, "UTF-8");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Bad URL encoding", e);
        }
        // TODO: add in Roadie location
        String url = STATIC_MAP_BASE
                + "?size=" + width + "x" + height
                + "&markers="
                + "color:green%7Clabel:A%7C" + start.latitude + "," + start.longitude
                + "&markers="
                + "color:red%7Clabel:B%7C" + end.latitude + "," + end.longitude
                + "&path="
                + "weight:" + pathWidth
                + "%7Ccolor:" + pathColor
                + "%7Cenc:" + urlEncodedRoute;
        return url;
    }

    public static final List<String> reverseGeocode(Context context, LatLng point) {
        String geocodingUrl = GEOCODE_API_BASE + OUT_JSON + "?latlng="
                + point.latitude + "," + point.longitude
                + "&key=" + context.getResources().getString(R.string.GOOGLE_API_KEY);

        String jsonResults = getJson(geocodingUrl);
        List<String> resultList = null;
        try {
            JSONObject jsonObj = new JSONObject(jsonResults);
            JSONArray resJsonArray = jsonObj.getJSONArray("results");
            resultList = new ArrayList<String>(resJsonArray.length());
            for (int i = 0; i < resJsonArray.length(); i++) {
                JSONObject obj = resJsonArray.getJSONObject(i);
                resultList.add(obj.getString("formatted_address"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return resultList;
    }

    public static ArrayList<LocationWithId> autocomplete(Context context, String input, LatLng center, int radius) {
        ArrayList<LocationWithId> resultList = null;

        StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
        sb.append("?key=" + context.getResources().getString(R.string.GOOGLE_API_KEY));
        sb.append("&components=country:us");
        if (center != null) {
            sb.append("&location=" + center.latitude + "," + center.longitude);
            sb.append("&radius=" + radius);
        }
        try {
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Bad URL encoding", e);
        }
        String jsonResults = getJson(sb.toString());

        try {
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            resultList = new ArrayList<LocationWithId>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                JSONObject obj = predsJsonArray.getJSONObject(i);
                resultList.add(new LocationWithId(obj.getString("place_id"), obj.getString("description")));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    public static LatLng getPlaceDetails(Context context, String placeId, String placeName) {
        StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_DETAILS + OUT_JSON);
        sb.append("?key=" + context.getResources().getString(R.string.GOOGLE_API_KEY));
        sb.append("&placeid=" + placeId);
        String jsonResults = getJson(sb.toString());

        try {
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONObject locationObj = jsonObj.getJSONObject("result")
                    .getJSONObject("geometry")
                    .getJSONObject("location");
            return new LatLng(locationObj.getDouble("lat"), locationObj.getDouble("lng"));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return null;
    }

    public static List<String> getDirections(LatLng start, LatLng finish) {
        StringBuilder sb = new StringBuilder(DIRECTIONS_API_BASE + OUT_JSON);
        sb.append("?origin=" + start.latitude + "," + start.longitude);
        sb.append("&destination=" + finish.latitude + "," + finish.longitude);
        sb.append("&mode=bicycling");
        String jsonResults = getJson(sb.toString());

        List results = new ArrayList<String>();

        try {
            System.out.println(jsonResults.toString());
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            String status = jsonObj.getString("status");
            if (!status.equals("OK")) {
                return results;
            }

            JSONArray routes = jsonObj.getJSONArray("routes");
            for (int i = 0; i < routes.length(); i++) {
                JSONObject route = routes.getJSONObject(i);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String points = overviewPolyline.getString("points");
                results.add(points);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    private static String getJson(String urlStr) {
        StringBuffer jsonResults = new StringBuffer();
        HttpURLConnection conn = null;

        try {
            URL url = new URL(urlStr.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return jsonResults.toString();
    }

    public static class LocationWithId {
        private String mId;
        private String mDisplayName;

        public LocationWithId(String id, String displayName) {
            mId = id;
            mDisplayName = displayName;
        }

        public String getId() {
            return mId;
        }

        public String getDisplayName() {
            return mDisplayName;
        }

        public String toString() {
            return mDisplayName;
        }
    }

    public static List<String> getGeoHashForBounds(LatLngBounds bounds) {
        GeoHashBoundingBoxQuery query = new GeoHashBoundingBoxQuery(
                new ch.hsr.geohash.BoundingBox(bounds.southwest.latitude, bounds.northeast.latitude,
                        bounds.southwest.longitude, bounds.northeast.longitude));
        List<GeoHash> geoHashes = query.getSearchHashes();
        List<String> geoHashStrs = new ArrayList<String>();
        for (GeoHash geoHash : geoHashes) {
            geoHashStrs.add(geoHashToString(geoHash));
        }
        return geoHashStrs;
    }

    public static String getGeoHashForLocation(LatLng latLng) {
        GeoHashCircleQuery query = new GeoHashCircleQuery(new WGS84Point(latLng.latitude, latLng.longitude), 1);
        List<GeoHash> hashes = query.getSearchHashes();
        return geoHashToString(hashes.get(0));
    }

    private static String geoHashToString(GeoHash hash) {
        int sigBits = hash.significantBits();
        sigBits = (sigBits / 5) * 5;
        long value = hash.longValue();
        GeoHash newHash = GeoHash.fromLongValue(value, sigBits);
        return newHash.toBase32();
    }

}
