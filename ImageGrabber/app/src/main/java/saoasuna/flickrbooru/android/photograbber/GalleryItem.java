package saoasuna.flickrbooru.android.photograbber;

import android.net.Uri;

/**
 * Created by Ryan on 04/11/2015.
 */
public class GalleryItem {
    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    private String mCaption;
    private String mId;
    private String mUrl;

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    public Uri getPhotoPageUri() {
        return Uri.parse("http://www.flickr.com/photos/").buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }

    private String mOwner;

    @Override
    public String toString() {
        return mCaption;
    }
}
