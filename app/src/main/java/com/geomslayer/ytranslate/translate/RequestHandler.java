package com.geomslayer.ytranslate.translate;

import android.os.AsyncTask;
import android.util.Log;

public class RequestHandler extends AsyncTask<Void, Void, Void> {

    private final static long DELAY = 350;
    private final static long WAIT_TIME = 25;

    volatile private boolean wasQuery = false;
    volatile private int requestsCount = 0;
    volatile private long passedTime = 0;

    final private OnRequestReadyListener listener;

    public RequestHandler(OnRequestReadyListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while (true) {
            if (isCancelled()) {
                break;
            }

            if (wasQuery && passedTime <= 0) {
                wasQuery = false;
                publishProgress();
            }
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                // fine
            }
            passedTime -= WAIT_TIME;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        listener.getTranslation();
    }

    public void requestTranslation() {
        if (this.getStatus() != AsyncTask.Status.RUNNING) {
            this.execute();
        }
        this.passedTime = DELAY;
        if (!wasQuery) {
            ++requestsCount;
        }
        wasQuery = true;
    }

    public int getRequestsCount() {
        return requestsCount;
    }

    public void onCompleteRequest() {
        requestsCount = Math.max(0, requestsCount - 1);
    }

    public interface OnRequestReadyListener {
        void getTranslation();
    }

}