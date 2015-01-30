/*     */ package com.pushio.manager;
/*     */ 
/*     */ import android.app.PendingIntent;
/*     */ import android.content.ComponentName;
/*     */ import android.content.Context;
/*     */ import android.content.Intent;
/*     */ import android.content.IntentFilter;
/*     */ import android.content.pm.PackageInfo;
/*     */ import android.content.pm.PackageManager;
/*     */ import android.content.pm.PackageManager.NameNotFoundException;
/*     */ import android.content.pm.ResolveInfo;
/*     */ import android.content.pm.ServiceInfo;
/*     */ import android.os.Build.VERSION;
/*     */ import android.os.Handler;
/*     */ import android.os.Looper;
/*     */ import android.util.Log;
/*     */ import com.pushio.manager.tasks.PushIOEngagementListener;
/*     */ import com.pushio.manager.tasks.PushIOEngagementTask;
/*     */ import com.pushio.manager.tasks.PushIOListener;
/*     */ import com.pushio.manager.tasks.PushIORegisterTask;
/*     */ import com.pushio.manager.trackers.PushIOPublisher;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class PushIOManager
/*     */   implements PushIOListener, PushIOEngagementListener
/*     */ {
/*  23 */   private static final String TAG = PushIOManager.class.getSimpleName();
/*     */ 
/*  25 */   private static PushIOManager mInstance = null;
/*     */ 
/*  27 */   private static String LIB_VERSION = "2.10.7";
/*     */   private static final String PACKAGE_NAME_GMS = "com.google.android.gms";
/*  31 */   public static int PUSHIO_ENGAGEMENT_METRIC_LAUNCH = 1;
/*  32 */   public static int PUSHIO_ENGAGEMENT_METRIC_ACTIVE_SESSION = 2;
/*  33 */   public static int PUSHIO_ENGAGEMENT_METRIC_INAPP_PURCHASE = 3;
/*  34 */   public static int PUSHIO_ENGAGEMENT_METRIC_PREMIUM_CONTENT = 4;
/*  35 */   public static int PUSHIO_ENGAGEMENT_METRIC_SOCIAL = 5;
/*  36 */   public static int PUSHIO_ENGAGEMENT_METRIC_OTHER = 6;
/*     */ 
/*  38 */   public static String PUSHIO_ENGAGEMENTID_KEY = "ei";
/*     */ 
/*  41 */   public static String PUSH_STATUS = "push_status";
/*     */ 
/*  43 */   public static int PUSH_UNHANDLED = 1;
/*  44 */   public static int PUSH_HANDLED_IN_APP = 2;
/*  45 */   public static int PUSH_HANDLED_NOTIFICATION = 3;
/*     */   public static final String USER_ID_VALIDATOR = "\\A[a-zA-Z0-9\\-_|:\\/.+=%?@]{1,255}\\Z";
/*  50 */   private boolean mRegistrationUpdateScheduled = false;
/*  51 */   private boolean mRegistrationUpdating = false;
/*     */   private PushIOConfig mPushIOConfig;
/*     */   private PushIOSharedPrefs mSharedPrefs;
/*     */   private Context mAppContext;
/*     */   private PushIOListener mListener;
/*     */   private PushIOEngagementListener mEngagementListener;
/*     */   private PushIOBroadcastReceiver mRetryReceiver;
/*     */   private boolean mDeleteScheduled;
/*     */   private boolean mRegistrationIsDirty;
/*     */   private List<String> mScheduledCategories;
/*     */   private List<String> mRegisteringCategories;
/*     */   private List<PushIOPublisher> mScheduledPublishers;
/*     */   private List<PushIOPublisher> mRegisteringPublishers;
/*     */   private String mScheduledUserId;
/*     */   private String mRegisteringUserId;
/*  67 */   private static int RESYNC_TIME = 18000000;
/*     */ 
/*  69 */   private Object LOCK = PushIOManager.class;
/*     */ 
/*     */   public static synchronized PushIOManager getInstance(Context context) {
/*  72 */     if (mInstance == null) {
/*  73 */       mInstance = new PushIOManager(context.getApplicationContext(), null, null);
/*     */     }
/*     */ 
/*  76 */     return mInstance;
/*     */   }
/*     */ 
/*     */   private PushIOManager(Context appContext, PushIOListener listener, PushIOEngagementListener engagementListener)
/*     */   {
/*  87 */     Log.d("pushio", "Push IO Manager - version: " + LIB_VERSION);
/*     */ 
/*  89 */     this.mAppContext = appContext;
/*  90 */     this.mSharedPrefs = new PushIOSharedPrefs(appContext);
/*  91 */     detectNotificationService(this.mSharedPrefs);
/*  92 */     this.mPushIOConfig = PushIOConfig.createPushIOConfigFromAssets(appContext, this.mSharedPrefs.getNotificationService());
/*  93 */     this.mListener = listener;
/*  94 */     this.mEngagementListener = engagementListener;
/*     */   }
/*     */ 
/*     */   public void registerPushIOListener(PushIOListener pushIOListener) {
/*  98 */     this.mListener = pushIOListener;
/*     */   }
/*     */ 
/*     */   public void registerPushIOEngagementListener(PushIOEngagementListener engagementListener) {
/* 102 */     this.mEngagementListener = engagementListener;
/*     */   }
/*     */ 
/*     */   public void overwriteApiKey(String apiKey)
/*     */   {
/* 110 */     this.mPushIOConfig.overwriteApiKey(apiKey);
/*     */   }
/*     */ 
/*     */   public String getUUID()
/*     */   {
/* 119 */     return this.mSharedPrefs.getUUID();
/*     */   }
/*     */ 
/*     */   public String getAPIKey()
/*     */   {
/* 127 */     return this.mPushIOConfig.getPushIOKey();
/*     */   }
/*     */ 
/*     */   public void finalize() throws Throwable
/*     */   {
/* 132 */     super.finalize();
/* 133 */     unregisterRetryReceiver();
/*     */   }
/*     */ 
/*     */   public void ensureRegistration()
/*     */   {
/*     */     try
/*     */     {
/* 142 */       int lVersionCode = this.mAppContext.getPackageManager().getPackageInfo(this.mAppContext.getPackageName(), 0).versionCode;
/* 143 */       if (lVersionCode != this.mSharedPrefs.getLastVersion()) {
/* 144 */         this.mSharedPrefs.setEID(null);
/* 145 */         this.mSharedPrefs.setRegistrationKey(null);
/* 146 */         this.mSharedPrefs.commit();
/* 147 */         registerCategories(null, false);
/* 148 */         return;
/*     */       }
/*     */ 
/* 152 */       synchronized (this.LOCK) {
/* 153 */         if (((this.mScheduledCategories != null) || (this.mScheduledPublishers != null) || (this.mScheduledUserId != null)) && 
/* 154 */           (this.mSharedPrefs
/* 154 */           .getRegistrationKey() != null) && (!this.mRegistrationUpdateScheduled))
/* 155 */           registerCategories(null, false);
/* 156 */         else if ((this.mSharedPrefs.getIsBroadcastRegistered()) && (!this.mRegistrationUpdateScheduled) && (!this.mRegistrationUpdating))
/*     */         {
/* 159 */           if ((this.mSharedPrefs
/* 159 */             .getLastUpdateTime() != 0L) && 
/* 160 */             (new Date()
/* 160 */             .getTime() > this.mSharedPrefs.getLastUpdateTime() + RESYNC_TIME)) {
/* 161 */             Log.d("pushio", "It has been more than 5 hours, refreshing push.io registration now");
/* 162 */             registerCategories(null, false);
/*     */           }
/*     */         }
/*     */       }
/*     */     } catch (PackageManager.NameNotFoundException e) {
/* 167 */       Log.e("pushio", e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getLibVersion() {
/* 172 */     return LIB_VERSION;
/*     */   }
/*     */ 
/*     */   private void scheduleRegistration(List<String> categories, List<PushIOPublisher> publishers, String userId, boolean deleteRegistration) {
/* 176 */     synchronized (this.LOCK)
/*     */     {
/* 178 */       this.mRegistrationIsDirty = true;
/* 179 */       this.mScheduledCategories = categories;
/* 180 */       this.mScheduledPublishers = publishers;
/* 181 */       this.mScheduledUserId = userId;
/* 182 */       this.mDeleteScheduled = deleteRegistration;
/*     */ 
/* 184 */       boolean lProjectIDsMatch = this.mPushIOConfig.getNotificationServiceProjectId().equals(this.mSharedPrefs.getProjectId());
/*     */ 
/* 186 */       if ((this.mSharedPrefs.getRegistrationKey() == null) || (!lProjectIDsMatch))
/*     */       {
/* 188 */         if (("ADM".equals(this.mSharedPrefs.getNotificationService())) && 
/* 189 */           (this.mSharedPrefs
/* 189 */           .getProjectId() != null) && (!lProjectIDsMatch))
/*     */         {
/* 192 */           Log.d("pushio", "ADM project ID has changed, unregister.");
/* 193 */           Intent lIntent = new Intent("com.amazon.device.messaging.intent.UNREGISTER");
/* 194 */           lIntent.putExtra("app", PendingIntent.getBroadcast(this.mAppContext, 0, new Intent(), 0));
/* 195 */           lIntent.putExtra("sender", this.mSharedPrefs.getProjectId());
/*     */ 
/* 197 */           this.mAppContext.startService(lIntent);
/* 198 */           return;
/*     */         }
/*     */ 
/* 202 */         registerWithNotificationService();
/*     */ 
/* 204 */         return;
/*     */       }
/*     */ 
/* 207 */       if ((this.mRegistrationUpdateScheduled) || (this.mRegistrationUpdating))
/*     */       {
/* 209 */         Log.d("pushio", "Updating existing categories");
/*     */       } else {
/* 211 */         Log.d("pushio", "Scheduling a new registration in 15 seconds");
/* 212 */         this.mRegistrationUpdateScheduled = true;
/* 213 */         new Handler(Looper.getMainLooper()).postDelayed(getTimerTask(), 15000L);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private void registerWithNotificationService() {
/* 219 */     if (this.mSharedPrefs.getNotificationService() == null) {
/* 220 */       Log.d("pushio", "No supported notification service was detected, skipping registration.");
/* 221 */       return;
/*     */     }
/*     */ 
/* 225 */     if ((this.mSharedPrefs.getRegistrationKey() == null) || (!this.mPushIOConfig.getNotificationServiceProjectId().equals(this.mSharedPrefs.getProjectId()))) {
/* 226 */       Log.d("pushio", String.format("Registering device with '%s' servers...", new Object[] { this.mSharedPrefs.getNotificationService() }));
/*     */ 
/* 228 */       this.mRetryReceiver = new PushIOBroadcastReceiver();
/* 229 */       IntentFilter filter = new IntentFilter("com.pushio.manager.push.intent.RETRY");
/* 230 */       filter.addCategory(this.mAppContext.getPackageName());
/* 231 */       this.mAppContext.registerReceiver(this.mRetryReceiver, filter);
/*     */ 
/* 234 */       Intent lIntent = new Intent("GCM"
/* 234 */         .equals(this.mSharedPrefs
/* 234 */         .getNotificationService()) ? "com.google.android.c2dm.intent.REGISTER" : "com.amazon.device.messaging.intent.REGISTER");
/*     */ 
/* 236 */       this.mSharedPrefs.setProjectId(this.mPushIOConfig.getNotificationServiceProjectId());
/* 237 */       this.mSharedPrefs.commit();
/* 238 */       lIntent.putExtra("app", PendingIntent.getBroadcast(this.mAppContext, 0, new Intent(), 0));
/* 239 */       lIntent.putExtra("sender", this.mPushIOConfig.getNotificationServiceProjectId());
/*     */ 
/* 241 */       if (Build.VERSION.SDK_INT >= 21) {
/* 242 */         lIntent = createExplicitFromImplicitIntent(this.mAppContext, lIntent);
/*     */       }
/*     */ 
/* 245 */       if (lIntent != null)
/* 246 */         this.mAppContext.startService(lIntent);
/*     */       else
/* 248 */         Log.d("pushio", "Google Play Services not found on this device.");
/*     */     }
/*     */     else {
/* 251 */       Log.d("pushio", "Your Push IO Device ID is: " + this.mSharedPrefs.getUUID());
/*     */     }
/*     */   }
/*     */ 
/*     */   private void detectNotificationService(PushIOSharedPrefs prefs) {
/* 256 */     Log.d("pushio", "Detecting notification service...");
/*     */     try {
/* 258 */       this.mAppContext.getPackageManager().getPermissionInfo("com.google.android.c2dm.permission.RECEIVE", 0);
/*     */ 
/* 260 */       Log.d("pushio", "Device has com.google.android.c2dm.permission.RECEIVE permission defined, using GCM for notification service.");
/* 261 */       this.mSharedPrefs.setNotificationService("GCM");
/* 262 */       this.mSharedPrefs.commit();
/*     */     } catch (PackageManager.NameNotFoundException e) {
/* 264 */       Log.d("pushio", "GCM not found. Trying ADM.");
/*     */       try {
/* 266 */         this.mAppContext.getPackageManager().getPermissionInfo("com.amazon.device.messaging.permission.RECEIVE", 0);
/*     */ 
/* 268 */         Log.d("pushio", "Device has 'com.amazon.device.messaging.permission.RECEIVE' permission defined, using ADM for notification service.");
/* 269 */         this.mSharedPrefs.setNotificationService("ADM");
/* 270 */         this.mSharedPrefs.commit();
/*     */       } catch (PackageManager.NameNotFoundException e1) {
/* 272 */         Log.d("pushio", "ADM not found. Device supports no known notification services.");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private Runnable getTimerTask() {
/* 278 */     return new Object()
/*     */     {
/*     */       public void run() {
/* 281 */         Log.d("pushio", "Starting registration...");
/* 282 */         synchronized (PushIOManager.this.LOCK) {
/* 283 */           PushIOManager.this.mRegistrationIsDirty = false;
/* 284 */           PushIOManager.this.mRegistrationUpdating = true;
/* 285 */           PushIOManager.this.mRegistrationUpdateScheduled = false;
/* 286 */           PushIORegisterTask task = new PushIORegisterTask(PushIOManager.this.mAppContext, PushIOManager.getInstance(PushIOManager.this.mAppContext), PushIOManager.this.mScheduledCategories, PushIOManager.this.mScheduledPublishers, PushIOManager.this.mScheduledUserId, PushIOManager.this.mPushIOConfig, PushIOManager.this.mSharedPrefs, PushIOManager.this.mDeleteScheduled);
/* 287 */           PushIOManager.this.mRegisteringCategories = PushIOManager.this.mScheduledCategories;
/* 288 */           PushIOManager.this.mRegisteringPublishers = PushIOManager.this.mScheduledPublishers;
/* 289 */           PushIOManager.this.mRegisteringUserId = PushIOManager.this.mScheduledUserId;
/* 290 */           PushIOManager.this.mScheduledCategories = null;
/* 291 */           PushIOManager.this.mScheduledPublishers = null;
/* 292 */           PushIOManager.this.mScheduledUserId = null;
/* 293 */           task.executeCompat(new Void[0]);
/*     */         }
/*     */       }
/*     */     };
/*     */   }
/*     */ 
/*     */   public void registerCategory(String category)
/*     */   {
/* 301 */     if (category != null) {
/* 302 */       List categories = new ArrayList();
/* 303 */       categories.add(category);
/* 304 */       registerCategories(categories, false);
/*     */     } else {
/* 306 */       registerCategories(null, false);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void unregisterCategory(String category) {
/* 311 */     List categories = new ArrayList();
/* 312 */     categories.add(category);
/* 313 */     unregisterCategories(categories);
/*     */   }
/*     */ 
/*     */   public void registerCategories(List<String> categories, boolean clearExisting)
/*     */   {
/* 322 */     synchronized (this.LOCK)
/*     */     {
/*     */       List lCategories;
/*     */       List lCategories;
/* 324 */       if (!clearExisting)
/* 325 */         lCategories = mergeLists(getRegisteredCategories(), categories);
/*     */       else {
/* 327 */         lCategories = categories;
/*     */       }
/* 329 */       scheduleRegistration(lCategories, getRegisteredPublishers(), getRegisteredUserId(), false);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void unregisterCategories(List<String> categories)
/*     */   {
/* 340 */     synchronized (this.LOCK) {
/* 341 */       List lCategories = getRegisteredCategories();
/* 342 */       for (String lCategory : categories) {
/* 343 */         lCategories.remove(lCategory);
/*     */       }
/* 345 */       scheduleRegistration(lCategories, getRegisteredPublishers(), getRegisteredUserId(), false);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void unregisterAllCategories()
/*     */   {
/* 353 */     scheduleRegistration(new ArrayList(), getRegisteredPublishers(), getRegisteredUserId(), false);
/*     */   }
/*     */ 
/*     */   public void unregisterDevice()
/*     */   {
/* 360 */     scheduleRegistration(null, null, null, true);
/*     */   }
/*     */ 
/*     */   public List<String> getRegisteredCategories()
/*     */   {
/* 368 */     synchronized (this.LOCK) {
/* 369 */       if ((this.mRegisteringCategories != null) || (this.mScheduledCategories != null))
/*     */       {
/* 371 */         List categories = new ArrayList();
/*     */ 
/* 373 */         if (this.mRegisteringCategories != null) {
/* 374 */           categories.addAll(this.mRegisteringCategories);
/*     */         }
/*     */ 
/* 377 */         if (this.mScheduledCategories != null) {
/* 378 */           categories = mergeLists(categories, this.mScheduledCategories);
/*     */         }
/*     */ 
/* 381 */         return categories;
/*     */       }
/* 383 */       return this.mSharedPrefs.getCategories();
/*     */     }
/*     */   }
/*     */ 
/*     */   public PushIOPublisher getPublisher(String publisherName)
/*     */   {
/* 395 */     synchronized (this.LOCK)
/*     */     {
/* 397 */       if (this.mScheduledPublishers != null) {
/* 398 */         for (PushIOPublisher publisher : this.mScheduledPublishers) {
/* 399 */           if (publisher.getPublisherKey().contentEquals(publisherName)) {
/* 400 */             return publisher;
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 406 */       if (this.mRegisteringPublishers != null)
/*     */       {
/* 408 */         for (PushIOPublisher publisher : this.mRegisteringPublishers) {
/* 409 */           if (publisher.getPublisherKey().contentEquals(publisherName)) {
/* 410 */             return publisher;
/*     */           }
/*     */         }
/*     */       }
/* 414 */       for (PushIOPublisher publisher : this.mSharedPrefs.getPublisherList()) {
/* 415 */         if (publisher.getPublisherKey().contentEquals(publisherName)) {
/* 416 */           return publisher;
/*     */         }
/*     */       }
/* 419 */       return new PushIOPublisher(publisherName);
/*     */     }
/*     */   }
/*     */ 
/*     */   private List<PushIOPublisher> getRegisteredPublishers()
/*     */   {
/* 425 */     synchronized (this.LOCK) {
/* 426 */       List publisherList = new ArrayList();
/*     */ 
/* 428 */       if (this.mScheduledPublishers != null)
/* 429 */         publisherList.addAll(this.mScheduledPublishers);
/*     */       Iterator localIterator1;
/* 433 */       if (this.mRegisteringPublishers != null)
/* 434 */         for (localIterator1 = this.mRegisteringPublishers.iterator(); localIterator1.hasNext(); ) { publisher = (PushIOPublisher)localIterator1.next();
/* 435 */           boolean publisherFound = false;
/* 436 */           for (Iterator localIterator2 = publisherList.iterator(); localIterator2.hasNext(); ) { existingPublisher = (PushIOPublisher)localIterator2.next();
/* 437 */             if (existingPublisher.getPublisherKey().contentEquals(publisher.getPublisherKey())) {
/* 438 */               publisherFound = true;
/*     */             }
/*     */           }
/*     */ 
/* 442 */           if (!publisherFound)
/* 443 */             publisherList.add(publisher);
/*     */         }
/*     */       PushIOPublisher publisher;
/*     */       PushIOPublisher existingPublisher;
/* 447 */       List savedPublishers = this.mSharedPrefs.getPublisherList();
/*     */ 
/* 449 */       for (PushIOPublisher publisher : savedPublishers) {
/* 450 */         boolean publisherFound = false;
/* 451 */         for (PushIOPublisher existingPublisher : publisherList) {
/* 452 */           if (existingPublisher.getPublisherKey().contentEquals(publisher.getPublisherKey())) {
/* 453 */             publisherFound = true;
/*     */           }
/*     */         }
/*     */ 
/* 457 */         if (!publisherFound) {
/* 458 */           publisherList.add(publisher);
/*     */         }
/*     */       }
/* 461 */       return publisherList;
/*     */     }
/*     */   }
/*     */ 
/*     */   private List<String> mergeLists(List<String> list1, List<String> list2) {
/* 466 */     List categories = new ArrayList();
/* 467 */     categories.addAll(list1);
/*     */ 
/* 469 */     if (list2 != null) {
/* 470 */       for (int i = list2.size() - 1; i >= 0; i--) {
/* 471 */         if (!list1.contains(list2.get(i))) {
/* 472 */           categories.add(list2.get(i));
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 477 */     return categories;
/*     */   }
/*     */ 
/*     */   public void registerPublisher(PushIOPublisher publisher)
/*     */   {
/* 486 */     List lPublishers = getRegisteredPublishers();
/*     */ 
/* 488 */     for (PushIOPublisher lPublisher : lPublishers) {
/* 489 */       if (lPublisher.getPublisherKey().contentEquals(publisher.getPublisherKey())) {
/* 490 */         lPublishers.remove(lPublisher);
/*     */       }
/*     */     }
/* 493 */     lPublishers.add(publisher);
/* 494 */     publisher.removeDuplicates();
/*     */ 
/* 496 */     scheduleRegistration(getRegisteredCategories(), lPublishers, getRegisteredUserId(), false);
/*     */   }
/*     */ 
/*     */   public void trackEngagement(int metric)
/*     */   {
/* 504 */     PushIOEngagementTask lTask = new PushIOEngagementTask(this.mAppContext, this, this.mPushIOConfig, this.mSharedPrefs, metric, null);
/* 505 */     lTask.executeCompat(new Void[0]);
/*     */   }
/*     */ 
/*     */   public void trackEngagement(int metric, String engagementId) {
/* 509 */     PushIOEngagementTask lTask = new PushIOEngagementTask(this.mAppContext, this, this.mPushIOConfig, this.mSharedPrefs, metric, null);
/* 510 */     lTask.setEngagementId(engagementId);
/* 511 */     lTask.executeCompat(new Void[0]);
/*     */   }
/*     */ 
/*     */   public void trackEngagementSynchronous(int metric, String engagementId)
/*     */   {
/* 516 */     PushIOEngagementTask lTask = new PushIOEngagementTask(this.mAppContext, this, this.mPushIOConfig, this.mSharedPrefs, metric, null);
/* 517 */     lTask.setEngagementId(engagementId);
/* 518 */     lTask.trackEngagement();
/*     */   }
/*     */ 
/*     */   public void trackCustomEngagement(String engagement)
/*     */   {
/* 526 */     PushIOEngagementTask lTask = new PushIOEngagementTask(this.mAppContext, this, this.mPushIOConfig, this.mSharedPrefs, PUSHIO_ENGAGEMENT_METRIC_OTHER, engagement);
/* 527 */     lTask.executeCompat(new Void[0]);
/*     */   }
/*     */ 
/*     */   public void resetEID()
/*     */   {
/* 534 */     this.mSharedPrefs.resetEID();
/*     */   }
/*     */ 
/*     */   public void setEngagementId(Intent intent)
/*     */   {
/* 542 */     if (intent.hasExtra(PUSHIO_ENGAGEMENTID_KEY)) {
/* 543 */       this.mSharedPrefs.setEID(intent.getStringExtra(PUSHIO_ENGAGEMENTID_KEY));
/* 544 */       this.mSharedPrefs.commit();
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isCurrentSessionAnEngagement()
/*     */   {
/* 553 */     return this.mSharedPrefs.getEID() != null;
/*     */   }
/*     */ 
/*     */   public void registerUserId(String userId)
/*     */   {
/* 561 */     if (userId != null) {
/* 562 */       Pattern pattern = Pattern.compile("\\A[a-zA-Z0-9\\-_|:\\/.+=%?@]{1,255}\\Z");
/* 563 */       Matcher matcher = pattern.matcher(userId);
/*     */ 
/* 565 */       if (matcher.matches()) {
/* 566 */         scheduleRegistration(getRegisteredCategories(), getRegisteredPublishers(), userId, false);
/*     */       }
/*     */       else
/* 569 */         throw new IllegalArgumentException(
/* 569 */           String.format("The provided User ID did not match the expected format: %s", new Object[] { "\\A[a-zA-Z0-9\\-_|:\\/.+=%?@]{1,255}\\Z" }));
/*     */     }
/*     */     else
/*     */     {
/* 572 */       scheduleRegistration(getRegisteredCategories(), getRegisteredPublishers(), null, false);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getRegisteredUserId() {
/* 577 */     if (this.mScheduledUserId != null)
/* 578 */       return this.mScheduledUserId;
/* 579 */     if (this.mRegisteringUserId != null) {
/* 580 */       return this.mRegisteringUserId;
/*     */     }
/* 582 */     return this.mSharedPrefs.getUserId();
/*     */   }
/*     */ 
/*     */   public void unregisterUserId()
/*     */   {
/* 587 */     scheduleRegistration(getRegisteredCategories(), getRegisteredPublishers(), null, false);
/*     */   }
/*     */ 
/*     */   public boolean getIsBroadcastRegistered() {
/* 591 */     return this.mSharedPrefs.getIsBroadcastRegistered();
/*     */   }
/*     */ 
/*     */   public void setDefaultSmallIcon(int resourceId) {
/* 595 */     this.mSharedPrefs.setSmallDefaultIcon(resourceId);
/* 596 */     this.mSharedPrefs.commit();
/*     */   }
/*     */ 
/*     */   public void setDefaultLargeIcon(int resourceId) {
/* 600 */     this.mSharedPrefs.setLargeDefaultIcon(resourceId);
/* 601 */     this.mSharedPrefs.commit();
/*     */   }
/*     */ 
/*     */   public void setNotificationsStacked(boolean notificationsStacked) {
/* 605 */     this.mSharedPrefs.setNotificationsStacked(notificationsStacked);
/* 606 */     this.mSharedPrefs.commit();
/*     */   }
/*     */ 
/*     */   private void unregisterRetryReceiver() {
/* 610 */     if (this.mRetryReceiver != null) {
/* 611 */       this.mAppContext.unregisterReceiver(this.mRetryReceiver);
/*     */     }
/* 613 */     this.mRetryReceiver = null;
/*     */   }
/*     */ 
/*     */   private Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent)
/*     */   {
/* 628 */     PackageManager pm = context.getPackageManager();
/* 629 */     List resolveInfo = pm.queryIntentServices(implicitIntent, 0);
/*     */ 
/* 632 */     if ((resolveInfo == null) || (resolveInfo.size() < 1)) {
/* 633 */       return null;
/*     */     }
/*     */ 
/* 636 */     for (ResolveInfo serviceInfo : resolveInfo) {
/* 637 */       String packageName = serviceInfo.serviceInfo.packageName;
/*     */ 
/* 640 */       if ((packageName != null) && (packageName.equals("com.google.android.gms")))
/*     */       {
/* 642 */         String className = serviceInfo.serviceInfo.name;
/*     */ 
/* 644 */         ComponentName component = new ComponentName(packageName, className);
/*     */ 
/* 647 */         Intent explicitIntent = new Intent(implicitIntent);
/*     */ 
/* 650 */         explicitIntent.setComponent(component);
/*     */ 
/* 652 */         return explicitIntent;
/*     */       }
/*     */     }
/*     */ 
/* 656 */     return null;
/*     */   }
/*     */ 
/*     */   public void onPushIOSuccess()
/*     */   {
/* 661 */     Log.d("pushio", "Registration with push.io servers successful!");
/* 662 */     synchronized (this.LOCK) {
/* 663 */       this.mRegistrationUpdating = false;
/* 664 */       this.mRegisteringCategories = null;
/* 665 */       this.mRegisteringPublishers = null;
/* 666 */       this.mRegisteringUserId = null;
/*     */ 
/* 668 */       if (this.mRegistrationIsDirty) {
/* 669 */         Log.d("pushio", "Categories added during registration, scheduling another update in 15 seconds.");
/* 670 */         new Handler(Looper.getMainLooper()).postDelayed(getTimerTask(), 15000L);
/*     */       }
/*     */     }
/*     */ 
/* 674 */     if (this.mListener != null)
/* 675 */       this.mListener.onPushIOSuccess();
/*     */   }
/*     */ 
/*     */   public void onPushIOError(String aMessage)
/*     */   {
/* 682 */     Log.e("pushio", "Registration error. Message: " + aMessage);
/*     */ 
/* 684 */     synchronized (this.LOCK) {
/* 685 */       this.mRegistrationUpdating = false;
/* 686 */       this.mScheduledCategories = this.mRegisteringCategories;
/* 687 */       this.mScheduledPublishers = this.mRegisteringPublishers;
/* 688 */       this.mScheduledUserId = this.mRegisteringUserId;
/* 689 */       this.mRegisteringCategories = null;
/* 690 */       this.mRegisteringPublishers = null;
/* 691 */       this.mRegisteringUserId = null;
/*     */     }
/*     */ 
/* 694 */     if (this.mListener != null)
/* 695 */       this.mListener.onPushIOError(aMessage);
/*     */   }
/*     */ 
/*     */   public void onEngagementSuccess()
/*     */   {
/* 701 */     Log.d("pushio", "Engagement Tracked");
/* 702 */     if (this.mEngagementListener != null)
/* 703 */       this.mEngagementListener.onEngagementSuccess();
/*     */   }
/*     */ 
/*     */   public void onEngagementError(String error)
/*     */   {
/* 709 */     Log.e("pushio", "Engagement error. Message: " + error);
/* 710 */     if (this.mEngagementListener != null)
/* 711 */       this.mEngagementListener.onEngagementError(error);
/*     */   }
/*     */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.PushIOManager
 * JD-Core Version:    0.6.2
 */