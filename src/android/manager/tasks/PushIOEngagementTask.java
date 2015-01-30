/*     */ package com.pushio.manager.tasks;
/*     */ 
/*     */ import android.content.Context;
/*     */ import android.util.Log;
/*     */ import com.pushio.manager.PushIOConfig;
/*     */ import com.pushio.manager.PushIOManager;
/*     */ import com.pushio.manager.PushIOSharedPrefs;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.net.URL;
/*     */ import javax.net.ssl.HttpsURLConnection;
/*     */ 
/*     */ public class PushIOEngagementTask extends PushIOTask<Void, Void, Integer>
/*     */ {
/*  15 */   private final String TAG = PushIOEngagementTask.class.getSimpleName();
/*     */   PushIOConfig mPushIOConfig;
/*     */   PushIOEngagementListener mListener;
/*     */   PushIOSharedPrefs mSharedPrefs;
/*     */   String mResponse;
/*     */   int mMetric;
/*     */   String mCustomMetric;
/*  23 */   String mEngagementId = null;
/*     */ 
/*     */   public PushIOEngagementTask(Context context, PushIOEngagementListener listener, PushIOConfig config, PushIOSharedPrefs sharedPrefs, int metric, String customMetric) {
/*  26 */     super(context);
/*  27 */     this.mPushIOConfig = config;
/*  28 */     this.mListener = listener;
/*  29 */     this.mSharedPrefs = sharedPrefs;
/*  30 */     this.mMetric = metric;
/*  31 */     this.mCustomMetric = customMetric;
/*     */   }
/*     */ 
/*     */   public void setEngagementId(String engagementId) {
/*  35 */     this.mEngagementId = engagementId;
/*     */   }
/*     */ 
/*     */   protected Integer doInBackground(Void[] voids)
/*     */   {
/*  40 */     return trackEngagement();
/*     */   }
/*     */ 
/*     */   public Integer trackEngagement()
/*     */   {
/*     */     try
/*     */     {
/*     */       String lUrlString;
/*  46 */       if (this.mPushIOConfig.getPushIOEngagementHost() != null) {
/*  47 */         lUrlString = "https://" + this.mPushIOConfig.getPushIOEngagementHost();
/*     */       }
/*     */       else {
/*  50 */         lUrlString = "https://" + this.mPushIOConfig.getPushIOApiHost();
/*     */       }
/*     */ 
/*  53 */       String lUrlString = lUrlString + "/e/" + this.mPushIOConfig.getPushIOKey();
/*  54 */       URL lUrl = new URL(lUrlString);
/*  55 */       HttpsURLConnection lConnection = (HttpsURLConnection)lUrl.openConnection();
/*     */ 
/*  57 */       lConnection.setDoInput(true);
/*  58 */       lConnection.setDoOutput(true);
/*  59 */       lConnection.setRequestMethod("POST");
/*     */ 
/*  62 */       OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter(lConnection.getOutputStream());
/*  63 */       StringBuffer lRequest = new StringBuffer();
/*     */ 
/*  65 */       lRequest.append(String.format("di=%s&", new Object[] { this.mSharedPrefs.getUUID() }));
/*     */ 
/*  67 */       if ((this.mMetric == PushIOManager.PUSHIO_ENGAGEMENT_METRIC_OTHER) && (this.mCustomMetric != null))
/*  68 */         lRequest.append(String.format("m=%s", new Object[] { this.mCustomMetric }));
/*     */       else {
/*  70 */         lRequest.append(String.format("m=%s", new Object[] { metricToString(this.mMetric) }));
/*     */       }
/*     */ 
/*  73 */       if (this.mEngagementId != null)
/*  74 */         lRequest.append(String.format("&ei=%s", new Object[] { this.mEngagementId }));
/*  75 */       else if (this.mSharedPrefs.getEID() != null) {
/*  76 */         lRequest.append(String.format("&ei=%s", new Object[] { this.mSharedPrefs.getEID() }));
/*     */       }
/*     */ 
/*  79 */       String lRequestString = lRequest.toString();
/*  80 */       Log.d("pushio", "Engagement: Request String (post): " + lRequestString);
/*  81 */       lOutputStreamWriter.write(lRequestString);
/*  82 */       lOutputStreamWriter.close();
/*     */ 
/*  84 */       int lResponseCode = lConnection.getResponseCode();
/*  85 */       this.mResponse = lConnection.getResponseMessage();
/*     */ 
/*  87 */       lConnection.disconnect();
/*     */ 
/*  89 */       return Integer.valueOf(lResponseCode);
/*     */     }
/*     */     catch (Exception e) {
/*  92 */       Log.e(this.TAG, "Failed to track engagement. Exception message: " + e.getMessage());
/*  93 */       this.mResponse = e.getMessage();
/*     */     }
/*     */ 
/*  96 */     return null;
/*     */   }
/*     */ 
/*     */   private String metricToString(int metric)
/*     */   {
/* 101 */     if (metric == PushIOManager.PUSHIO_ENGAGEMENT_METRIC_LAUNCH)
/* 102 */       return "launch";
/* 103 */     if (metric == PushIOManager.PUSHIO_ENGAGEMENT_METRIC_ACTIVE_SESSION)
/* 104 */       return "active";
/* 105 */     if (metric == PushIOManager.PUSHIO_ENGAGEMENT_METRIC_INAPP_PURCHASE)
/* 106 */       return "iap";
/* 107 */     if (metric == PushIOManager.PUSHIO_ENGAGEMENT_METRIC_PREMIUM_CONTENT)
/* 108 */       return "premium";
/* 109 */     if (metric == PushIOManager.PUSHIO_ENGAGEMENT_METRIC_SOCIAL)
/* 110 */       return "social";
/* 111 */     if (metric == PushIOManager.PUSHIO_ENGAGEMENT_METRIC_OTHER) {
/* 112 */       return "other";
/*     */     }
/* 114 */     return null;
/*     */   }
/*     */ 
/*     */   protected void onPostExecute(Integer aResponseCode)
/*     */   {
/* 121 */     if (aResponseCode == null) {
/* 122 */       this.mListener.onEngagementError("Network error: " + this.mResponse);
/*     */     }
/* 124 */     else if (aResponseCode.intValue() == 202) {
/* 125 */       this.mListener.onEngagementSuccess();
/* 126 */     } else if (aResponseCode.intValue() == 406) {
/* 127 */       Log.e("pushio", "push.io engagement error. Invalid Request. Error=" + this.mResponse);
/* 128 */       this.mListener.onEngagementError("push.io registration error. Invalid Request. Error=" + this.mResponse);
/* 129 */     } else if (aResponseCode.intValue() == 502) {
/* 130 */       Log.e("pushio", "push.io engagement error. Error persisting the request in the temporary store.");
/* 131 */       this.mListener.onEngagementError("push.io engagement error. Error persisting the request in the temporary store.");
/*     */     } else {
/* 133 */       this.mListener.onEngagementError("unknown response. code=" + aResponseCode);
/*     */     }
/*     */   }
/*     */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.tasks.PushIOEngagementTask
 * JD-Core Version:    0.6.2
 */