package com.example.go4lunch.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.os.Build;

import com.example.go4lunch.R;
import com.example.go4lunch.models.Restaurant;
import com.example.go4lunch.models.Worker;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.WorkerParameters;

import static com.example.go4lunch.Utils.restaurantsCollectionReference;
import static com.example.go4lunch.Utils.workersCollectionReference;

public class Notifications extends androidx.work.Worker {

    public String currentUserId = FirebaseAuth.getInstance().getUid();

    public Notifications(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void launchWorker(Context context){
        Date date = Calendar.getInstance().getTime();
        DateFormat hourFormat = new SimpleDateFormat("HHmm", Locale.FRENCH);
        int currentTime = Integer.parseInt(hourFormat.format(date));

        WorkRequest workRequest;

        int currentTimeInMinute = (currentTime /100 * 60) + (currentTime % 100);
        int triggerTime = 1159;
        int triggerTimeInMinute = (triggerTime /100 * 60) + (triggerTime % 100);

        if (currentTime > triggerTime){
            int minutesInADay = 1440;
            workRequest = new OneTimeWorkRequest.Builder(Notifications.class)
                    .setInitialDelay(triggerTimeInMinute + minutesInADay - currentTimeInMinute, TimeUnit.MINUTES).build();
        } else {
            workRequest = new OneTimeWorkRequest.Builder(Notifications.class)
                    .setInitialDelay(triggerTimeInMinute - currentTimeInMinute, TimeUnit.MINUTES).build();
        }
        WorkManager.getInstance(context).enqueue(workRequest);
    }

    public static void cancelWorker(Context context){
        WorkManager.getInstance(context).cancelAllWork();
    }

    @NonNull
    @Override
    public Result doWork() {
        getCurrentUser(this.getApplicationContext());
        return Result.success();
    }

    public void getCurrentUser(Context context) {
        workersCollectionReference.document(currentUserId).get().addOnCompleteListener(task -> {
            Worker worker = task.getResult().toObject(Worker.class);
            if (Objects.requireNonNull(worker).getRestaurant() != null) {
                getRestaurant(context, worker);
            }
        });
    }

    public void getRestaurant(Context context, Worker worker) {
        restaurantsCollectionReference
                .document(worker.getRestaurant().getId()).get().addOnCompleteListener(task -> {
                    Restaurant restaurant = task.getResult().toObject(Restaurant.class);
                    getInformations(context, Objects.requireNonNull(restaurant), worker);
                });
    }

    public void getInformations(Context context, Restaurant restaurant, Worker currentUser) {
        List<String> participantsList = new ArrayList<>();
        String participants;

        for (Worker worker : restaurant.getWorkerList()) {
            if (!worker.getId().equals(currentUser.getId())) {
                participantsList.add(worker.getName());
            }
        }

        String SEPARATOR = ",";
        StringBuilder stringBuilder = new StringBuilder();

        for (String name : participantsList){
            stringBuilder.append(name);
            stringBuilder.append(SEPARATOR);
        }
        stringBuilder.setLength(stringBuilder.length()-1);
        String names = stringBuilder.toString();

        if (!names.isEmpty()) {
            participants = context.getString(R.string.coworkers, names);
        } else
            participants = context.getString(R.string.nobody);

        setNotification(context, participants, restaurant);
    }

    public void setNotification(Context context, String participants, Restaurant restaurant){
        String channelId = "channel id";

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(restaurant.getName());
        inboxStyle.addLine(restaurant.getAddress());
        inboxStyle.addLine(participants);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_baseline_dinner_dining_24)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(restaurant.getAddress())
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setStyle(inboxStyle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, "notification", NotificationManager.IMPORTANCE_HIGH);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannel);
            notificationManager.notify("GO4LUNCH", 7, notificationBuilder.build());
        }
    }
}
