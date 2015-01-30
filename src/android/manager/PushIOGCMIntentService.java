/*     */ package com.pushio.manager;
/*     */ 
/*     */ import android.app.AlarmManager;
/*     */ import android.app.IntentService;
/*     */ import android.app.Notification;
/*     */ import android.app.NotificationManager;
/*     */ import android.app.PendingIntent;
/*     */ import android.content.BroadcastReceiver;
/*     */ import android.content.Context;
/*     */ import android.content.Intent;
/*     */ import android.content.pm.ApplicationInfo;
/*     */ import android.content.pm.PackageInfo;
/*     */ import android.content.pm.PackageManager;
/*     */ import android.content.pm.PackageManager.NameNotFoundException;
/*     */ import android.content.res.Resources;
/*     */ import android.graphics.Bitmap;
/*     */ import android.graphics.BitmapFactory;
/*     */ import android.net.Uri;
/*     */ import android.os.Bundle;
/*     */ import android.os.PowerManager;
/*     */ import android.os.PowerManager.WakeLock;
/*     */ import android.os.SystemClock;
/*     */ import android.support.v4.app.NotificationCompat.Builder;
/*     */ import android.text.TextUtils;
/*     */ import android.util.Log;
/*     */ 
/*     */ public class PushIOGCMIntentService extends IntentService
/*     */ {
/*  23 */   private static String KEY_REGISTRATIONID = "registration_id";
/*  24 */   private static String KEY_ERROR = "error";
/*  25 */   private static String KEY_UNREGISTERED = "unregistered";
/*     */ 
/*  27 */   private static String ERROR_TYPE_SERVICE_NOT_AVAILABLE = "SERVICE_NOT_AVAILABLE";
/*     */ 
/*  29 */   private static String PUSH_KEY_ALERT = "alert";
/*  30 */   private static String PUSH_KEY_BADGE = "badge";
/*  31 */   private static String PUSH_KEY_SOUND = "sound";
/*     */ 
/*  33 */   private static int MAX_BACKOFF_DURATION_MS = 14400000;
/*     */   private static PowerManager.WakeLock sWakeLock;
/*  36 */   private static final Object LOCK = PushIOGCMIntentService.class;
/*     */ 
/*     */   public PushIOGCMIntentService() {
/*  39 */     super("PushIOGCMIntentService");
/*     */   }
/*     */ 
/*     */   static void runIntentInService(Context context, Intent intent) {
/*  43 */     synchronized (LOCK) {
/*  44 */       if (sWakeLock == null) {
/*  45 */         PowerManager pm = (PowerManager)context.getSystemService("power");
/*  46 */         sWakeLock = pm.newWakeLock(1, "PushIOGCMWakeLock");
/*     */       }
/*     */     }
/*  49 */     sWakeLock.acquire();
/*  50 */     intent.setClassName(context, PushIOGCMIntentService.class.getName());
/*  51 */     context.startService(intent);
/*     */   }
/*     */ 
/*     */   public final void onHandleIntent(Intent intent)
/*     */   {
/*  56 */     Log.d("pushio", "Push Broadcast");
/*     */     try {
/*  58 */       String action = intent.getAction();
/*  59 */       if ((action.equals("com.google.android.c2dm.intent.REGISTRATION")) || (action.equals("com.amazon.device.messaging.intent.REGISTRATION"))) {
/*  60 */         PushIOSharedPrefs lPrefs = new PushIOSharedPrefs(getApplicationContext());
/*     */ 
/*  62 */         if (intent.hasExtra(KEY_ERROR)) {
/*  63 */           String lError = intent.getStringExtra(KEY_ERROR);
/*     */ 
/*  65 */           if (lError.equals(ERROR_TYPE_SERVICE_NOT_AVAILABLE)) {
/*  66 */             long backoffTimeMs = lPrefs.getBackoffTime();
/*  67 */             if (backoffTimeMs == 0L) {
/*  68 */               backoffTimeMs = 1000L;
/*     */             }
/*  70 */             long nextAttempt = SystemClock.elapsedRealtime() + backoffTimeMs;
/*  71 */             Intent retryIntent = new Intent("com.pushio.manager.push.intent.RETRY");
/*  72 */             retryIntent.putExtra("uuid", lPrefs.getUUID());
/*     */ 
/*  74 */             PendingIntent retryPendingIntent = PendingIntent.getBroadcast(this, 0, retryIntent, 0);
/*     */ 
/*  75 */             AlarmManager lAlarmManager = (AlarmManager)getSystemService("alarm");
/*  76 */             lAlarmManager.set(3, nextAttempt, retryPendingIntent);
/*  77 */             Log.e("pushio", "Push source registration error. Not available. Retrying in " + backoffTimeMs + " MS");
/*     */ 
/*  79 */             backoffTimeMs *= 2L;
/*     */ 
/*  81 */             if (backoffTimeMs > MAX_BACKOFF_DURATION_MS) {
/*  82 */               backoffTimeMs = MAX_BACKOFF_DURATION_MS;
/*     */             }
/*     */ 
/*  85 */             lPrefs.setBackoffTime(backoffTimeMs);
/*  86 */             lPrefs.commit();
/*     */           } else {
/*  88 */             Log.e("pushio", "Push source registration error. Code=" + intent.getStringExtra(KEY_ERROR));
/*     */           }
/*  90 */         } else if ((intent.hasExtra(KEY_UNREGISTERED)) && (intent.getBooleanExtra(KEY_UNREGISTERED, false))) {
/*  91 */           Log.d("pushio", "Unregistered from ADM.");
/*  92 */           lPrefs.setProjectId(null);
/*  93 */           lPrefs.commit();
/*     */ 
/*  96 */           PushIOManager.getInstance(this).registerCategories(null, false);
/*     */         } else {
/*  98 */           String lRegistrationId = intent.getStringExtra(KEY_REGISTRATIONID);
/*  99 */           Log.d("pushio", "Push registration received. id: " + lRegistrationId);
/*     */ 
/* 101 */           lPrefs.setRegistrationKey(lRegistrationId);
/* 102 */           lPrefs.setBackoffTime(0L);
/*     */ 
/* 104 */           int lVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
/* 105 */           lPrefs.setLastVersion(lVersionCode);
/* 106 */           lPrefs.commit();
/*     */ 
/* 109 */           PushIOManager.getInstance(this).registerCategories(null, false);
/*     */ 
/* 111 */           Log.d("pushio", "Device Token= " + lRegistrationId);
/*     */         }
/*     */       }
/* 114 */       else if ((action.equals("com.google.android.c2dm.intent.RECEIVE")) || (action.equals("com.amazon.device.messaging.intent.RECEIVE"))) {
/* 115 */         Log.d("pushio", "Push received!");
/* 116 */         handleMessage(intent);
/* 117 */       } else if (action.equals("com.pushio.manager.push.intent.RETRY")) {
/* 118 */         Log.d("pushio", "Retry received.");
/* 119 */         PushIOSharedPrefs lPrefs = new PushIOSharedPrefs(getApplicationContext());
/*     */ 
/* 121 */         if (intent.getStringExtra("uuid").equals(lPrefs.getUUID())) {
/* 122 */           Intent lIntent = new Intent(lPrefs.getNotificationService().equals("GCM") ? "com.google.android.c2dm.intent.REGISTER" : "com.amazon.device.messaging.intent.REGISTER");
/*     */ 
/* 127 */           lIntent.putExtra("app", PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(), 0));
/* 128 */           lIntent.putExtra("sender", lPrefs.getProjectId());
/* 129 */           getApplicationContext().startService(lIntent);
/*     */         }
/*     */       }
/*     */     } catch (PackageManager.NameNotFoundException e) {
/* 133 */       Log.e("pushio", e.getMessage());
/*     */     } finally {
/* 135 */       synchronized (LOCK) {
/* 136 */         if ((sWakeLock != null) && (sWakeLock.isHeld()))
/* 137 */           sWakeLock.release();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void handleMessage(Intent intent) {
/* 143 */     String lIntentStr = getApplicationContext().getPackageName() + ".PUSHIOPUSH";
/* 144 */     Intent lCustomNotificationIntent = new Intent(lIntentStr);
/* 145 */     lCustomNotificationIntent.putExtras(intent);
/* 146 */     sendOrderedBroadcast(lCustomNotificationIntent, null, new BroadcastReceiver()
/*     */     {
/*     */       public void onReceive(Context context, Intent intent)
/*     */       {
/* 150 */         int lPushStatus = getResultExtras(true).getInt(PushIOManager.PUSH_STATUS, PushIOManager.PUSH_UNHANDLED);
/*     */ 
/* 152 */         if (lPushStatus == PushIOManager.PUSH_HANDLED_NOTIFICATION)
/*     */         {
/* 154 */           return;
/*     */         }
/* 156 */         if (lPushStatus == PushIOManager.PUSH_HANDLED_IN_APP)
/*     */         {
/* 158 */           PushIOSharedPrefs lSharedPrefs = new PushIOSharedPrefs(PushIOGCMIntentService.this.getApplicationContext());
/* 159 */           lSharedPrefs.setEID(intent.getStringExtra(PushIOManager.PUSHIO_ENGAGEMENTID_KEY));
/* 160 */           lSharedPrefs.commit();
/*     */ 
/* 162 */           PushIOManager lPushIOManager = PushIOManager.getInstance(context);
/* 163 */           lPushIOManager.trackEngagement(PushIOManager.PUSHIO_ENGAGEMENT_METRIC_ACTIVE_SESSION, intent.getStringExtra(PushIOManager.PUSHIO_ENGAGEMENTID_KEY));
/* 164 */           return;
/* 165 */         }if (lPushStatus == PushIOManager.PUSH_UNHANDLED)
/*     */         {
/* 168 */           if ((intent.hasExtra(PushIOGCMIntentService.PUSH_KEY_ALERT)) && (!TextUtils.isEmpty(intent.getStringExtra(PushIOGCMIntentService.PUSH_KEY_ALERT))))
/*     */           {
/* 170 */             PushIOSharedPrefs sharedPrefs = new PushIOSharedPrefs(PushIOGCMIntentService.this.getApplicationContext());
/*     */ 
/* 185 */             PushIOGCMIntentService.this.getApplicationContext(); NotificationManager lNotificationManager = (NotificationManager)PushIOGCMIntentService.this.getSystemService("notification");
/*     */ 
/* 187 */             String alert = intent.getStringExtra(PushIOGCMIntentService.PUSH_KEY_ALERT);
/* 188 */             int notificationId = sharedPrefs.getIsNotificationsStacked() ? 0 : sharedPrefs.getAndIncrementNotificationCount();
/*     */ 
/* 190 */             Log.d("pushio", "alert=" + alert);
/*     */ 
/* 193 */             Intent launchIntent = new Intent(context, PushIOActivityLauncher.class);
/*     */ 
/* 195 */             launchIntent.putExtras(intent);
/* 196 */             int pendingIntentFlag = sharedPrefs.getIsNotificationsStacked() ? 134217728 : 1073741824;
/* 197 */             PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, launchIntent, pendingIntentFlag);
/*     */ 
/* 200 */             String[] alertStrings = alert.split("\\n");
/*     */             String contentText;
/*     */             String titleText;
/*     */             String contentText;
/* 205 */             if (alertStrings.length >= 2) {
/* 206 */               String titleText = alertStrings[0].trim();
/* 207 */               contentText = alertStrings[1].trim();
/*     */             } else {
/* 209 */               titleText = PushIOGCMIntentService.this.getResources().getString(PushIOGCMIntentService.this.getApplicationInfo().labelRes);
/* 210 */               contentText = alert;
/*     */             }
/*     */             int smallIconRes;
/*     */             int smallIconRes;
/* 213 */             if (intent.hasExtra(PushIOGCMIntentService.PUSH_KEY_BADGE)) {
/* 214 */               smallIconRes = PushIOGCMIntentService.this.getResources().getIdentifier(intent.getStringExtra(PushIOGCMIntentService.PUSH_KEY_BADGE), "drawable", PushIOGCMIntentService.this.getPackageName());
/*     */             }
/*     */             else
/*     */             {
/*     */               int smallIconRes;
/* 215 */               if (sharedPrefs.getSmallDefaultIcon() != 0) {
/* 216 */                 smallIconRes = sharedPrefs.getSmallDefaultIcon();
/*     */               } else {
/* 218 */                 ApplicationInfo info = PushIOGCMIntentService.this.getApplicationInfo();
/*     */                 int smallIconRes;
/* 220 */                 if ((info != null) && (info.icon != 0))
/* 221 */                   smallIconRes = info.icon;
/*     */                 else {
/* 223 */                   smallIconRes = 17301651;
/*     */                 }
/*     */               }
/*     */             }
/* 227 */             Uri soundUri = null;
/* 228 */             if (intent.hasExtra(PushIOGCMIntentService.PUSH_KEY_SOUND)) {
/* 229 */               soundUri = Uri.parse("android.resource://" + PushIOGCMIntentService.this.getApplicationContext().getApplicationInfo().packageName + "/raw/" + intent.getStringExtra(PushIOGCMIntentService.PUSH_KEY_SOUND));
/*     */             }
/*     */ 
/* 233 */             NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(PushIOGCMIntentService.this
/* 233 */               .getApplicationContext());
/*     */ 
/* 235 */             if (soundUri == null)
/* 236 */               notificationBuilder.setDefaults(1);
/*     */             else {
/* 238 */               notificationBuilder = notificationBuilder.setSound(soundUri);
/*     */             }
/*     */ 
/* 242 */             if (sharedPrefs.getLargeDefaultIcon() != 0) {
/* 243 */               Bitmap largeIconBitmap = BitmapFactory.decodeResource(PushIOGCMIntentService.this.getResources(), sharedPrefs.getLargeDefaultIcon());
/* 244 */               notificationBuilder = notificationBuilder.setLargeIcon(largeIconBitmap);
/*     */             }
/*     */ 
/* 247 */             notificationBuilder.setContentTitle(titleText);
/* 248 */             notificationBuilder.setContentText(contentText);
/* 249 */             notificationBuilder.setTicker(alert);
/* 250 */             notificationBuilder.setWhen(System.currentTimeMillis());
/* 251 */             notificationBuilder.setContentIntent(pendingIntent);
/* 252 */             notificationBuilder.setAutoCancel(true);
/* 253 */             notificationBuilder.setSmallIcon(smallIconRes);
/*     */ 
/* 255 */             Notification notification = notificationBuilder.build();
/*     */ 
/* 257 */             lNotificationManager.notify(notificationId, notification);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     , null, 0, null, null);
/*     */   }
/*     */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.PushIOGCMIntentService
 * JD-Core Version:    0.6.2
 */