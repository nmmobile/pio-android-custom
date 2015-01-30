/*     */ package com.pushio.manager;
/*     */ 
/*     */ import android.content.Context;
/*     */ import android.content.SharedPreferences;
/*     */ import android.content.SharedPreferences.Editor;
/*     */ import android.os.Build.VERSION;
/*     */ import android.util.Log;
/*     */ import com.pushio.manager.trackers.PushIOPublisher;
/*     */ import com.pushio.manager.trackers.PushIOTracker;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.UUID;
/*     */ import org.json.JSONArray;
/*     */ import org.json.JSONObject;
/*     */ 
/*     */ public final class PushIOSharedPrefs
/*     */ {
/*     */   private static final String CATEGORY_PREFIX_KEY = "category.";
/*     */   private static final String PUBLISHER_PREFIX_KEY = "publisher.";
/*     */   private static final String UUID_KEY = "uuid";
/*     */   private static final String INSTALLED_AT_KEY = "installed_at";
/*     */   private static final String REGISTRATION_KEY = "registration_key";
/*     */   private static final String LAST_VERSION_KEY = "last_version";
/*     */   private static final String PROJECTID_KEY = "project_id";
/*     */   private static final String EID_KEY = "pushio_eid";
/*     */   private static final String USER_ID_KEY = "user_id";
/*     */   private static final String BACKOFF_TIME_KEY = "retry_backoff_time";
/*     */   private static final String BROADCAST_REGISTERED_KEY = "broadcast_registered_key";
/*     */   private static final String LAST_UPDATE_TIME_KEY = "last_update";
/*     */   private static final String DEFAULT_SMALL_ICON_KEY = "small_icon_res";
/*     */   private static final String DEFAULT_LARGE_ICON_KEY = "large_icon_res";
/*     */   private static final String NOTIFICATION_SERVICE_KEY = "notification_service";
/*     */   private static final String STACK_NOTIFICATIONS_KEY = "stack_notifications";
/*     */   private static final String NOTIFICIATION_COUNT_KEY = "notification_count";
/*     */   private SharedPreferences prefs;
/*     */   private SharedPreferences.Editor prefsEditor;
/*     */   private int mApiVersion;
/*     */ 
/*     */   public PushIOSharedPrefs(Context context)
/*     */   {
/*  44 */     this.prefs = context.getSharedPreferences(context.getApplicationContext().getPackageName(), 0);
/*  45 */     this.prefsEditor = this.prefs.edit();
/*     */   }
/*     */ 
/*     */   public void addCategory(String value) {
/*  49 */     String key = "category." + value;
/*  50 */     this.prefsEditor.putString(key, value);
/*     */   }
/*     */ 
/*     */   public void removeCategory(String value) {
/*  54 */     String key = "category." + value;
/*  55 */     this.prefsEditor.remove(key);
/*     */   }
/*     */ 
/*     */   public List<String> getCategories() {
/*  59 */     Map pairs = this.prefs.getAll();
/*  60 */     List lCategories = new ArrayList();
/*     */ 
/*  62 */     for (String key : pairs.keySet()) {
/*  63 */       if (key.startsWith("category.")) {
/*  64 */         lCategories.add(key.substring("category.".length()));
/*     */       }
/*     */     }
/*     */ 
/*  68 */     return lCategories;
/*     */   }
/*     */ 
/*     */   public List<PushIOPublisher> getPublisherList() {
/*  72 */     List lPublisherList = new ArrayList();
/*     */ 
/*  74 */     Map lPairs = this.prefs.getAll();
/*     */ 
/*  79 */     for (String lKey : lPairs.keySet()) {
/*  80 */       if (lKey.startsWith("publisher.")) {
/*  81 */         String lPublisherName = lKey.substring("publisher.".length());
/*  82 */         PushIOPublisher lPublisher = new PushIOPublisher(lPublisherName);
/*  83 */         lPublisherList.add(lPublisher);
/*     */ 
/*  85 */         String lPublisherStr = this.prefs.getString(lKey, null);
/*  86 */         if (lPublisherStr != null) {
/*     */           try {
/*  88 */             JSONArray lPublisherJSON = new JSONArray(lPublisherStr);
/*     */ 
/*  91 */             for (int i = 0; i < lPublisherJSON.length(); i++) {
/*  92 */               JSONObject lTrackerJSON = lPublisherJSON.getJSONObject(i);
/*  93 */               PushIOTracker lTracker = lPublisher.addTracker();
/*  94 */               JSONArray lInterestsJSON = lTrackerJSON.getJSONArray("interests");
/*     */ 
/*  96 */               for (int lInterestCounter = 0; lInterestCounter < lInterestsJSON.length(); lInterestCounter++) {
/*  97 */                 lTracker.addInterest(lInterestsJSON.getString(lInterestCounter));
/*     */               }
/*     */ 
/* 100 */               JSONArray lValueJSON = lTrackerJSON.getJSONArray("values");
/*     */ 
/* 102 */               for (int lValueCounter = 0; lValueCounter < lValueJSON.length(); lValueCounter++)
/* 103 */                 lTracker.addValue(lValueJSON.getString(lValueCounter));
/*     */             }
/*     */           }
/*     */           catch (Exception e) {
/* 107 */             Log.e("pushio", "JSON Error", e);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 113 */     return lPublisherList;
/*     */   }
/*     */ 
/*     */   public void addPublisher(PushIOPublisher publisher) {
/*     */     try {
/* 118 */       JSONArray lBaseArray = new JSONArray();
/*     */ 
/* 120 */       List lTrackerList = publisher.getTrackerList();
/*     */ 
/* 122 */       for (PushIOTracker lTracker : lTrackerList) {
/* 123 */         JSONObject lJSONObject = new JSONObject();
/* 124 */         lBaseArray.put(lJSONObject);
/*     */ 
/* 126 */         JSONArray lInterestArray = new JSONArray(lTracker.getInterestList());
/* 127 */         lJSONObject.put("interests", lInterestArray);
/*     */ 
/* 129 */         JSONArray lValueArray = new JSONArray(lTracker.getValueList());
/* 130 */         lJSONObject.put("values", lValueArray);
/*     */       }
/*     */ 
/* 133 */       Log.d("pushio", lBaseArray.toString());
/* 134 */       this.prefsEditor.putString("publisher." + publisher.getPublisherKey(), lBaseArray.toString());
/*     */     }
/*     */     catch (Exception e) {
/* 137 */       Log.e("pushio", "JSON Error", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clearCategories() {
/* 142 */     Map lPairs = this.prefs.getAll();
/* 143 */     for (String lKey : lPairs.keySet()) {
/* 144 */       if (lKey.startsWith("category.")) {
/* 145 */         this.prefsEditor.remove(lKey);
/*     */       }
/*     */     }
/* 148 */     commit();
/*     */   }
/*     */ 
/*     */   public void clearPublishers() {
/* 152 */     Map lPairs = this.prefs.getAll();
/* 153 */     for (String lKey : lPairs.keySet()) {
/* 154 */       if (lKey.startsWith("publisher.")) {
/* 155 */         this.prefsEditor.remove(lKey);
/*     */       }
/*     */     }
/* 158 */     commit();
/*     */   }
/*     */ 
/*     */   public synchronized String getUUID()
/*     */   {
/* 163 */     String lUUID = this.prefs.getString("uuid", null);
/* 164 */     if (lUUID == null) {
/* 165 */       lUUID = createUUID();
/* 166 */       long time = createInstalledAt();
/*     */ 
/* 168 */       this.prefsEditor.putString("uuid", lUUID);
/* 169 */       this.prefsEditor.putLong("installed_at", time);
/* 170 */       commit();
/*     */     }
/* 172 */     return lUUID;
/*     */   }
/*     */ 
/*     */   private String createUUID()
/*     */   {
/* 177 */     String lUUID = UUID.randomUUID().toString();
/*     */ 
/* 179 */     return lUUID;
/*     */   }
/*     */ 
/*     */   public long getInstalledAt() {
/* 183 */     long time = this.prefs.getLong("installed_at", -1L);
/* 184 */     if (time == -1L) {
/* 185 */       if (this.prefs.getString("uuid", null) == null) {
/* 186 */         String lUUID = createUUID();
/* 187 */         this.prefsEditor.putString("uuid", lUUID);
/*     */       }
/*     */ 
/* 190 */       time = createInstalledAt();
/* 191 */       this.prefsEditor.putLong("installed_at", time);
/* 192 */       commit();
/*     */     }
/* 194 */     return time;
/*     */   }
/*     */ 
/*     */   private long createInstalledAt()
/*     */   {
/* 199 */     long timestamp = System.currentTimeMillis() / 1000L;
/*     */ 
/* 201 */     return timestamp;
/*     */   }
/*     */ 
/*     */   public void setRegistrationKey(String aKey) {
/* 205 */     this.prefsEditor.putString("registration_key", aKey);
/*     */   }
/*     */ 
/*     */   public String getRegistrationKey() {
/* 209 */     return this.prefs.getString("registration_key", null);
/*     */   }
/*     */ 
/*     */   public void setProjectId(String aProjectId) {
/* 213 */     this.prefsEditor.putString("project_id", aProjectId);
/*     */   }
/*     */ 
/*     */   public String getProjectId() {
/* 217 */     return this.prefs.getString("project_id", null);
/*     */   }
/*     */ 
/*     */   public String getEID() {
/* 221 */     return this.prefs.getString("pushio_eid", null);
/*     */   }
/*     */ 
/*     */   public void setEID(String aEID) {
/* 225 */     this.prefsEditor.putString("pushio_eid", aEID);
/*     */   }
/*     */ 
/*     */   public void resetEID() {
/* 229 */     this.prefsEditor.remove("pushio_eid");
/* 230 */     commit();
/*     */   }
/*     */ 
/*     */   public void setUserId(String userId) {
/* 234 */     this.prefsEditor.putString("user_id", userId);
/*     */   }
/*     */ 
/*     */   public String getUserId() {
/* 238 */     return this.prefs.getString("user_id", null);
/*     */   }
/*     */ 
/*     */   public void setNotificationService(String notificationService) {
/* 242 */     this.prefsEditor.putString("notification_service", notificationService);
/*     */   }
/*     */ 
/*     */   public String getNotificationService() {
/* 246 */     return this.prefs.getString("notification_service", null);
/*     */   }
/*     */ 
/*     */   public long getBackoffTime() {
/* 250 */     return this.prefs.getLong("retry_backoff_time", 0L);
/*     */   }
/*     */ 
/*     */   public void setBackoffTime(long backoffTime) {
/* 254 */     this.prefsEditor.putLong("retry_backoff_time", backoffTime);
/*     */   }
/*     */ 
/*     */   public int getLastVersion() {
/* 258 */     return this.prefs.getInt("last_version", 0);
/*     */   }
/*     */ 
/*     */   public void setLastVersion(int lastVersion) {
/* 262 */     this.prefsEditor.putInt("last_version", lastVersion);
/*     */   }
/*     */ 
/*     */   public void setIsBroadcastRegistered(boolean isBroadcastRegistered) {
/* 266 */     this.prefsEditor.putBoolean("broadcast_registered_key", isBroadcastRegistered);
/*     */   }
/*     */ 
/*     */   public boolean getIsBroadcastRegistered() {
/* 270 */     return this.prefs.getBoolean("broadcast_registered_key", false);
/*     */   }
/*     */ 
/*     */   public void setLastUpdateTime(long time) {
/* 274 */     this.prefsEditor.putLong("last_update", time);
/*     */   }
/*     */ 
/*     */   public long getLastUpdateTime() {
/* 278 */     return this.prefs.getLong("last_update", 0L);
/*     */   }
/*     */ 
/*     */   public void setSmallDefaultIcon(int res) {
/* 282 */     this.prefsEditor.putInt("small_icon_res", res);
/*     */   }
/*     */ 
/*     */   public int getSmallDefaultIcon() {
/* 286 */     return this.prefs.getInt("small_icon_res", 0);
/*     */   }
/*     */ 
/*     */   public void setLargeDefaultIcon(int res) {
/* 290 */     this.prefsEditor.putInt("large_icon_res", res);
/*     */   }
/*     */ 
/*     */   public int getLargeDefaultIcon() {
/* 294 */     return this.prefs.getInt("large_icon_res", 0);
/*     */   }
/*     */ 
/*     */   public void setNotificationsStacked(boolean notificationsStacked) {
/* 298 */     this.prefsEditor.putBoolean("stack_notifications", notificationsStacked);
/*     */   }
/*     */ 
/*     */   public boolean getIsNotificationsStacked() {
/* 302 */     return this.prefs.getBoolean("stack_notifications", true);
/*     */   }
/*     */ 
/*     */   public int getAndIncrementNotificationCount() {
/* 306 */     int count = this.prefs.getInt("notification_count", 0);
/*     */ 
/* 308 */     this.prefsEditor.putInt("notification_count", ++count);
/* 309 */     commit();
/*     */ 
/* 311 */     return count;
/*     */   }
/*     */ 
/*     */   public void commit() {
/* 315 */     if (Build.VERSION.SDK_INT >= 9)
/* 316 */       this.prefsEditor.apply();
/*     */     else
/* 318 */       this.prefsEditor.commit();
/*     */   }
/*     */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.PushIOSharedPrefs
 * JD-Core Version:    0.6.2
 */