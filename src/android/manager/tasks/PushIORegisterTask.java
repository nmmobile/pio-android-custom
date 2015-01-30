/*     */ package com.pushio.manager.tasks;
/*     */ 
/*     */ import android.content.Context;
/*     */ import android.content.pm.PackageInfo;
/*     */ import android.content.pm.PackageManager;
/*     */ import android.content.res.Resources;
/*     */ import android.location.Criteria;
/*     */ import android.location.Location;
/*     */ import android.location.LocationManager;
/*     */ import android.os.Build;
/*     */ import android.os.Build.VERSION;
/*     */ import android.telephony.TelephonyManager;
/*     */ import android.util.DisplayMetrics;
/*     */ import android.util.Log;
/*     */ import com.pushio.manager.PushIOConfig;
/*     */ import com.pushio.manager.PushIOManager;
/*     */ import com.pushio.manager.PushIOSharedPrefs;
/*     */ import com.pushio.manager.trackers.PushIOPublisher;
/*     */ import com.pushio.manager.trackers.PushIOTracker;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.URL;
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import java.util.TimeZone;
/*     */ import org.apache.http.NameValuePair;
/*     */ 
/*     */ public class PushIORegisterTask extends PushIOTask<Void, Void, Integer>
/*     */ {
/*  26 */   private final String TAG = PushIORegisterTask.class.getSimpleName();
/*     */   List<String> mCategories;
/*     */   List<PushIOPublisher> mPublishers;
/*     */   String mUserId;
/*     */   PushIOConfig mPushIOConfig;
/*     */   PushIOListener mListener;
/*     */   PushIOSharedPrefs mSharedPrefs;
/*     */   String mResponse;
/*  35 */   boolean mIsDelete = false;
/*     */ 
/*     */   public PushIORegisterTask(Context context, PushIOListener listener, List<String> categories, List<PushIOPublisher> publishers, String userId, PushIOConfig config, PushIOSharedPrefs sharedPrefs, boolean isDelete)
/*     */   {
/*  39 */     super(context);
/*  40 */     this.mCategories = categories;
/*  41 */     this.mPublishers = publishers;
/*  42 */     this.mUserId = userId;
/*  43 */     this.mPushIOConfig = config;
/*  44 */     this.mListener = listener;
/*  45 */     this.mSharedPrefs = sharedPrefs;
/*  46 */     this.mIsDelete = isDelete;
/*     */   }
/*     */ 
/*     */   protected Integer doInBackground(Void[] voids)
/*     */   {
/*     */     try
/*     */     {
/*  56 */       int i = 0;
/*  57 */       Thread lCurThread = Thread.currentThread();
/*     */ 
/*  59 */       while ((i < 60) && 
/*  60 */         (this.mSharedPrefs.getRegistrationKey() == null)) {
/*  61 */         Thread.sleep(1000L);
/*  62 */         i++;
/*     */       }
/*     */ 
/*  68 */       if (this.mSharedPrefs.getRegistrationKey() == null) {
/*  69 */         return Integer.valueOf(-1);
/*     */       }
/*     */ 
/*  72 */       StringBuilder sb = new StringBuilder();
/*     */ 
/*  74 */       if (this.mPushIOConfig.getScheme() == PushIOConfig.SCHEME_HTTP)
/*  75 */         sb.append("http://");
/*     */       else {
/*  77 */         sb.append("https://");
/*     */       }
/*     */ 
/*  80 */       sb.append(this.mPushIOConfig.getPushIOApiHost());
/*     */ 
/*  82 */       if (this.mIsDelete)
/*  83 */         sb.append("/d/");
/*     */       else {
/*  85 */         sb.append("/r/");
/*     */       }
/*     */ 
/*  88 */       sb.append(this.mPushIOConfig.getPushIOKey());
/*     */ 
/*  90 */       URL lUrl = new URL(sb.toString());
/*     */ 
/*  92 */       HttpURLConnection lConnection = (HttpURLConnection)lUrl.openConnection();
/*     */ 
/*  96 */       lConnection.setDoInput(true);
/*  97 */       lConnection.setDoOutput(true);
/*  98 */       lConnection.setRequestMethod("POST");
/*     */ 
/* 102 */       OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter(lConnection.getOutputStream());
/* 103 */       StringBuffer lRequest = new StringBuffer();
/*     */ 
/* 106 */       lRequest.append(String.format("dt=%s&", new Object[] { this.mSharedPrefs.getRegistrationKey() }));
/*     */ 
/* 108 */       Locale locale = Locale.US;
/* 109 */       TimeZone timeZone = TimeZone.getDefault();
/*     */ 
/* 112 */       lRequest.append(String.format("ins=%s&", new Object[] { Long.valueOf(this.mSharedPrefs.getInstalledAt()) }));
/* 113 */       lRequest.append(String.format("tz=%s&", new Object[] { timeZone.getID() }));
/* 114 */       lRequest.append(String.format("utc=%s&", new Object[] { Integer.valueOf(timeZone.getOffset(new Date().getTime()) / 1000) }));
/* 115 */       lRequest.append(String.format("appv=%s&", new Object[] { this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0).versionName }));
/* 116 */       lRequest.append(String.format("libv=%s&", new Object[] { PushIOManager.getLibVersion() }));
/*     */ 
/* 118 */       TelephonyManager telephonyManager = (TelephonyManager)this.context.getSystemService("phone");
/* 119 */       lRequest.append(String.format("cr=%s&", new Object[] { telephonyManager.getNetworkOperator() }));
/*     */ 
/* 121 */       lRequest.append(String.format("mf=%s&", new Object[] { Build.MANUFACTURER }));
/* 122 */       lRequest.append(String.format("mod=%s&", new Object[] { Build.MODEL }));
/* 123 */       lRequest.append(String.format("osv=%s&", new Object[] { Integer.valueOf(Build.VERSION.SDK_INT) }));
/* 124 */       lRequest.append(String.format("d=%s&", new Object[] { Float.valueOf(this.context.getResources().getDisplayMetrics().density) }));
/* 125 */       lRequest.append(String.format("w=%s&", new Object[] { Integer.valueOf(this.context.getResources().getDisplayMetrics().widthPixels) }));
/* 126 */       lRequest.append(String.format("h=%s&", new Object[] { Integer.valueOf(this.context.getResources().getDisplayMetrics().heightPixels) }));
/* 127 */       lRequest.append(String.format("l=%s&", new Object[] { Locale.getDefault().toString() }));
/*     */ 
/* 129 */       LocationManager mgr = (LocationManager)this.context.getSystemService("location");
/* 130 */       Criteria criteria = new Criteria();
/* 131 */       criteria.setAccuracy(0);
/*     */ 
/* 133 */       String providerName = mgr.getBestProvider(criteria, true);
/*     */       Location location;
/* 134 */       if (providerName != null) {
/* 135 */         location = mgr.getLastKnownLocation(providerName);
/* 136 */         if (location != null) {
/* 137 */           lRequest.append(String.format("lat=%f&", new Object[] { Double.valueOf(location.getLatitude()) }));
/* 138 */           lRequest.append(String.format("lon=%f&", new Object[] { Double.valueOf(location.getLongitude()) }));
/* 139 */           lRequest.append(String.format("acc=%f&", new Object[] { Float.valueOf(location.getAccuracy()) }));
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 145 */       lRequest.append(String.format("di=%s", new Object[] { this.mSharedPrefs.getUUID() }));
/* 146 */       if (!this.mIsDelete)
/*     */       {
/* 148 */         if (this.mUserId != null) {
/* 149 */           lRequest.append(String.format("&usr=%s", new Object[] { this.mUserId }));
/*     */         }
/*     */ 
/* 153 */         lRequest.append("&c=");
/*     */         String lCategories;
/* 155 */         if (this.mCategories != null) {
/* 156 */           for (location = this.mCategories.iterator(); location.hasNext(); ) { lCategories = (String)location.next();
/* 157 */             lRequest.append(new StringBuilder().append(lCategories).append(",").toString());
/*     */           }
/* 159 */           if (this.mCategories.size() > 0) {
/* 160 */             lRequest.deleteCharAt(lRequest.length() - 1);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 165 */         if (this.mPublishers != null) {
/* 166 */           Boolean lTrackerExists = Boolean.valueOf(false);
/* 167 */           lRequest.append("&tr=");
/* 168 */           lRequest.append("[");
/* 169 */           for (PushIOPublisher lPublisher : this.mPublishers) {
/* 170 */             lPublisherValue = null;
/* 171 */             for (NameValuePair lPair : this.mPushIOConfig.getPublisherList()) {
/* 172 */               if (lPair.getName().contentEquals(lPublisher.getPublisherKey())) {
/* 173 */                 lPublisherValue = lPair.getValue();
/* 174 */                 break;
/*     */               }
/*     */             }
/* 177 */             for (PushIOTracker lTracker : lPublisher.getTrackerList()) {
/* 178 */               lTrackerExists = Boolean.valueOf(true);
/* 179 */               lRequest.append("[");
/* 180 */               lRequest.append(new StringBuilder().append("\"").append(lPublisherValue).append("\",").toString());
/*     */ 
/* 182 */               lRequest.append("[");
/* 183 */               for (String lString : lTracker.getInterestList()) {
/* 184 */                 lRequest.append(new StringBuilder().append("\"").append(lString).append("\",").toString());
/*     */               }
/* 186 */               lRequest.deleteCharAt(lRequest.length() - 1);
/* 187 */               lRequest.append("],");
/*     */ 
/* 189 */               lRequest.append("[");
/* 190 */               for (String lString : lTracker.getValueList()) {
/* 191 */                 lRequest.append(new StringBuilder().append("\"").append(lString).append("\",").toString());
/*     */               }
/* 193 */               lRequest.deleteCharAt(lRequest.length() - 1);
/* 194 */               lRequest.append("]");
/*     */ 
/* 196 */               lRequest.append("],");
/*     */             }
/*     */           }
/*     */           String lPublisherValue;
/* 200 */           if (lTrackerExists.booleanValue()) {
/* 201 */             lRequest.deleteCharAt(lRequest.length() - 1);
/*     */           }
/* 203 */           lRequest.append("]");
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 208 */       String lRequestString = lRequest.toString();
/* 209 */       if (this.mIsDelete)
/* 210 */         Log.d("pushio", new StringBuilder().append("Delete - Request String:").append(lRequestString).toString());
/*     */       else {
/* 212 */         Log.d("pushio", new StringBuilder().append("Register - Request String: ").append(lRequestString).toString());
/*     */       }
/* 214 */       lOutputStreamWriter.write(lRequestString);
/* 215 */       lOutputStreamWriter.close();
/*     */ 
/* 217 */       int lResponseCode = lConnection.getResponseCode();
/* 218 */       this.mResponse = lConnection.getResponseMessage();
/*     */ 
/* 220 */       lConnection.disconnect();
/*     */ 
/* 222 */       return Integer.valueOf(lResponseCode);
/*     */     }
/*     */     catch (Exception e) {
/* 225 */       Log.e(this.TAG, "Failed To Register", e);
/* 226 */       this.mResponse = e.getMessage();
/*     */     }
/*     */ 
/* 229 */     return null;
/*     */   }
/*     */ 
/*     */   protected void onPostExecute(Integer aResponseCode)
/*     */   {
/* 236 */     if (aResponseCode == null) {
/* 237 */       this.mListener.onPushIOError(new StringBuilder().append("Network error: ").append(this.mResponse).toString());
/*     */     }
/* 239 */     else if (aResponseCode.intValue() == 202) {
/* 240 */       this.mSharedPrefs.clearCategories();
/* 241 */       this.mSharedPrefs.setLastUpdateTime(new Date().getTime());
/*     */ 
/* 244 */       if (this.mIsDelete)
/* 245 */         this.mSharedPrefs.setIsBroadcastRegistered(false);
/*     */       else {
/* 247 */         this.mSharedPrefs.setIsBroadcastRegistered(true);
/*     */       }
/*     */ 
/* 250 */       if (this.mCategories != null) {
/* 251 */         for (String lCategory : this.mCategories) {
/* 252 */           this.mSharedPrefs.addCategory(lCategory);
/*     */         }
/*     */       }
/*     */ 
/* 256 */       this.mSharedPrefs.clearPublishers();
/* 257 */       if (this.mPublishers != null) {
/* 258 */         for (PushIOPublisher lPublisher : this.mPublishers) {
/* 259 */           this.mSharedPrefs.addPublisher(lPublisher);
/*     */         }
/*     */       }
/*     */ 
/* 263 */       this.mSharedPrefs.setUserId(this.mUserId);
/*     */ 
/* 265 */       this.mSharedPrefs.commit();
/* 266 */       this.mListener.onPushIOSuccess();
/* 267 */     } else if (aResponseCode.intValue() == 406) {
/* 268 */       Log.e("pushio", new StringBuilder().append("push.io registration error. Invalid Request. Error=").append(this.mResponse).toString());
/* 269 */       this.mListener.onPushIOError(new StringBuilder().append("push.io registration error. Invalid Request. Error=").append(this.mResponse).toString());
/* 270 */     } else if (aResponseCode.intValue() == 502) {
/* 271 */       Log.e("pushio", "push.io registration error. Error persisting the request in the temporary store.");
/* 272 */       this.mListener.onPushIOError("push.io registration error. Error persisting the request in the temporary store.");
/* 273 */     } else if (aResponseCode.intValue() == -1) {
/* 274 */       Log.e("pushio", "Failed to register with GCM");
/* 275 */       this.mListener.onPushIOError("Failed to register with Google Cloud Messaging.");
/*     */     } else {
/* 277 */       this.mListener.onPushIOError(new StringBuilder().append("unknown response. code=").append(aResponseCode).toString());
/*     */     }
/*     */   }
/*     */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.tasks.PushIORegisterTask
 * JD-Core Version:    0.6.2
 */