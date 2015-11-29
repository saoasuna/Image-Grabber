package saoasuna.flickrbooru.android.photograbber;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by Ryan on 09/11/2015.
 */

// generic fragment that hides foreground notifications
public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null); // dynamic broadcast receiver (vs standalone broadcast receiver)
    }   // only the broadcaster holding the string represented by PERM_PRIVATE can send an intent to this receiver
    // in other words, only this app

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "cancelling notification");
            setResultCode(Activity.RESULT_CANCELED); // if we receive this, we're visible (since this is
            //a dynamic broadcast receiver) so adjust the orderedbroadcast to cancel the notification
        }
    };
}
