/*     */ package com.pushio.manager;
/*     */ 
/*     */ import android.content.Context;
/*     */ import android.content.res.AssetManager;
/*     */ import android.text.TextUtils;
/*     */ import android.util.Log;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.apache.http.message.BasicNameValuePair;
/*     */ import org.json.JSONArray;
/*     */ import org.json.JSONException;
/*     */ import org.json.JSONObject;
/*     */ 
/*     */ public class PushIOConfig
/*     */ {
/*  20 */   public static int SCHEME_HTTP = 1;
/*  21 */   public static int SCHEME_HTTPS = 2;
/*     */ 
/*  23 */   private static String JSON_KEY_PUSHIO = "pushio";
/*  24 */   private static String JSON_KEY_GOOGLE = "google";
/*     */ 
/*  26 */   private static String JSON_KEY_API_HOST = "apiHost";
/*  27 */   private static String JSON_KEY_ENGAGEMENT_HOST = "engagementHost";
/*  28 */   private static String JSON_KEY_API_KEY = "apiKey";
/*  29 */   private static String JSON_KEY_TRACKERS = "trackers";
/*  30 */   private static String JSON_KEY_PROJECTID = "projectId";
/*  31 */   private static String JSON_KEY_SCHEME = "scheme";
/*     */   private String mPushIOKey = "dNsLs9cn8Q_gaZU";
/*     */   private String mPushIOApiHost;
/*     */   private String mPushIOEngagementHost;
/*     */   private String mNotificationServiceProjectId = "736051551286";
/*  37 */   private int mScheme = SCHEME_HTTPS;
/*     */   private List<BasicNameValuePair> mPublisherList;
/*     */ 
/*     */   public String getPushIOApiHost()
/*     */   {
/*  43 */     return this.mPushIOApiHost;
/*     */   }
/*     */ 
/*     */   public String getPushIOKey() {
/*  47 */     return this.mPushIOKey;
/*     */   }
/*     */ 
/*     */   public void overwriteApiKey(String apiKey) {
/*  51 */     this.mPushIOKey = apiKey;
/*     */   }
/*     */ 
/*     */   public String getNotificationServiceProjectId() {
/*  55 */     return this.mNotificationServiceProjectId;
/*     */   }
/*     */ 
/*     */   public String getPushIOEngagementHost() {
/*  59 */     return this.mPushIOEngagementHost;
/*     */   }
/*     */ 
/*     */   public List<BasicNameValuePair> getPublisherList() {
/*  63 */     return this.mPublisherList;
/*     */   }
/*     */ 
/*     */   public int getScheme() {
/*  67 */     return this.mScheme;
/*     */   }
/*     */ 
/*     */   public PushIOConfig()
/*     */   {
/*  72 */     this.mPublisherList = new ArrayList();
/*     */   }
/*     */ 
/*     */   public static PushIOConfig createPushIOConfig(JSONObject aJSONObject, String aNotificationService) {
/*  76 */     PushIOConfig lPushIOConfig = new PushIOConfig();
/*     */     try
/*     */     {
/*  79 */       JSONObject lPushIOJSON = aJSONObject.getJSONObject(JSON_KEY_PUSHIO);
/*     */ 
/*  81 */       if (lPushIOJSON.has(JSON_KEY_SCHEME)) {
/*  82 */         String scheme = lPushIOJSON.getString(JSON_KEY_SCHEME);
/*  83 */         if (scheme.equals("http"))
/*  84 */           lPushIOConfig.mScheme = SCHEME_HTTP;
/*     */         else {
/*  86 */           lPushIOConfig.mScheme = SCHEME_HTTPS;
/*     */         }
/*     */       }
/*  89 */       lPushIOConfig.mPushIOApiHost = lPushIOJSON.getString(JSON_KEY_API_HOST);
/*  90 */       lPushIOConfig.mPushIOKey = lPushIOJSON.getString(JSON_KEY_API_KEY);
/*  91 */       lPushIOConfig.mPushIOEngagementHost = lPushIOJSON.optString(JSON_KEY_ENGAGEMENT_HOST, null);
/*     */ 
/*  95 */       JSONObject lTrackersJSON = lPushIOJSON.optJSONObject("trackers");
/*  96 */       if (lTrackersJSON != null)
/*     */       {
/*  99 */         for (int i = 0; i < lTrackersJSON.length(); i++) {
/* 100 */           String lPublisherKey = lTrackersJSON.names().getString(i);
/* 101 */           String lPublisherValue = lTrackersJSON.getString(lPublisherKey);
/*     */ 
/* 103 */           lPushIOConfig.mPublisherList.add(new BasicNameValuePair(lPublisherKey, lPublisherValue));
/*     */         }
/*     */       }
/*     */ 
/* 107 */       if ("GCM".equals(aNotificationService)) {
/* 108 */         JSONObject lServiceJSON = aJSONObject.getJSONObject(JSON_KEY_GOOGLE);
/* 109 */         lPushIOConfig.mNotificationServiceProjectId = lServiceJSON.getString(JSON_KEY_PROJECTID);
/*     */       }
/*     */     }
/*     */     catch (JSONException e) {
/* 113 */       Log.e("PUSHIOCONFIG", e.getMessage());
/*     */     }
/*     */ 
/* 116 */     return lPushIOConfig;
/*     */   }
/*     */ 
/*     */   public static PushIOConfig createPushIOConfig(JSONObject aJSONObject, String aNotificationService, String aApiKey) {
/* 120 */     PushIOConfig lPushIOConfig = createPushIOConfig(aJSONObject, aNotificationService);
/*     */ 
/* 122 */     lPushIOConfig.mNotificationServiceProjectId = aApiKey;
/*     */ 
/* 124 */     return lPushIOConfig;
/*     */   }
/*     */ 
/*     */   public static PushIOConfig createPushIOConfigFromAssets(Context aContext, String aNotificationService) {
/* 128 */     PushIOConfig lPushIOCOnfig = null;
/* 129 */     String lConfigStr = null;
/*     */ 
/* 131 */     boolean isGCM = "GCM".equals(aNotificationService);
/* 132 */     String configAsset = isGCM ? "pushio_config.json" : "pushio_config_adm.json";
/*     */     try
/*     */     {
/* 137 */       InputStream fileStream = aContext.getAssets().open(configAsset);
/*     */ 
/* 139 */       InputStreamReader streamReader = new InputStreamReader(fileStream);
/* 140 */       BufferedReader reader = new BufferedReader(streamReader);
/* 141 */       String json = "";
/*     */       String line;
/* 143 */       while ((line = reader.readLine()) != null) {
/* 144 */         json = json + line + "\n";
/*     */       }
/*     */ 
/* 147 */       lConfigStr = json;
/*     */ 
/* 150 */       JSONObject lJsonConfig = null;
/*     */ 
/* 152 */       if (false == TextUtils.isEmpty(lConfigStr))
/*     */         try {
/* 154 */           lJsonConfig = new JSONObject(lConfigStr);
/*     */ 
/* 156 */           if (isGCM) {
/* 157 */             lPushIOCOnfig = createPushIOConfig(lJsonConfig, aNotificationService);
/*     */           } else {
/* 159 */             String lADMKey = getADMKeyFromAssets(aContext);
/* 160 */             lPushIOCOnfig = createPushIOConfig(lJsonConfig, aNotificationService, lADMKey);
/*     */           }
/*     */         } catch (JSONException e) {
/* 163 */           Log.e("pushioconfig", String.format("unable to parse json: \"%s\"", new Object[] { lConfigStr }), e);
/*     */         }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 168 */       Log.d("pushioconfig", 
/* 169 */         String.format("Error reading local %s. Ensure that you have downloaded the %s from manage.push.io, and include it in your application assets.", new Object[] { configAsset, configAsset }), 
/* 169 */         e);
/*     */ 
/* 172 */       if (((e instanceof FileNotFoundException)) && ("ADM".equals(aNotificationService))) {
/* 173 */         Log.d("pushioconfig", "Running on an ADM device with no pushio_config_adm.json file. Attempting to load the GCM config. Pushes on an ADM device will not work without a valid pushio_config_adm.json file.");
/*     */ 
/* 176 */         lPushIOCOnfig = createPushIOConfigFromAssets(aContext, "GCM");
/*     */       }
/*     */     }
/*     */ 
/* 180 */     return lPushIOCOnfig;
/*     */   }
/*     */ 
/*     */   public static String getADMKeyFromAssets(Context aContext) {
/* 184 */     String key = null;
/*     */     try
/*     */     {
/* 187 */       InputStream fileStream = aContext.getAssets().open("api_key.txt");
/*     */ 
/* 189 */       InputStreamReader streamReader = new InputStreamReader(fileStream);
/* 190 */       BufferedReader reader = new BufferedReader(streamReader);
/* 191 */       key = reader.readLine();
/*     */     } catch (IOException e) {
/* 193 */       Log.d("pushioconfig", "Error reading ADM API key. Ensure that you have placed the key in a file at 'assets/api_key.txt' with no spaces or line breaks", e);
/*     */     }
/*     */ 
/* 196 */     return key != null ? key : "";
/*     */   }
/*     */ }

