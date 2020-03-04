package com.example.floodmonitor;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.example.floodmonitor.Constants.CHANNEL_ID;

public class HomePage extends AppCompatActivity {
    public static TextView tV,temp,hum,pres,flood,serreply;
    Button maps,getdata,get,noti;
    Spinner spinner;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private Toolbar toolbar;
    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    private  ActionBarDrawerToggle drawerToggle;
    private int mSelectedId;
    DatabaseReference df;
    private final int NOTI_ID =     1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        df = FirebaseDatabase.getInstance().getReference().child("Current").child("-M1-7CyPXRW1xxLClSC6");
        serreply = findViewById(R.id.serreply);
        flood = findViewById(R.id.flood);
        get = findViewById(R.id.get);

        spinner = findViewById(R.id.spinner);


        tV = findViewById(R.id.textView);
        temp = findViewById(R.id.temp);
        hum= findViewById(R.id.hum);
        pres = findViewById(R.id.pres);
        getdata = findViewById(R.id.getdata);
        maps = findViewById(R.id.buttonMaps);
        noti = findViewById(R.id.noti);
        toolbar = findViewById(R.id.toolBar);


        noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    Intent intent = new Intent(HomePage.this,HeatmapsDemoActivity2.class);
                    startActivity(intent);
                }
            }
        });

        df.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long num = (Long) dataSnapshot.child("Level").getValue();
                Long fs = (Long) dataSnapshot.child("Flood Status").getValue();
                String n = dataSnapshot.child("Flood Status").getValue().toString();
                //String nam = dataSnapshot.child("Name").getValue().toString();
                System.out.println("VAlue is" + n);
                Intent mapInt = new Intent(HomePage.this,HeatmapsDemoActivity2.class);

                flood.setText("FLood Stauts" + n);

                if(num > 4.0) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(HomePage.this, CHANNEL_ID);
                    PendingIntent mapActivityIntent = PendingIntent.getActivity(
                            HomePage.this,  // calling from Activity
                            0,
                            mapInt,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setSmallIcon(R.drawable.ic_launcher_background)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                            .setVibrate(new long[]{100, 200, 300, 400, 500, 600, 700, 800, 900})
                            .setColor(100)
                            .setContentIntent(mapActivityIntent)
                            .setContentTitle("Flood Alert")
                            .setContentText("The Flood Status is "+ fs);
                    NotificationManagerCompat nmc = NotificationManagerCompat.from(HomePage.this);
                    nmc.notify(NOTI_ID, builder.build());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // df = FirebaseDatabase.getInstance().getReference().child("Current").child("-M1--oWe6MiDwIY_AFOc");
                df.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String n = dataSnapshot.child("Level").getValue().toString();
                        System.out.println("Value is" + n);
                        serreply.setText("Water Level " + n);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, Constants.CHANNEL_NAME, importance);
            mChannel.setDescription(Constants.CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }

        findViewById(R.id.buttonSubscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String topic = spinner.getSelectedItem().toString();
                FirebaseMessaging.getInstance().subscribeToTopic(topic);
                Toast.makeText(getApplicationContext(), "Topic Subscribed", Toast.LENGTH_LONG).show();
            }
        });

        final Double lat=13.1155,lon=77.6070;
       /* mSwipeRefreshLayout = findViewById(R.id.swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //String strurl = "https://api.darksky.net/forecast/f56dfbbbd9e86b3715992d25589ebe1b/12.9924,78.1768";
                String strurl1 = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units=metric&appid=70ced719f56db4379844320c96a0484b";
                //String strurl = "https://maps.googleapis.com/maps/api/elevation/json?locations=12.9304,%2077.6784&key=AIzaSyCCiSkMDtJv9pTad1rxhsGWu7YXCmStJOk";
                UrlHandler process = new UrlHandler();
                process.execute(strurl1);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });*/

        //String strurl = "https://api.darksky.net/forecast/f56dfbbbd9e86b3715992d25589ebe1b/12.9924,78.1768";
        String strurl1 = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units=metric&appid=70ced719f56db4379844320c96a0484b";
        //String strurl = "https://maps.googleapis.com/maps/api/elevation/json?locations=12.9304,%2077.6784&key=AIzaSyCCiSkMDtJv9pTad1rxhsGWu7YXCmStJOk";
        UrlHandler process = new UrlHandler();
        process.execute(strurl1);

        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomePage.this, HeatmapsDemoActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1:

                break;
            case R.id.item3:
                HomePage.this.finish();
                break;
            case R.id.item2:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(HomePage.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Toast.makeText(this,"Logged Out",Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
