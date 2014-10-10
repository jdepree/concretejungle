package org.concretejungle;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;
import java.util.Set;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.queries.GeoHashBoundingBoxQuery;

public class MapActivity extends FragmentActivity {

    private GoogleMap mMap;
    private LinearLayout mPopupView;
    private TextView mTreeTitleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parse.initialize(this, "YOUR PARSE KEY", "YOUR PARSE SECRET");

        setContentView(R.layout.activity_map);
        setUpMapIfNeeded();
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view = super.onCreateView(name, context, attrs);

        mPopupView = (LinearLayout)findViewById(R.id.details_view);
        mTreeTitleView = (TextView)findViewById(R.id.details_tree_title);

        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(33.7480, -84.3879), 13));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mTreeTitleView.setText(marker.getTitle());

                if (mPopupView.getVisibility() == View.GONE) {
                    doAnimation(0, (int) getResources().getDimension(R.dimen.details_popup_height));
                }
                return true;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
           @Override
           public void onMapClick(LatLng latLng) {
               int finalHeight = mPopupView.getHeight();

               if (mPopupView.getVisibility() == View.VISIBLE) {
                   doAnimation(finalHeight, 0);
               }
           }
        });

        mMap.setOnCameraChangeListener(mOnCameraChangeListener);
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
        public void onCameraChange(CameraPosition cameraPosition) {
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
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
                ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Tree")
                        .whereStartsWith("tree_hash", encoded)
                        .whereGreaterThan("updatedAt", new Date(System.currentTimeMillis() - 3600 * 24 * 1000));
                parseQuery.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> treeList, ParseException e) {
                        for (ParseObject tree : treeList) {
                            double latitude = tree.getNumber("tree_lat").doubleValue();
                            double longitude = tree.getNumber("tree_lng").doubleValue();
                            String notes = tree.getString("tree_notes");
                            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(notes));

                        }
                    }
                });
            }

        }
    };
}
