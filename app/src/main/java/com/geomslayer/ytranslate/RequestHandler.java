package com.geomslayer.ytranslate;

import android.os.AsyncTask;

// this class need for handling translate calls
// when user changes text really fast, translator make only one last request instead of bunch
public class RequestHandler extends AsyncTask<Void, Void, Void> {

    private final static long DELAY = 350;
    private final static long WAIT_TIME = 25;

    volatile private boolean wasQuery = false;

    // maintain count for proper saving in history strategy
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

    // this is real request
    @Override
    protected void onProgressUpdate(Void... values) {
        listener.doRealRequest();
    }

    // user changes text and calls this
    public void doFakeRequest() {
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
        void doRealRequest();
    }

}