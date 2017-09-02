package com.project.denail.stockhawk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.project.denail.stockhawk.listener.VoidListener;

/**
 * Created by denail on 17/09/01.
 */

public class NetworkReceiver extends BroadcastReceiver {

    private VoidListener listener;

    public NetworkReceiver(VoidListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        listener.onAction();
    }
}
