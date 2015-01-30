/*    */ package com.pushio.manager;
/*    */ 
/*    */ import android.app.Activity;
/*    */ import android.content.Intent;
/*    */ import android.os.Bundle;
/*    */ 
/*    */ public class PushIOActivityLauncher extends Activity
/*    */ {
/*    */   public void onCreate(Bundle savedInstanceState)
/*    */   {
/* 14 */     super.onCreate(savedInstanceState);
/* 15 */     Intent intent = getIntent();
/* 16 */     if (intent.hasExtra(PushIOManager.PUSHIO_ENGAGEMENTID_KEY)) {
/* 17 */       PushIOSharedPrefs lSharedPrefs = new PushIOSharedPrefs(this);
/* 18 */       lSharedPrefs.setEID(intent.getStringExtra(PushIOManager.PUSHIO_ENGAGEMENTID_KEY));
/* 19 */       lSharedPrefs.commit();
/*    */ 
/* 21 */       intent.putExtra(PushIOEngagementService.PUSHIO_ENGAGEMENT_TYPE, PushIOManager.PUSHIO_ENGAGEMENT_METRIC_LAUNCH);
/* 22 */       intent.putExtra(PushIOEngagementService.PUSHIO_ENGAGEMENT_ID, intent.getStringExtra(PushIOManager.PUSHIO_ENGAGEMENTID_KEY));
/* 23 */       PushIOEngagementService.runIntentInService(this, intent);
/*    */     }
/*    */ 
/* 26 */     Intent lNewIntent = new Intent(getPackageName() + ".NOTIFICATIONPRESSED");
/* 27 */     lNewIntent.putExtras(intent);
/* 28 */     lNewIntent.setFlags(335544320);
/*    */ 
/* 30 */     startActivity(lNewIntent);
/* 31 */     finish();
/*    */   }
/*    */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.PushIOActivityLauncher
 * JD-Core Version:    0.6.2
 */