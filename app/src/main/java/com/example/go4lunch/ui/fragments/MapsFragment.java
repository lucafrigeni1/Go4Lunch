package com.example.go4lunch.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.go4lunch.R;
import com.example.go4lunch.di.Injections;
import com.example.go4lunch.di.ViewModelFactory;
import com.example.go4lunch.models.Restaurant;
import com.example.go4lunch.ui.activity.RestaurantDetailActivity;
import com.example.go4lunch.viewmodel.RestaurantViewModel;
import com.example.go4lunch.viewmodel.WorkerViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private RestaurantViewModel restaurantViewModel;
    private WorkerViewModel workerViewModel;

    private GoogleMap map;
    private PlacesClient placesClient;
    FusedLocationProviderClient fusedLocationProviderClient;

    private Location location;
    double lat, lng;
    LatLng latLng;

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=48.259386,7.454241&radius=1500&type=restaurant&key=AIzaSyDvQNY3Hoc2titIZ-d0JfZh0w0uupLen2A
    FloatingActionButton floatingActionButton;
    private List<Restaurant> restaurantList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);

        floatingActionButton = view.findViewById(R.id.location_button);

        setViewModel();
        init();

        setFloatingActionButton();

        return view;
    }

    private void setViewModel(){
        ViewModelFactory viewModelFactory = Injections.provideViewModelFactory(this.getContext());
        this.restaurantViewModel = ViewModelProviders.of(this, viewModelFactory).get(RestaurantViewModel.class);
        this.workerViewModel = ViewModelProviders.of(this, viewModelFactory).get(WorkerViewModel.class);
    }

    private void init() {
        Places.initialize(Objects.requireNonNull(this.getActivity()), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this.getActivity());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    private void setFloatingActionButton() {
        floatingActionButton.setOnClickListener(v -> getDeviceLocation());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setMapStyle();

        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    private void setMapStyle(){
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(Objects.requireNonNull(this.getContext()), R.raw.style_maps));
    }

    //ALLOW THE APP TO SEE THE USER LOCATION
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(this.getActivity()),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
    }

    //GET USER LOCATION AND ADAPT CAMERA POSITION
    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(Objects.requireNonNull(this.getActivity()), task -> {
                    if (task.isSuccessful()) {
                        location = task.getResult();
                //workerViewModel.getUserLocation(this.getActivity());
                        if (location != null) {
                            getLocation();
                            getPlaces();
                            getRestaurants();
                            setMarkers();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocation(){
        lat = location.getLatitude();
        lng = location.getLongitude();
        latLng = new LatLng(lat, lng);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    private void getPlaces(){
        restaurantViewModel.getPlaces(lng, lat);
    }

    private void getRestaurants(){
        restaurantViewModel.getRestaurantsList().observe(this, restaurants -> {
            restaurantList = restaurants;
        });
    }

    private void setMarkers(){
        for (int i = 0; i < restaurantList.size(); i++){
            Gson gson = new Gson();
            Log.e("workerList: ",gson.toJson(restaurantList.get(i).getWorkerList()));
            com.example.go4lunch.models.retrofit.Location location =
                    restaurantList.get(i).getLocation();
            LatLng latLng = new LatLng(location.getLat(), location.getLng());

            Bitmap bm;

            if (restaurantList.get(i).getWorkerList().size() == 0){
                bm = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_marker_red);
            }else {
                bm = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_marker_green);
            }

            map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(bm))
                    .title(restaurantList.get(i).getName())
            );
        }

        map.setOnInfoWindowClickListener(marker -> {
            for (Restaurant restaurant : restaurantList){
                if (restaurant.getName().equals(marker.getTitle())){
                    Intent intent = new Intent(this.getContext(), RestaurantDetailActivity.class);
                    intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT, restaurant.getId());
                    this.getContext().startActivity(intent);
                }
            }
        });
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                location = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


}