/*    */ package com.pushio.manager;
/*    */ 
/*    */ import android.content.BroadcastReceiver;
/*    */ import android.content.Context;
/*    */ import android.content.Intent;
/*    */ 
/*    */ public class PushIOADMBroadcastReceiver extends BroadcastReceiver
/*    */ {
/*    */   public void onReceive(Context context, Intent intent)
/*    */   {
/* 12 */     PushIOGCMIntentService.runIntentInService(context, intent);
/* 13 */     setResult(-1, null, null);
/*    */   }
/*    */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.PushIOADMBroadcastReceiver
 * JD-Core Version:    0.6.2
 */