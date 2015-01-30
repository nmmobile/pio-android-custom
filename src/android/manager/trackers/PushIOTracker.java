/*    */ package com.pushio.manager.trackers;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ public class PushIOTracker
/*    */ {
/*    */   private ArrayList<String> mInterests;
/*    */   private ArrayList<String> mValues;
/*    */ 
/*    */   public PushIOTracker()
/*    */   {
/* 12 */     this.mInterests = new ArrayList();
/* 13 */     this.mValues = new ArrayList();
/*    */   }
/*    */ 
/*    */   public PushIOTracker addInterest(String interest) {
/* 17 */     this.mInterests.add(interest);
/* 18 */     return this;
/*    */   }
/*    */ 
/*    */   public PushIOTracker addValue(String value) {
/* 22 */     this.mValues.add(value);
/* 23 */     return this;
/*    */   }
/*    */ 
/*    */   public void removeInterest(String interest) {
/* 27 */     this.mInterests.remove(interest);
/*    */   }
/*    */ 
/*    */   public void removeValue(String value) {
/* 31 */     this.mValues.remove(value);
/*    */   }
/*    */ 
/*    */   public List<String> getInterestList()
/*    */   {
/* 36 */     return this.mInterests;
/*    */   }
/*    */ 
/*    */   public List<String> getValueList() {
/* 40 */     return this.mValues;
/*    */   }
/*    */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.trackers.PushIOTracker
 * JD-Core Version:    0.6.2
 */