package com.sensordatalabeler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class ForegroundOnlySensorLabelerService extends Service {
    public void stopSensorLabeler() {

    }

    public void startSensorlabeler() {

    }

    @Nullable
    @androidx.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
