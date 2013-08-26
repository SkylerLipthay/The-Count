package com.skylerlipthay.thecount;

import java.math.BigInteger;

// I can't believe I'm actually making this class
public class Counter {
  final private BigInteger one = new BigInteger("1");
  private BigInteger count;
  
  public Counter() {
    reset();
  }
  
  public BigInteger getCount() {
    return count;
  }
  
  public void increase() {
    count = count.add(one);
  }
  
  public void decrease() {
    count = count.subtract(one);
  }
  
  public void reset() {
    count = new BigInteger("0");
  }
  
  public void set(BigInteger newCount) {
    count = newCount;
  }
}
