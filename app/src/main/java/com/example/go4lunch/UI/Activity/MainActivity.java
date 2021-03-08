package com.example.go4lunch.UI.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.go4lunch.R;
import com.example.go4lunch.UI.Fragments.MapsFragment;
import com.example.go4lunch.UI.Fragments.RestaurantListFragment;
import com.example.go4lunch.UI.Fragments.WorkerListFragment;
import com.example.go4lunch.Utils.BaseActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import static com.example.go4lunch.R.color.white;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SIGN_OUT_TASK = 10;

    FrameLayout frameLayout;
    MapsFragment mapsFragment;
    RestaurantListFragment restaurantListFragment;
    WorkerListFragment workerListFragment;

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    BottomNavigationView bottomNavigationView;

    ImageView headerPicture;
    TextView headerName;
    TextView headerMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();
        pageSelected();
        configureToolBar();
        setHeader();
    }

    public void findViewById(){
        bottomNavigationView = findViewById(R.id.bottom_menu);
        frameLayout = findViewById(R.id.fragment_container);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.drawer_menu);
        toolbar = findViewById(R.id.top_toolbar);
    }

    //TOP BAR
    private void configureToolBar(){
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
                );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(white));

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //DRAWER MENU
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.your_lunch:
                break;
            case R.id.settings:
                break;
            case R.id.logout:
                signOutUserFromFirebase();
                break;
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setHeader(){
        View header = navigationView.inflateHeaderView(R.layout.drawer_menu_header);
        headerPicture = header.findViewById(R.id.profil_image);
        headerName = header.findViewById(R.id.worker_name);
        headerMail = header.findViewById(R.id.worker_mail);

        if (this.getCurrentUser() != null){
            if (this.getCurrentUser().getPhotoUrl() != null){
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(headerPicture);
            } else
                Glide.with(this)
                        .load(R.drawable.ic_baseline_person_24)
                        .apply(RequestOptions.circleCropTransform())
                        .into(headerPicture);

            headerName.setText(this.getCurrentUser().getDisplayName());
            headerMail.setText(this.getCurrentUser().getEmail());
        }
    }

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return aVoid -> {
            if (origin == SIGN_OUT_TASK) {
                startAuthenticationActivity();
            }
        };
    }

    private void startAuthenticationActivity() {
        finish();
        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivity(intent);
    }

    //BOTTOM MENU
    @SuppressLint("NonConstantResourceId")
    private void pageSelected(){
        initFragment();
        setFragment(mapsFragment);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.map_page:
                    toolbar.setTitle(getString(R.string.toolbar_text_1));
                    setFragment(mapsFragment);
                    break;
                case R.id.restaurant_page:
                    toolbar.setTitle(getString(R.string.toolbar_text_1));
                    setFragment(restaurantListFragment);
                    break;
                case R.id.worker_page:
                    toolbar.setTitle(getString(R.string.toolbar_text_2));
                    setFragment(workerListFragment);
                    break;
            }
            return true;
        });
    }

    private void initFragment() {
        mapsFragment = new MapsFragment();
        restaurantListFragment = new RestaurantListFragment();
        workerListFragment = new WorkerListFragment();
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
    }

}
