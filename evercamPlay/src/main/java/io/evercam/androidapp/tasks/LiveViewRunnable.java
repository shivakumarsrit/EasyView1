package io.evercam.androidapp.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import org.phoenixframework.channels.Channel;
import org.phoenixframework.channels.Envelope;
import org.phoenixframework.channels.IMessageCallback;
import org.phoenixframework.channels.ISocketCloseCallback;
import org.phoenixframework.channels.Socket;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.evercam.androidapp.video.VideoActivity;

public class LiveViewRunnable implements Runnable {

    private final String TAG = "LiveViewRunnable";
    private final String HOST = "wss://media.evercam.io/socket/websocket";
    private final String ENVELOPE_KEY_TIMESTAMP = "timestamp";
    private final String ENVELOPE_KEY_IMAGE = "image";
    private final String EVENT_SNAPSHOT_TAKEN = "snapshot-taken";

    private Socket mSocket;
    private Channel mChannel;
    private String mCameraId;

    //Check if it's the first image so that the progress bar should be hidden
    private boolean isFirstImage = true;

    private final Handler mHandler;
    private WeakReference<VideoActivity> mVideoActivityReference;

    public LiveViewRunnable(VideoActivity videoActivity, String cameraId) {
        mCameraId = cameraId;
        mHandler = new Handler(Looper.getMainLooper());
        mVideoActivityReference = new WeakReference<>(videoActivity);
    }

    @Override
    public void run() {
        connectWebsocket();
    }

    private VideoActivity getActivity() {
        return mVideoActivityReference.get();
    }

    private void connectWebsocket() {
        try {
            mSocket = new Socket(HOST);
            mSocket.connect();

            mSocket.onClose(new ISocketCloseCallback() {
                @Override
                public void onClose() {
                    Log.e(TAG, "socket:onClose");
                }
            });

            mChannel = mSocket.chan("cameras:" + mCameraId, null);

            mChannel.join()
                    .receive("ignore", new IMessageCallback() {
                        @Override
                        public void onMessage(Envelope envelope) {
                            Log.d(TAG, "receive:ignore " + envelope.toString());
                        }
                    })
                    .receive("ok", new IMessageCallback() {
                        @Override
                        public void onMessage(Envelope envelope) {
                            Log.d(TAG, "receive:ok " + envelope.toString());
                        }
                    });

            mChannel.on(EVENT_SNAPSHOT_TAKEN, new IMessageCallback() {
                @Override
                public void onMessage(Envelope envelope) {

                    if(isFirstImage)
                    {
                        isFirstImage = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(getActivity() != null) {
                                    getActivity().onFirstJpgLoaded();
                                }
                            }
                        });
                    }
                    Log.d(TAG, "Timestamp: " + envelope.getPayload().get(ENVELOPE_KEY_TIMESTAMP).toString());

                    String base64String = envelope.getPayload().get(ENVELOPE_KEY_IMAGE).toString();
                    //Log.d(TAG, "Data: " + base64String);

                    byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(getActivity() != null) {
                                getActivity().updateImage(bitmap, mCameraId);
                            }
                        }
                    });
                }
            });

            mChannel.onClose(new IMessageCallback() {
                @Override
                public void onMessage(Envelope envelope) {
                    Log.d(TAG, "Channel Closed");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        isFirstImage = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket.remove(mChannel);
                    mChannel = null;
                    mSocket.disconnect();
                    mSocket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }
}
