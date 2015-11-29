package saoasuna.flickrbooru.android.photograbber;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Ryan on 04/11/2015.
 */
public class ThumbnailDownloader<T> extends HandlerThread { // background thread

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler; // attached to main thread's looper
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler; // attached to main thread's looper
    }

    public void queueThumbnail(T target, String url) { // T is the identifier for the mRequestMap
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        }
        else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget(); // the message we send does not actually include
            // the URL itself; instead we update mRequestMap with a mapping between the request identifier (photohoder)
            // and the url for request
            // sets target field to mRequestHandler
            // messages have 3 main fields: what, obj, and target (handler)
        }
    }

    @Override
    protected void onLooperPrepared() { // called before the looper checks the queue for the first time: good place to create
        // handler implementation, setting up the handler
        // handlers must implement handleMessage to receive messages
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {        // this is how the handler will handle messages
                if(msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target); // get URL from the mapping

            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length); // get bitmap from URL
            Log.i(TAG, "Bitmap created");

            mResponseHandler.post(new Runnable(){   // attach this runnable to the message queue
            // it is a runnable that is executed when the message is handled
                public void run() {
                    if (mRequestMap.get(target) != url) { // necessary because recyclerview recycles views, the target may have changed?
                        return;
                    }
                    mRequestMap.remove(target); // remove it from the mapping
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap); // binds the bitmap
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

}
