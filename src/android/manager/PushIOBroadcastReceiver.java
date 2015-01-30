/*    */ package com.pushio.manager;
/*    */ 
/*    */ import android.content.BroadcastReceiver;
/*    */ import android.content.Context;
/*    */ import android.content.Intent;
/*    */ 
/*    */ public class PushIOBroadcastReceiver extends BroadcastReceiver
/*    */ {
/*    */   public final void onReceive(Context context, Intent intent)
/*    */   {
/* 11 */     PushIOGCMIntentService.runIntentInService(context, intent);
/* 12 */     setResult(-1, null, null);
/*    */   }
/*    */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.PushIOBroadcastReceiver
 * JD-Core Version:    0.6.2
 */