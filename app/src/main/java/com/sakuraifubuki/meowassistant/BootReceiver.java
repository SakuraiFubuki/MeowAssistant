package com.sakuraifubuki.meowassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? "" : String.valueOf(intent.getAction());
        if (!"android.intent.action.BOOT_COMPLETED".equals(action)
                && !"android.intent.action.MY_PACKAGE_REPLACED".equals(action)) {
            return;
        }
        try {
            CatConfig cfg = CatConfig.load(context);
            if (cfg.enableAutoStart && cfg.enableKeepAlive) {
                KeepAliveService.start(context);
            }
        } catch (Exception e) {
        }
    }
}
