package com.example.fastandgo;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener , RoutingListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5445;
    LatLngInterpolator.Spherical latlang=new LatLngInterpolator.Spherical();
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentLocationMarker;
    private Location currentLocation;
    private boolean firstTimeFlag = true;
    private double lat;
    private double longi;
    private double restaurantLat, restaurantLng;
    private Button findBtn1, settingBtn;
    private LatLng latLang;
    private Marker restaurantMarker;
    private int ctr;
    private boolean ctr1=false;
    private boolean RequestBol=false;
    private Marker PickUpMarker;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    private String nameStr,phoneStr;
    private  DatabaseReference CustomerRequesref;


    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.currentLocationImageButton && googleMap != null && currentLocation != null)
                CustomerMapActivity.this.animateCamera(currentLocation);
        }
    };

    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null)
                return;
            currentLocation = locationResult.getLastLocation();
            if (firstTimeFlag && googleMap != null) {
                animateCamera(currentLocation);
                firstTimeFlag = false;
            }

            showMarker(currentLocation);
            geoFireUpdate(currentLocation);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        supportMapFragment.getMapAsync(this);
        findViewById(R.id.currentLocationImageButton).setOnClickListener(clickListener);
        findBtn1=(Button) findViewById(R.id.findBtn);
        settingBtn=(Button) findViewById(R.id.settingBtn);

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(CustomerMapActivity.this, CustomerSetting.class);
                startActivity(i);
                return;

            }
        });
        polylines=new ArrayList<>();
        findBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(RequestBol){
                    geoQuery.removeAllListeners();
                    resLocationRef.removeEventListener(RestaurantLocationListener);
                    RequestBol=false;
                    cusLocationRef.removeEventListener(CusLocationListener);

                    if(restaurantFoundId!=null){
                        DatabaseReference restRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Admin").child(restaurantFoundId);
                        restRef.setValue(true);
                        restaurantFoundId=null;
                    }
                    restaurantFound=false;
                    radius=1;
                    String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("customerRequest");
                    //ref.removeValue();
                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.removeLocation(userId);

                    if(PickUpMarker!=null){
                        PickUpMarker.remove();
                    }
                    erasePolyLine();
                    findBtn1.setText("Find Restaurant");
                    findBtn1.setClickable(true);
                    ctr1=false;


                }else{
                    RequestBol=true;
                    String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    //DatabaseReference customerDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(userId);
                    CustomerRequesref=FirebaseDatabase.getInstance().getReference("customerRequest");
                    //HashMap map=new HashMap();
                    //map.put("customerResId",userId);
                    //CustomerRequesref.updateChildren(map);
                    GeoFire geoFire=new GeoFire(CustomerRequesref);
                    geoFire.setLocation(userId,new GeoLocation(lat,longi));
                    latLang=new LatLng(	lat,longi);
                    PickUpMarker=googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(latLang).title("DITO"));
                    findBtn1.setText("Wait for the Restaurant Response");
                    findBtn1.setClickable(false);

                    getClosestRestaurant();
                    //saveUserInfromation();
                    ctr1=true;
                }

            }
        });
    }
    private void saveUserInfromation(){
        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(userId);
        customerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&& dataSnapshot.getChildrenCount()<0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        nameStr = map.get("name").toString();
                        Log.d("MyApp","Name: "+nameStr);

                    }else{
                        Log.d("MyApp","Means Empty");
                    }
                    if (map.get("phone") != null) {
                        nameStr = map.get("phone").toString();


                    }else{
                        Log.d("MyApp","Means Empty");
                    }
                }else{
                    Log.d("MyApp","No data");
                }
                Map userinfo=new HashMap();
                userinfo.put("name",nameStr);
                userinfo.put("phone",phoneStr);
                CustomerRequesref.updateChildren(userinfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });





    }
    private int radius=1;
    private boolean restaurantFound=false;
    private String restaurantFoundId;
    GeoQuery geoQuery;


    private void getClosestRestaurant(){
        DatabaseReference restaurantLocation=FirebaseDatabase.getInstance().getReference().child("RestaurantAvailable");
        GeoFire geoFire=new GeoFire(restaurantLocation);
        geoQuery=geoFire.queryAtLocation(new GeoLocation(latLang.latitude,latLang.longitude),radius );
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!restaurantFound && RequestBol){
                    restaurantFound=true;
                    restaurantFoundId=key;
                    DatabaseReference restRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Admin").child(restaurantFoundId);
                    String customerId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map=new HashMap();
                    map.put("customerResId",customerId);
                    restRef.updateChildren(map);
                    Log.d("MyApp","THIS IS THEREAD"+restaurantFoundId);
                    getRestaurantLocation();
                    findBtn1.setText("Setting Location");

                }else{
                    RequestBol=false;
                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!restaurantFound){
                    radius++;
                    getClosestRestaurant();
                    Log.d("MyApp","here");           }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }
    private DatabaseReference resLocationRef;
    private ValueEventListener RestaurantLocationListener;
    private void getRestaurantLocation(){
        if(restaurantFoundId.equals("")){

        }else{
            resLocationRef=FirebaseDatabase.getInstance().getReference().child("RestaurantAvailable").child(restaurantFoundId).child("l");
            RestaurantLocationListener=resLocationRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && RequestBol){
                        List<Object> map =(List<Object>) dataSnapshot.getValue();

                        findBtn1.setText("Restaurant Found");
                        if(map.get(0)!=null){
                            restaurantLat=Double.parseDouble(map.get(0).toString());
                        }
                        if(map.get(1)!=null){
                            restaurantLng=Double.parseDouble(map.get(1).toString());
                        }

                        Location loc1=new Location("");
                        loc1.setLatitude(restaurantLat);
                        loc1.setLongitude(restaurantLng);

                        Location loc2=new Location("");
                        loc2.setLatitude(currentLocation.getLatitude());
                        loc2.setLongitude(currentLocation.getLongitude());
                        ctr++;


                        float distance=loc1.distanceTo(loc2)/1000;
                        getCustomerLocation();

                        if(distance<=0.0001){
                            findBtn1.setText("You arrive at your destination");
                        }else{
                            findBtn1.setClickable(true);
                            findBtn1.setText("Distance from the Location: "+String.valueOf(distance));
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }
    private DatabaseReference cusLocationRef;
    private ValueEventListener CusLocationListener;
    private void getCustomerLocation(){
            String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();

        cusLocationRef=FirebaseDatabase.getInstance().getReference().child("Customer").child(userId).child("l");
        CusLocationListener=cusLocationRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && RequestBol){
                        if(restaurantLat!=0 && restaurantLng!=0){

                            if(PickUpMarker!=null){
                                PickUpMarker.remove();
                            }
                            LatLng latLangRestaurant=new LatLng(restaurantLat,restaurantLng);

                            PickUpMarker=googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(latLangRestaurant).title("Your Restaurant"));

                            Location loc1=new Location("");
                            loc1.setLatitude(restaurantLat);
                            loc1.setLongitude(restaurantLng);

                            Location loc2=new Location("");
                            loc2.setLatitude(currentLocation.getLatitude());
                            loc2.setLongitude(currentLocation.getLongitude());
                            ctr++;

                            //getRouteToMarker(latLangRestaurant);
                            float distance=loc1.distanceTo(loc2)/1000;


                            if(distance<=0.0001){
                                findBtn1.setText("You arrive at your destination");
                            }else{
                                findBtn1.setClickable(true);
                                findBtn1.setText("Distance from the Location: "+String.valueOf(distance));
                            }

                        }else{
                            Log.d("MyApp","Customer Lat and Long not Available");
                        }
                        }

                }



            @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



    }
    private void getRouteToMarker(LatLng latLangRestaurant) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),latLangRestaurant)
                .key(getString(R.string.google_maps_key))
                .build();
        routing.execute();


    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }


    private void startCurrentLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CustomerMapActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                return;
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

    }


    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status)
            return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(this, "Please Install google play services to use this application", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, "Permission denied by uses", Toast.LENGTH_SHORT).show();
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startCurrentLocationUpdates();


        }
    }

    private void animateCamera(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
    }


    private void geoFireUpdate(@NonNull Location currentLocation){
        this.lat = currentLocation.getLatitude();
        this.longi = currentLocation.getLongitude();


        String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("MyApp","I am here"+ctr);
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Customer");
        GeoFire geoFire=new GeoFire(ref);

        geoFire.setLocation(user_id, new GeoLocation(lat,longi));
        if(ctr1){
            getCustomerLocation();
        }
        /**if(ctr1){
            if(RequestBol) {
                getRestaurantLocation();
                Log.d("MyApp","Called RestaurantFound");
            }
        }**/
    }
    @NonNull
    private CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(15).build();
    }

    private void showMarker(@NonNull Location currentLocation) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (currentLocationMarker == null)
            currentLocationMarker = googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(latLng));
        else

            MarkerAnimation.animateMarkerToGB(currentLocationMarker, latLng,latlang );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            startCurrentLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient = null;
        googleMap = null;
    }


    @Override
    public void onLocationChanged(Location location) {
        /**latitude = location.getLatitude();
        longitude = location.getLongitude();

        String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("MyApp","I am here");
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerAvailable");
        GeoFire geoFire=new GeoFire(ref);

        geoFire.setLocation(user_id, new GeoLocation(latitude,longitude));
        **/
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = googleMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }



    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolyLine(){
        for(Polyline line:polylines){
            line.remove();
        }
        polylines.clear();
    }






}