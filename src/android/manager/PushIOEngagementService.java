/*    */ package com.pushio.manager;
/*    */ 
/*    */ import android.app.IntentService;
/*    */ import android.content.Context;
/*    */ import android.content.Intent;
/*    */ import android.os.PowerManager;
/*    */ import android.os.PowerManager.WakeLock;
/*    */ import android.util.Log;
/*    */ 
/*    */ public class PushIOEngagementService extends IntentService
/*    */ {
/*    */   private static PowerManager.WakeLock sWakeLock;
/* 13 */   private static final Object LOCK = PushIOEngagementService.class;
/*    */ 
/* 15 */   public static String PUSHIO_ENGAGEMENT_TYPE = "pushio_engagement_type";
/* 16 */   public static String PUSHIO_ENGAGEMENT_ID = "engagement_id";
/*    */ 
/*    */   public PushIOEngagementService() {
/* 19 */     super("PushIOEngagementService  ");
/*    */   }
/*    */ 
/*    */   static void runIntentInService(Context context, Intent intent) {
/* 23 */     synchronized (LOCK) {
/* 24 */       if (sWakeLock == null) {
/* 25 */         PowerManager pm = (PowerManager)context.getSystemService("power");
/* 26 */         sWakeLock = pm.newWakeLock(1, "PushIOEngagementServiceLock");
/*    */       }
/*    */     }
/* 29 */     sWakeLock.acquire();
/* 30 */     Log.d("pushio", "Acquired wake lock");
/* 31 */     intent.setClassName(context, PushIOEngagementService.class.getName());
/* 32 */     context.startService(intent);
/*    */   }
/*    */ 
/*    */   protected void onHandleIntent(Intent intent)
/*    */   {
/*    */     try {
/* 38 */       int lEngagementType = intent.getIntExtra(PUSHIO_ENGAGEMENT_TYPE, PushIOManager.PUSHIO_ENGAGEMENT_METRIC_LAUNCH);
/* 39 */       String engagementId = intent.getStringExtra(PUSHIO_ENGAGEMENT_ID);
/* 40 */       PushIOManager lPushIOManager = PushIOManager.getInstance(this);
/* 41 */       lPushIOManager.trackEngagementSynchronous(lEngagementType, engagementId);
/*    */     } catch (Exception e) {
/*    */     }
/*    */     finally {
/* 45 */       synchronized (LOCK) {
/* 46 */         Log.d("pushio", "Releasing Wake Lock");
/* 47 */         sWakeLock.release();
/*    */       }
/*    */     }
/*    */   }
/*    */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.PushIOEngagementService
 * JD-Core Version:    0.6.2
 */