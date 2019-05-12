package com.example.fastandgo;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RestaurantMainControlPanel extends AppCompatActivity implements View.OnClickListener {
    private Switch restaurantAvailable;
    private int customerCount;
    private  LinearLayout restaurantLinearLayout;
    private  String custIdRequest, customerName;
    private Button customerBtnName;
    private ArrayList<String> names=new ArrayList<>();
    private GeoQuery geoQuery;
    int x=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_main_control_panel);
        restaurantAvailable=(Switch) findViewById(R.id.restaurantAvailable);
        restaurantLinearLayout = (LinearLayout) findViewById(R.id.restaurantLinearLayout);
        restaurantAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(isChecked){
                    Log.d("MyApp","ID=Ischecked");
                    getCustomerRequestCount();
                    Iterator<String> it = names.iterator();
                    while (it.hasNext()) {
                        Log.d("MyApp","ID="+it.next());
                    }


                    setRestaurantLocation();


                }else{
                    Log.d("MyApp","ID=!Ischecked");
                    restaurantLinearLayout.removeAllViews();

                }
            }
        });


    }
    private void getAssignedCustomer(){

        String driverID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Admin").child(driverID).child("customerResId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){


                    custIdRequest=dataSnapshot.getValue().toString();
                    Log.d("MyApp","CustId="+custIdRequest);
                    getAssignedCustomerLocation();


                }else{
                    custIdRequest="";
                    Log.d("MyApp","CustId="+custIdRequest);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void setRestaurantLocation(){

        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("RestaurantAvailable");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.setLocation(userId,new GeoLocation(13.976000,120.878998));


    }

    private void getCustomerRequestCount(){

        getAssignedCustomer();
        if(custIdRequest==null){
            getAssignedCustomer();
            Log.d("MyApp", "custId is Empy=");
        }else {
            DatabaseReference customerRequestCount = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(custIdRequest).child("name");
            customerRequestCount.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.exists()) {
                        Log.d("MyApp", "ID  TAKEN=");

                        String value = dataSnapshot.getValue().toString();
                        names.add(value);
                    } else {
                        Log.d("MyApp", "ID NOT TAKEN=");
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
       /** customerRequestCount.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){


                    customerCount=(int) dataSnapshot.getChildrenCount();

                    //List<Object> map=(List<Object>) dataSnapshot.getValue();



                    if(customerCount!=0){

                        DatabaseReference customerRequestCount= FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(custIdRequest);
                        customerRequestCount.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()&& dataSnapshot.getChildrenCount()>0){
                                    Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();
                                    if(map.get("name")!=null){
                                        x++;

                                        customerName=map.get("name").toString();
                                        Log.d("MyApp","Name ="+customerName);
                                        Log.d("MyApp","Loop ="+x);


                                    }else{
                                        Log.d("MyApp","Name Noadata");
                                    }

                                }
                                else{
                                    Log.d("MyApp","Noadata");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }

                    });
                        for(int x=0;x<customerCount;x++){

                           customerBtnName = new Button(RestaurantMainControlPanel.this);
                            customerBtnName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            customerBtnName.setText(customerName);
                            customerBtnName.setTextSize(20);
                            customerBtnName.setTypeface(customerBtnName.getTypeface(), Typeface.BOLD);
                            customerBtnName.setId(x+1);
                            customerBtnName.setTag(x);
                            customerBtnName.setOnClickListener(RestaurantMainControlPanel.this);

                            //add button to the layout
                            restaurantLinearLayout.addView(customerBtnName);
                        }




                    }

                }else{
                    TextView noAvailableCust = new TextView(RestaurantMainControlPanel.this);
                    noAvailableCust.setText("No Customer Available");

                    noAvailableCust.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));



                    restaurantLinearLayout.addView(noAvailableCust);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });**/
    }

    @Override
    public void onClick(View v) {
        String tag=v.getTag().toString();
        if(tag.equals("0")){
            Intent i = new Intent(this, AdminMapAcvitiy.class);

            startActivity(i);

            return;
        }
    }
    private DatabaseReference assignedCustomerLocation;
    private  ValueEventListener assignedCustomerListener;
    private void getAssignedCustomerLocation(){
        assignedCustomerLocation=FirebaseDatabase.getInstance().getReference().child("Customer").child(custIdRequest).child("l");
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





                    Location loc1=new Location("");
                    loc1.setLatitude(locationLat);
                    loc1.setLongitude(locationLng);

                    Location loc2=new Location("");
                    loc2.setLatitude(13.976000);
                    loc2.setLongitude(120.878998);



                    float distance=loc1.distanceTo(loc2)/1000;

                    if(distance<10){
                        //customerBtnName.setBackgroundResource(R.color.Red);
                        if(distance<4){
                            //customerBtnName.setBackgroundResource(R.color.Yellow);
                        }
                        if(distance<2){
                           // customerBtnName.setBackgroundResource(R.color.Green);
                        }
                    }
                    Log.d("MyApp","ayosto:"+distance);



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
