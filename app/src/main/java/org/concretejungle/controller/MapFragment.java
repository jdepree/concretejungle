package org.concretejungle.controller;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.concretejungle.BaseMapFragment;
import org.concretejungle.R;
import org.concretejungle.SingleFragmentActivity;
import org.concretejungle.model.Tree;
import org.concretejungle.model.TreeType;
import org.concretejungle.model.User;
import org.concretejungle.model.data.LocationStore;
import org.concretejungle.model.data.TreeStore;
import org.concretejungle.util.MapUtils;
import org.concretejungle.view.PlaceSelectorView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.queries.GeoHashBoundingBoxQuery;

public class MapFragment extends BaseMapFragment {

    private MapView mMapView;
    private RelativeLayout mPopupView;
    private TextView mTreeTitleView;
    private ImageView mTreeImageView;
    private PlaceSelectorView mOriginFinderView;
    private PlaceSelectorView mDestinationFinderView;

    private Hashtable<Marker, Tree> mMarkerMap;
    private LatLng mLastViewedLocation;
    private LatLng mOrigin;
    private LatLng mDestination;
    private List<Polyline> mPolylines;

    private TreeStore mStore;

    public static Fragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStore = TreeStore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mPopupView = (RelativeLayout)view.findViewById(R.id.details_view);
        mTreeTitleView = (TextView)view.findViewById(R.id.details_tree_title);
        mTreeImageView = (ImageView)view.findViewById(R.id.details_fruit_image);

        mMapView = (MapView)view.findViewById(R.id.fragment_map_mapview);
        mMapView.onCreate(savedInstanceState);
        setupMapView(savedInstanceState);

        mOriginFinderView = (PlaceSelectorView)view.findViewById(R.id.activity_map_search_origin);
        mOriginFinderView.setCenter(mLastViewedLocation, MapUtils.DEFAULT_MAP_RADIUS);
        mOriginFinderView.setOnLocationSelectedListener(new PlaceSelectorView.LocationSelectedListener() {
            @Override
            public void onLocationSelected(LatLng location) {
                mOrigin = location;
                selectLocation(location, mOriginFinderView);
            }
        });

