/*    */ package com.pushio.manager.tasks;
/*    */ 
/*    */ import android.content.Context;
/*    */ import android.os.AsyncTask;
/*    */ import android.os.Build.VERSION;
/*    */ import com.pushio.manager.PushIOSharedPrefs;
/*    */ 
/*    */ public abstract class PushIOTask<A, B, C> extends AsyncTask<A, B, C>
/*    */ {
/*    */   protected static final int STATUS_CODE_NOT_READY = -1;
/*    */   protected static final int STATUS_CODE_SUCCESS = 202;
/*    */   protected static final int STATUS_CODE_INVALID_REQUEST = 406;
/*    */   protected static final int STATUS_CODE_ERROR = 502;
/*    */   protected Context context;
/*    */   protected PushIOSharedPrefs prefs;
/*    */ 
/*    */   public void executeCompat(A[] params)
/*    */   {
/* 19 */     if (Build.VERSION.SDK_INT >= 11)
/* 20 */       executeOnExecutor(THREAD_POOL_EXECUTOR, params);
/*    */     else
/* 22 */       execute(params);
/*    */   }
/*    */ 
/*    */   public PushIOTask(Context context)
/*    */   {
/* 27 */     this.context = context;
/* 28 */     this.prefs = new PushIOSharedPrefs(context);
/*    */   }
/*    */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.tasks.PushIOTask
 * JD-Core Version:    0.6.2
 */