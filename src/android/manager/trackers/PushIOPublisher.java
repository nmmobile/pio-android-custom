/*     */ package com.pushio.manager.trackers;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ 
/*     */ public class PushIOPublisher
/*     */ {
/*     */   private List<PushIOTracker> mTrackerList;
/*     */   private String mPublisherKey;
/*     */ 
/*     */   public PushIOPublisher(String publisherKey)
/*     */   {
/*  13 */     this.mPublisherKey = publisherKey;
/*  14 */     this.mTrackerList = new ArrayList();
/*     */   }
/*     */ 
/*     */   public PushIOTracker addTracker()
/*     */   {
/*  22 */     PushIOTracker lTracker = new PushIOTracker();
/*  23 */     this.mTrackerList.add(lTracker);
/*  24 */     return lTracker;
/*     */   }
/*     */ 
/*     */   public void removeTracker(List<String> interests, List<String> values)
/*     */   {
/*  33 */     PushIOTracker lTracker = getTracker(interests, values);
/*     */ 
/*  35 */     if (lTracker != null)
/*  36 */       this.mTrackerList.remove(lTracker);
/*     */   }
/*     */ 
/*     */   public void removeTracker(String interest, String value)
/*     */   {
/*  47 */     PushIOTracker lTracker = getTracker(interest, value);
/*     */ 
/*  49 */     if (lTracker != null)
/*  50 */       this.mTrackerList.remove(lTracker);
/*     */   }
/*     */ 
/*     */   public List<PushIOTracker> getTrackerList()
/*     */   {
/*  59 */     return this.mTrackerList;
/*     */   }
/*     */ 
/*     */   public String getPublisherKey() {
/*  63 */     return this.mPublisherKey;
/*     */   }
/*     */ 
/*     */   public void removeDuplicates() {
/*  67 */     boolean lRemoveCurrent = false;
/*     */ 
/*  69 */     for (Iterator i = this.mTrackerList.iterator(); i.hasNext(); ) {
/*  70 */       PushIOTracker lTracker = (PushIOTracker)i.next();
/*  71 */       lRemoveCurrent = false;
/*  72 */       for (PushIOTracker lCompareTracker : this.mTrackerList) {
/*  73 */         if ((lTracker != lCompareTracker) && 
/*  74 */           (lTracker
/*  74 */           .getInterestList().toString().contentEquals(lCompareTracker.getInterestList().toString())) && 
/*  75 */           (lTracker
/*  75 */           .getValueList().toString().contentEquals(lCompareTracker.getValueList().toString()))) {
/*  76 */           lRemoveCurrent = true;
/*     */         }
/*     */       }
/*     */ 
/*  80 */       if (lRemoveCurrent)
/*  81 */         i.remove();
/*     */     }
/*     */   }
/*     */ 
/*     */   public List<PushIOTracker> getTrackersForInterests(List<String> interests)
/*     */   {
/*  92 */     List lTrackerList = new ArrayList();
/*     */ 
/*  94 */     for (PushIOTracker lTracker : this.mTrackerList) {
/*  95 */       if ((lTracker.getInterestList().containsAll(interests)) && (lTracker.getInterestList().size() == interests.size())) {
/*  96 */         lTrackerList.add(lTracker);
/*     */       }
/*     */     }
/*     */ 
/* 100 */     return lTrackerList;
/*     */   }
/*     */ 
/*     */   public PushIOTracker getTracker(List<String> interests, List<String> values)
/*     */   {
/* 110 */     PushIOTracker lMatchingTracker = null;
/*     */ 
/* 112 */     for (PushIOTracker lTracker : this.mTrackerList) {
/* 113 */       if ((lTracker.getInterestList().containsAll(interests)) && (lTracker.getInterestList().size() == interests.size()) && 
/* 114 */         (lTracker
/* 114 */         .getValueList().containsAll(values)) && (lTracker.getValueList().size() == values.size())) {
/* 115 */         lMatchingTracker = lTracker;
/*     */       }
/*     */     }
/*     */ 
/* 119 */     return lMatchingTracker;
/*     */   }
/*     */ 
/*     */   public PushIOTracker getTracker(String interest, String value)
/*     */   {
/* 129 */     List lInterestList = new ArrayList();
/* 130 */     lInterestList.add(interest);
/*     */ 
/* 132 */     List lValueList = new ArrayList();
/* 133 */     lValueList.add(value);
/*     */ 
/* 135 */     return getTracker(lInterestList, lValueList);
/*     */   }
/*     */ }

/* Location:           /Users/nroney/Desktop/PushIOManager/
 * Qualified Name:     com.pushio.manager.trackers.PushIOPublisher
 * JD-Core Version:    0.6.2
 */