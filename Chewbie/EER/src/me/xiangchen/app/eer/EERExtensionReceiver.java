package me.xiangchen.app.eer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EERExtensionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		intent.setClass(context, EERExtensionService.class);
		context.startService(intent);
	}
}
