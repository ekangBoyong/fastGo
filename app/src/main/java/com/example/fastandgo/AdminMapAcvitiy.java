package com.example.fastandgo;
import android.Manifest;
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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static android.view.View.GONE;


public class AdminMapAcvitiy extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5445;
    LatLngInterpolator.Spherical latlang=new LatLngInterpolator.Spherical();
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentLocationMarker;
    private Location currentLocation,currentLocation1;
    private boolean firstTimeFlag = true;
    private double Custlatitude;
    private double Custlongitude;
    private String custId="";
    private double restLat=0;
    private double restLng=0;
    private boolean ctrl=false;
    private Marker CustomerMarker;


    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.currentLocationImageButton && googleMap != null && currentLocation != null)
                AdminMapAcvitiy.this.animateCamera(currentLocation);
        }
    };

    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null)
                return;

            currentLocation1 = locationResult.getLastLocation();
            if(restLat!=0){
                if(restLng!=0){
                    currentLocation = new Location("");
                    currentLocation.setLatitude(restLat);
                    currentLocation.setLongitude(restLng);
                }else{
                    getRestaurantLocation();
                }
            }



            if (firstTimeFlag && googleMap != null) {
                animateCamera(currentLocation);
                firstTimeFlag = false;
            }

            showMarker(currentLocation);
            geoFireUpdate(currentLocation);

        }
    };
    private void getRestaurantLocation(){
        String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference resLocationRef=FirebaseDatabase.getInstance().getReference().child("RestaurantAvailable").child(user_id).child("l");
        resLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map =(List<Object>) dataSnapshot.getValue();
                    double locationLat=0;
                    double locationLng=0;

                    if(map.get(0)!=null){
                        locationLat=Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!=null){
                        locationLng=Double.parseDouble(map.get(1).toString());
                    }
                    restLat=locationLat;
                    restLng=locationLng;
                    Log.d("MyApp","I am here:"+restLat+"hello:"+restLng);




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void geoFireUpdate(@NonNull Location currentLocation){

        String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("RestaurantAvailable");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.setLocation(user_id, new GeoLocation(currentLocation.getLatitude(),currentLocation.getLongitude()));
        DatabaseReference custQueque=FirebaseDatabase.getInstance().getReference("customerRequest");
        DatabaseReference custAccepted=FirebaseDatabase.getInstance().getReference("customerAccepted");

        GeoFire geoFireCustQueque=new GeoFire(custQueque);
        GeoFire geoFireCustAccepted=new GeoFire(custAccepted);
        if(ctrl){
            getAssignedCustomerLocation();

        }

        switch(custId){
            case "":
                if((Custlatitude)!=0) {
                    if((Custlongitude)!=0){
                        geoFireCustAccepted.removeLocation(custId);
                        geoFireCustQueque.setLocation(custId, new GeoLocation(Custlatitude, Custlongitude));
                    }
                }
                break;
            default:
                if((Custlatitude)!=0) {
                    if((Custlongitude)!=0){
                        //geoFireCustQueque.removeLocation(custId);
                        geoFireCustAccepted.setLocation(custId, new GeoLocation(Custlatitude,Custlongitude));
                    }
                }


                break;
        }






    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        supportMapFragment.getMapAsync(this);

        findViewById(R.id.currentLocationImageButton).setOnClickListener(clickListener);
        Button findCust=(Button) findViewById(R.id.findBtn);
        findCust.setVisibility(GONE);

        Button settingCust=(Button) findViewById(R.id.settingBtn);
        settingCust.setVisibility(GONE);

        getAssignedCustomer();
        getRestaurantLocation();
        Log.d("MyApp","I am here11:"+restLat+"hello11:"+restLng);

        //usern = firebaseAuth.getInstance().getCurrentUser().getUid();

    }


    private void setRestaurantLocation(){
        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("RestaurantAvailable");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.setLocation(userId,new GeoLocation(13.976000,120.878998));


    }
    private void getAssignedCustomer(){
        String driverID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Admin").child(driverID).child("customerResId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                        ctrl=true;
                        custId=dataSnapshot.getValue().toString();
                        Log.d("MyApp","CustId="+custId);
                        getAssignedCustomerLocation();

                }else{
                        custId="";
                        if(CustomerMarker !=null){
                            CustomerMarker.remove();
                        }
                        if(assignedCustomerListener !=null){
                            assignedCustomerLocation.removeEventListener(assignedCustomerListener);
                        }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private DatabaseReference assignedCustomerLocation;
    private  ValueEventListener assignedCustomerListener;
    private void getAssignedCustomerLocation(){
         assignedCustomerLocation=FirebaseDatabase.getInstance().getReference().child("Customer").child(custId).child("l");
        assignedCustomerListener=assignedCustomerLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                   List<Object> map=(List<Object>) dataSnapshot.getValue();
                    double locationLat=0;
                    double locationLng=0;
                    if(map.get(0)!=null){
                        locationLat=Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!=null){
                        locationLng=Double.parseDouble(map.get(1).toString());
                    }
                    LatLng restaurantLatlang=new LatLng(locationLat,locationLng);
                    if(CustomerMarker !=null){
                        CustomerMarker.remove();
                    }
                    LatLng latLangCustomer=new LatLng(locationLat,locationLng);
                    if(custId.equals("")){
                        CustomerMarker.remove();
                    }else{
                        CustomerMarker =googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(latLangCustomer).title("Your Restaurant"));

                    }

                    Custlatitude=locationLat;
                    Custlongitude=locationLng;
                    Location loc1=new Location("");
                    loc1.setLatitude(restLat);
                    loc1.setLongitude(restLng);

                    Location loc2=new Location("");
                    loc2.setLatitude(Custlatitude);
                    loc2.setLongitude(Custlongitude);



                    float distance=loc1.distanceTo(loc2);
                    Log.d("MyApp","ayosto:"+distance);



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
                ActivityCompat.requestPermissions(AdminMapAcvitiy.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                return;
            }
        }
        getRestaurantLocation();
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
    protected void onStart() {
        setRestaurantLocation();
        getRestaurantLocation();
        super.onStart();
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
}