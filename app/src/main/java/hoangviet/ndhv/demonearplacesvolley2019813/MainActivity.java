package hoangviet.ndhv.demonearplacesvolley2019813;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int REQUEST_CODE_PERMISSION = 123;
    GoogleMap mMap;
    double latitude,longitude;
    private static final String TAG = "MainActivity";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted = false;
    private Marker mMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLocationPermission();



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDiviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }


    }
    private void initMap(){
        SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mMapgg);
        mMapFragment.getMapAsync(this);
    }
    //lấy vị trí hiện tại của máy
    private void getDiviceLocation (){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            final Task location =mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Location curentLocation = (Location) task.getResult();
                        latitude = curentLocation.getLatitude();
                        longitude = curentLocation.getLongitude();
                        LatLng latLng = new LatLng(latitude,longitude);
                        moveCamera(latLng);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.title("current location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                .position(latLng);

                        mMap.addMarker(markerOptions);
                    }
                }
            });

        }catch (SecurityException e){
            Log.d(TAG, "getDiviceLocation: "+e.getMessage());
        }
    }
    private void moveCamera(LatLng latLng){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

    private void getLocationPermission(){
        String [] permission = {FINE_LOCATION,COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            }else {
                ActivityCompat.requestPermissions(this,permission,REQUEST_CODE_PERMISSION);
            }
        }else {
            ActivityCompat.requestPermissions(this,permission,REQUEST_CODE_PERMISSION);
        }
    }
    private void nearbyPlaces(){
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = getUrl(latitude,longitude,"gas_station");
        JsonObjectRequest objectRequest =new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                double lat;
                double lng;
                String vicinity;
                String placeName ;
                MarkerOptions markerOptions = new MarkerOptions();

                try {
                    JSONArray results = response.getJSONArray("results");
                    for (int i = 0 ; i < results.length() ; i++){
                        JSONObject jsonResults = results.getJSONObject(i);
                        JSONObject jsonGeometry = jsonResults.getJSONObject("geometry");
                        JSONObject jsonLocation = jsonGeometry.getJSONObject("location");
                        lat = Double.parseDouble(jsonLocation.getString("lat"));
                        lng = Double.parseDouble(jsonLocation.getString("lng"));
                        placeName = jsonResults.getString("name");
                        vicinity = jsonResults.getString("vicinity");
                        LatLng latLng = new LatLng(lat,lng);
                        markerOptions.position(latLng)
                                .title(placeName)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                        mMarker = mMap.addMarker(markerOptions);
                        moveCamera(latLng);
                        Toast.makeText(MainActivity.this,vicinity, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Lỗi rồi bạn", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(objectRequest);

    }

    private String getUrl(double latitude,double longitude,String placeType) {
        StringBuffer googlePlaceUrl = new StringBuffer("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius=10000");
        googlePlaceUrl.append("&type="+placeType);
        googlePlaceUrl.append("&key="+getResources().getString(R.string.browser));
        Log.d(TAG,googlePlaceUrl.toString());
    return googlePlaceUrl.toString();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION)
        {
            if (grantResults.length > 0){
                for (int i = 0 ; i< grantResults.length;i++){
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        mLocationPermissionGranted = false;
                        return;
                    }
                }
                mLocationPermissionGranted = true;
                initMap();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void findGasStation(View view) {
        nearbyPlaces();
    }
}
