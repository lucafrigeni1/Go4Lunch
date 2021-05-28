package com.example.go4lunch.viewmodel;

import com.example.go4lunch.models.Restaurant;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class RestaurantViewModel extends ViewModel {

    private final RestaurantDataRepository restaurantDataSource;

    public RestaurantViewModel(RestaurantDataRepository restaurantDataRepository){
        this.restaurantDataSource = restaurantDataRepository;
    }

    public LiveData<List<Restaurant>> getRestaurantsList(){
        return restaurantDataSource.getAllRestaurants();
    }

    public LiveData<List<Restaurant>> restaurantsToShow(){
        return restaurantDataSource.getRestaurantToShow();
    }

    public LiveData<Restaurant> getRestaurant(String id){
        return restaurantDataSource.getRestaurant(id);
    }

    public void getPlaces(Double longitude, Double latitude){
        restaurantDataSource.getPlaces(longitude,latitude);
    }
}