        mDestinationFinderView = (PlaceSelectorView)view.findViewById(R.id.activity_map_search_destination);
        mDestinationFinderView.setCenter(mLastViewedLocation, MapUtils.DEFAULT_MAP_RADIUS);
        mDestinationFinderView.setOnLocationSelectedListener(new PlaceSelectorView.LocationSelectedListener() {
            @Override
            public void onLocationSelected(LatLng location) {
                mDestination = location;
                selectLocation(location, mDestinationFinderView);
            }
        });
        mDestinationFinderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOriginFinderView.setVisibility(View.VISIBLE);
                mDestinationFinderView.setHint(R.string.search_destination_replacement);
            }
        });

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_map_filter:
                Intent intent = TypeSelectActivity.newIntent(getActivity());
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setupMapView(Bundle savedInstanceState) {
        super.setupMapView(savedInstanceState);
        GoogleMap map = getMap();
        if (map == null) {
            return;
        }
        Location location = LocationStore.getInstance().getLastLocation();
        if (location != null) {
            mLastViewedLocation = new LatLng(location.getLatitude(), location.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastViewedLocation, 14));
        }
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Tree tree = mMarkerMap.get(marker);
                int type = tree.getTypeId();
                String pictureName = mStore.getTreeType(type).getImageName();

                mTreeTitleView.setText(marker.getTitle());
                mTreeImageView.setImageResource(getResources().getIdentifier(pictureName,
                        "drawable", getActivity().getPackageName()));
                System.out.println("SET DRAWABLE: " + pictureName);
                if (mPopupView.getVisibility() == View.GONE) {
                    doAnimation(0, (int) getResources().getDimension(R.dimen.details_popup_height));
                }
                return true;
            }
        });
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
           @Override
           public void onMapClick(LatLng latLng) {
               int finalHeight = mPopupView.getHeight();

               if (mPopupView.getVisibility() == View.VISIBLE) {
                   doAnimation(finalHeight, 0);
               }
               if (mOriginFinderView.getVisibility() == View.VISIBLE) {
                   mOriginFinderView.setVisibility(View.GONE);
               }
           }
        });

        map.setOnCameraChangeListener(mOnCameraChangeListener);
        map.setOnMyLocationChangeListener(mOnMyLocationChangeListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        populateMap();
    }

    private void populateMap() {
        GoogleMap map = getMap();

        map.clear();

        SparseArray<BitmapDescriptor> typeIcons = new SparseArray<BitmapDescriptor>();
        List<TreeType> typeList = mStore.getTreeTypes();
        for (TreeType nextType : typeList) {
            if (!nextType.isDisplayOnMap()) {
                continue;
            }

            String markerSuffix = nextType.getMarkerName();
            if (markerSuffix != null) {
                BitmapDescriptor nextDescriptor = BitmapDescriptorFactory.fromResource(getResources()
                        .getIdentifier("marker_" + nextType.getMarkerName(),
                                "drawable",
                                getActivity().getPackageName()));
                typeIcons.put(nextType.getId(), nextDescriptor);
            }
        }

        mMarkerMap = new Hashtable<Marker, Tree>();
        mPolylines = new ArrayList<Polyline>();

        for (Tree tree : mStore.getTreeList()) {
            int typeId = tree.getTypeId();

            TreeType type = mStore.getTreeType(typeId);
            if (!type.isDisplayOnMap()) {
                continue;
            }

            MarkerOptions options = new MarkerOptions();
            options.icon(typeIcons.get(typeId));
            options.position(new LatLng(tree.getLatitude(), tree.getLongitude()));
            options.title(type.getName());
            Marker newMarker = map.addMarker(options);
            mMarkerMap.put(newMarker, tree);
        }
    }

    @TargetApi(11)
    private ValueAnimator slideAnimator(int start, int end) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mPopupView.getLayoutParams();
                layoutParams.height = value;
                mPopupView.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    @TargetApi(11)
    private void doAnimation(int startHeight, int finalHeight) {
        ValueAnimator animator = slideAnimator(startHeight, finalHeight);

        if (finalHeight < startHeight) {
            if (Build.VERSION.SDK_INT >= 11) {
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mPopupView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
            }
        } else {
            mPopupView.setVisibility(View.VISIBLE);
        }

        animator.start();
    }

   private GoogleMap.OnCameraChangeListener mOnCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(final CameraPosition cameraPosition) {

          mLastViewedLocation = cameraPosition.target;
          /*  LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            GeoHashBoundingBoxQuery boxQuery = new GeoHashBoundingBoxQuery(
                    new ch.hsr.geohash.BoundingBox(bounds.southwest.latitude, bounds.northeast.latitude,
                            bounds.southwest.longitude, bounds.northeast.longitude));
            List<GeoHash> hashes = boxQuery.getSearchHashes();
            for (GeoHash hash : hashes) {
                int sigBits = hash.significantBits();
                sigBits = (sigBits / 5) * 5;
                long value = hash.longValue();
                GeoHash newHash = GeoHash.fromLongValue(value, sigBits);
                String encoded = newHash.toBase32();

                List<Tree> oldList = mHashTreeLists.get(encoded);
                Long timestamp = mHashTimestamps.get(encoded);

                final List<Tree> treeList = oldList == null ? new ArrayList<Tree>() : oldList;
*/
        }
    };

    private GoogleMap.OnMyLocationChangeListener mOnMyLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng newCenter = new LatLng(location.getLatitude(), location.getLongitude());
            mLastViewedLocation = newCenter;
        }
    };

    public void selectLocation(LatLng loc, PlaceSelectorView view) {
        GoogleMap map = getMap();
        if (map == null) {
            return;
        }

        if (mDestination == null) {
            if (mOrigin != null) {
                return;
            } else if (mLastViewedLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastViewedLocation, 15));
            }
        } else {
            if (mOrigin != null) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(mOrigin);
                builder.include(mDestination);
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));

                new DirectionsTask().execute(mOrigin, mDestination);
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(mDestination, 15));
            }
        }

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mDestinationFinderView.getWindowToken(), 0);

        mOriginFinderView.setVisibility(View.GONE);
    }

    @Override
    public MapView getMapView() {
        return mMapView;
    }

    public GoogleMap getMap() {
        return mMapView != null ? mMapView.getMap() : null;
    }

    private class DirectionsTask extends AsyncTask<LatLng, Void, List<String>> {
        @Override
        public List<String> doInBackground(LatLng... locations) {
            LatLng start = locations[0];
            LatLng finish = locations[1];

            return MapUtils.getDirections(start, finish);
        }

        @Override
        public void onPostExecute(List<String> routes) {
            for (String route : routes) {
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(PolyUtil.decode(route));
                mPolylines.add(getMap().addPolyline(polylineOptions));
            }
        }
    }

}
