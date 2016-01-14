package saoasuna.flickrbooru.android.photograbber;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;
import android.util.Log;


import java.util.List;

/**
 * Created by Ryan on 08/11/2015.
 */
// poll for new photos uploaded
public class PollService extends IntentService {
    private static final String TAG = "PollService";

    private static final long POLL_INTERVAL = 30*1000; //AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    public static final String ACTION_SHOW_NOTIFICATION = "saoasuna.flickrbooru.android.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "saoasuna.flickrbooru.android.photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public static Intent newIntent(Context context){
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0); // packages up an invocation of Context.startService(intent)

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
        }
        else {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        QueryPreferences.setAlarmOn(context, isOn);
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        // FLAG_NO_CREATE:  if the pendingintent does not already exist, then return null instead of creating it
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) { // view page 467-468 for a good diagram
        if(!isNetworkAvailableAndConnected()) {
            return;
        }
        String query = QueryPreferences.getStoredQuery(this); // get the last searched query
        String lastResultId = QueryPreferences.getLastResultId(this); // id of most recently fetched photo
        List<GalleryItem> items;

        if(query == null) {
            items = new FlickrFetchr().fetchRecentPhotos(); // update items according to whether search or just recent
        }
        else {
            items = new FlickrFetchr().searchPhotos(query);
        }

        if (items.size() == 0) {
            return;
        }

        String resultId = items.get(0).getId();
        if(resultId.equals(lastResultId)) {     // check if we got a new photo or not
            Log.i(TAG, "Got an old result: " + resultId);
        }
        else {
            Log.i(TAG, "Got a new result" + resultId);

            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            // build a new notification with ticker text (displayed in status bar first)
            // an icon to show in the status bar, a view to show in the notification drawer
            // and a pendingintent to fire when the user presses the notification itself
            Notification notification = new NotificationCompat.Builder(this).setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)    // when the user presses the notification it will be removed from the drawer
                    .build();

            /*NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(0, notification); // 0 is the notification id, should be unique for each notification
            //   unless we want the notification to be replaced by another one

            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);*/
            showBackgroundNotification(0, notification);
        }

        QueryPreferences.setLastResultId(this, resultId);   // update the lastResultId
    }
    //if the network is available to the background service, it gets an instance of android.net.NetworkInfo?
    //otherwise the onHandleIntent will retur without executing anything
    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null); //ordered broadcast = twoway communication
        // allow a sequence of broadcast receivers to process a broadcast intent in order, also allow sender of broadcast to receive
        // results from the broadcast's recipients
    }
}
