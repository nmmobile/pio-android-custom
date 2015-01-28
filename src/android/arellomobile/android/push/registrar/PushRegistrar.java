package com.arellomobile.android.push.registrar;

import android.content.Context;

public interface PushRegistrar {
	public void checkDevice(Context context) throws Exception;
	public void registerPW(Context context);
	public void unregisterPW(Context context);
}
