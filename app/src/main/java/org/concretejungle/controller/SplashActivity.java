package org.concretejungle.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.concretejungle.R;
import org.concretejungle.model.Tree;
import org.concretejungle.model.TreeType;
import org.concretejungle.model.data.LocationStore;
import org.concretejungle.model.data.TreeStore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SplashActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String IS_APP_STARTED = "appStarted";
    private boolean hasTypes;
    private boolean hasTrees;
    private boolean hasLocation;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        Parse.initialize(this, getString(R.string.PARSE_APP_ID), getString(R.string.PARSE_CLIENT_KEY));
        downloadBackgroundData();

        SharedPreferences prefStore = getSharedPreferences(HomeActivity.PREF_STORE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefStore.edit();
        editor.putBoolean(IS_APP_STARTED, false);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void downloadBackgroundData() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("TreeType")
            .whereGreaterThan("updatedAt", new Date(0))
            .whereEqualTo("tree_private", false);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> treeTypeList, ParseException e) {
                List<TreeType> typeList = new ArrayList<TreeType>();
                if (treeTypeList == null) {
                    e.printStackTrace();
                    return;
                }
                for (ParseObject treeType : treeTypeList) {
                    int typeId = treeType.getInt("tt_id");
                    String typeName = treeType.getString("tt_name");
                    String infoUrl = treeType.getString("tt_url");
                    String marker = treeType.getString("tt_marker");
                    int startDay = treeType.getInt("tt_season_start");
                    int endDay = treeType.getInt("tt_season_end");
                    typeList.add(new TreeType(typeId, typeName, infoUrl, marker, startDay, endDay));
                }
                TreeStore.getInstance().setTreeTypes(typeList);

                synchronized(this) {
                    hasTypes = true;
                    if (hasTrees && hasLocation) {
                        startNextActivity();
                    }
                }
            }
        });

        parseQuery = ParseQuery.getQuery("Tree")
            .whereGreaterThan("updatedAt", new Date(0))
            .setLimit(1000);

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> results, ParseException e) {
                List<Tree> treeList = new ArrayList<Tree>();
                for (ParseObject tree : results) {
                    int treeId = tree.getNumber("tree_id").intValue();
                    int treeType = tree.getNumber("tree_type").intValue();
                    float latitude = tree.getNumber("tree_lat").floatValue();
                    float longitude = tree.getNumber("tree_lng").floatValue();
                    String notes = tree.getString("tree_notes");
                    Date dateEntered = tree.getDate("tree_add_date");
                    int userId = tree.getInt("tree_user_id");
                    boolean isPrivate = tree.getBoolean("tree_private");
                    String hash = tree.getString("tree_hash");

                    Tree newTree = new Tree(treeId, treeType, latitude, longitude,
                            notes, dateEntered, userId, isPrivate, hash);
                    treeList.add(newTree);
                }
                TreeStore.getInstance().setTrees(treeList);

                synchronized(this) {
                    hasTrees = true;
                    if (hasTypes && hasLocation) {
                        startNextActivity();
                    }
                }
            }
        });
    }

    protected void startNextActivity() {
        SharedPreferences preferences = getSharedPreferences(HomeActivity.PREF_STORE_NAME, MODE_PRIVATE);
        if (preferences.getBoolean(IS_APP_STARTED, false) == true) {
            return;
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(IS_APP_STARTED, true);
            editor.commit();
        }
        Intent intent = HomeActivity.newIntent(this);

        if (isLaunchExternal()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        }
        startActivity(intent);
        finish();
    }

    private boolean isLaunchExternal() {
        String action = getIntent().getAction();
        return Intent.ACTION_VIEW.equals(action);
    }

    @Override
    public void onConnected(android.os.Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(com.google.android.gms.common.ConnectionResult connectionResult) {

    }

    public void onLocationChanged(Location location) {
        LocationStore.getInstance().setLastLocation(location);
        synchronized(this) {
            hasLocation = true;
            if (hasTypes && hasTrees) {
                startNextActivity();
            }
        }
    }

}