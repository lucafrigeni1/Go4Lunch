package com.example.go4lunch.viewmodel;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import com.example.go4lunch.models.Restaurant;
import com.example.go4lunch.models.Worker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class WorkerDataRepository {

    private final CollectionReference workersCollectionReference =
            FirebaseFirestore.getInstance().collection("Worker");

    private final String currentUserId = FirebaseAuth.getInstance().getUid();

    private final MutableLiveData<List<Worker>> dataWorkerList = new MutableLiveData<>();
    MutableLiveData<Worker> currentUser = new MutableLiveData<>();
    private List<Worker> workerList = new ArrayList<>();

    private final MutableLiveData<LatLng> dataLatlng = new MutableLiveData<>();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public boolean locationPermissionGranted;

    //CREATE
    public void createWorker(Worker worker) {
        workersCollectionReference
                .document(worker.getId())
                .set(worker);
    }

    //READ
    public LiveData<List<Worker>> getWorkersList() {
        workersCollectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Worker worker = document.toObject(Worker.class);
                    workerList.add(worker);
                }
                dataWorkerList.setValue(workerList);
            }
        });
        return dataWorkerList;
    }

    public LiveData<Worker> getCurrentUser() {
        workersCollectionReference.document(currentUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Worker worker = task.getResult().toObject(Worker.class);
                currentUser.setValue(worker);
            }
        });
        return currentUser;
    }

    public FirebaseUser getFirebaseUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    //UPDATE
    public void updateWorkerChoice(String restaurantId) {
        workersCollectionReference
                .document(currentUserId)
                .update("restaurantId", restaurantId);
    }

    public void updateWorkerFavoriteList(List<Restaurant> favoriteRestaurants) {
        workersCollectionReference.document(currentUserId).update("favoriteRestaurant", favoriteRestaurants);
    }
}
