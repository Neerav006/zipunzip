package com.neerav.hmt.zipunzip;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.asksira.loopingviewpager.LoopingViewPager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import com.neerav.hmt.zipunzip.adapter.AutoScrollpagerAdapter;
import com.neerav.hmt.zipunzip.common.RetrofitClient;
import com.neerav.hmt.zipunzip.model.Banner;
import com.neerav.hmt.zipunzip.model.MyBanner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.POST;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private LoopingViewPager loopingViewPager;
    private ArrayList<String> XMENArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        MobileAds.initialize(MainActivity.this, "ca-app-pub-3940256099942544~3347511713");




        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("Home");

        loopingViewPager = findViewById(R.id.vpAds);
        GetBanner getBanner = RetrofitClient.getClient("http://dnote.xyz/advertise/").create(GetBanner.class);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ZipUnZipActivity.class);
                startActivity(i);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        /**
         *   Banner api call
         */

        getBanner.getBanner().enqueue(new Callback<MyBanner>() {
            @Override
            public void onResponse(@NonNull Call<MyBanner> call, @NonNull Response<MyBanner> response) {

                if (response.isSuccessful()) {


                    assert response.body() != null;
                    ArrayList<Banner> bannerArrayList = (ArrayList<Banner>) response.body().getBanner();

                    if (bannerArrayList != null && bannerArrayList.size() > 0) {

                        for (int i = 0; i < bannerArrayList.size(); i++) {
                            XMENArray.add("http://dnote.xyz/advertise/".concat("banner/").concat(bannerArrayList.get(i).getImage()));
                        }


                        final AutoScrollpagerAdapter autoScrollpagerAdapter =
                                new AutoScrollpagerAdapter(MainActivity.this, XMENArray, true);

                        loopingViewPager.setAdapter(autoScrollpagerAdapter);
                    }

                }

            }

            @Override
            public void onFailure(@NonNull Call<MyBanner> call, @NonNull Throwable t) {

            }
        });


        /**
         *  End banner api call
         */
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_about_dev) {
            // Handle the camera action
            startActivity(new Intent(MainActivity.this, AboutDevActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Get Banner from server
     */

    interface GetBanner {

        @POST("banner/bannerlistapi/")
        Call<MyBanner> getBanner();

    }
}
