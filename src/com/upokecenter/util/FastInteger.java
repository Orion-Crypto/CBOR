package com.upokecenter.util;
/*
Written in 2013 by Peter O.
Any copyright is dedicated to the Public Domain.
http://creativecommons.org/publicdomain/zero/1.0/

If you like this, you should donate to Peter O.
at: http://upokecenter.com/d/
 */


import java.math.*;


  /**
   * A mutable integer class initially backed by a 64-bit integer, that
   * only uses a big integer when arithmetic operations would overflow
   * the 64-bit integer. <p>This class is ideal for cases where operations
   * should be arbitrary precision, but the need to use a precision greater
   * than 64 bits is very rare.</p>
   */
  final class FastInteger implements Comparable<FastInteger> {
    long smallValue;
    BigInteger largeValue;
    boolean usingLarge;
    
    private static BigInteger Int64MinValue=BigInteger.valueOf(Long.MIN_VALUE);
    private static BigInteger Int64MaxValue=BigInteger.valueOf(Long.MAX_VALUE);
    private static BigInteger Int32MinValue=BigInteger.valueOf(Integer.MIN_VALUE);
    private static BigInteger Int32MaxValue=BigInteger.valueOf(Integer.MAX_VALUE);
    
    public FastInteger(){
      smallValue=0;
      usingLarge=false;
    }
    
    public FastInteger(long value){
      smallValue=value;
      usingLarge=false;
    }
    
    public FastInteger(FastInteger value){
      smallValue=value.smallValue;
      usingLarge=value.usingLarge;
      largeValue=value.largeValue;
    }
    
    // This constructor converts a big integer to a 64-bit one
    // if it's small enough
    public FastInteger(BigInteger bigintVal){
      if(bigintVal.compareTo(Int64MinValue)>=0 &&
         bigintVal.compareTo(Int64MaxValue)<=0){
        smallValue=bigintVal.longValue();
        usingLarge=false;
      } else {
        smallValue=0;
        usingLarge=true;
        largeValue=bigintVal;
      }
    }
    
    public int AsInt32() {
      if(usingLarge){
        return largeValue.intValue();
      } else {
        return ((int)smallValue);
      }
    }
    public long AsInt64() {
      if(usingLarge){
        return largeValue.longValue();
      } else {
        return ((long)smallValue);
      }
    }
    public int compareTo(FastInteger val) {
      if(usingLarge || val.usingLarge){
        BigInteger valValue=val.largeValue;
        return largeValue.compareTo(valValue);
      } else {
        return (val.smallValue==smallValue) ? 0 :
          (smallValue<val.smallValue ? -1 : 1);
      }
    }
    public FastInteger Abs() {
      return (this.signum()<0) ? Negate() : this;
    }
    public FastInteger Mod(int divisor) {
      if(usingLarge){
        // Mod operator will always result in a
        // number that fits an int for int divisors
        largeValue=largeValue.remainder(BigInteger.valueOf(divisor));
        smallValue=largeValue.intValue();
        usingLarge=false;
      } else {
        smallValue%=divisor;
      }
      return this;
    }
    
    public int compareTo(long val) {
      if(usingLarge){
        return largeValue.compareTo(BigInteger.valueOf(val));
      } else {
        return (val==smallValue) ? 0 : (smallValue<val ? -1 : 1);
      }
    }
    
    /**
     * Sets this object's value to the current value times another integer.
     * @param val The integer to multiply by.
     * @return This object.
     */
    public FastInteger Multiply(int val) {
      if(val==0){
        smallValue=0;
        usingLarge=false;
      } else if(usingLarge){
        largeValue=largeValue.multiply(BigInteger.valueOf(val));
      } else {
        boolean apos = (smallValue > 0L);
        boolean bpos = (val > 0L);
        if (
          (apos && ((!bpos && (Long.MIN_VALUE / smallValue) > val) ||
                    (bpos && smallValue > (Long.MAX_VALUE / val)))) ||
          (!apos && ((!bpos && smallValue != 0L &&
                      (Long.MAX_VALUE / smallValue) > val) ||
                     (bpos && smallValue < (Long.MIN_VALUE / val))))) {
          // would overflow, convert to large
          largeValue=BigInteger.valueOf(smallValue);
          usingLarge=true;
          largeValue=largeValue.multiply(BigInteger.valueOf(val));
        } else {
          smallValue*=val;
        }
      }
      return this;
    }
    
    /**
     * Sets this object's value to the given integer.
     * @param val The value to set.
     * @return This object.
     */
    public FastInteger Set(int val) {
      usingLarge=false;
      smallValue=val;
      return this;
    }
    
    /**
     * Sets this object's value to 0 minus its current value (reverses its
     * sign).
     * @return This object.
     */
    public FastInteger Negate() {
      if(usingLarge){
        largeValue=(largeValue).negate();
      } else {
        if(smallValue==Long.MIN_VALUE){
          // would overflow, convert to large
          largeValue=BigInteger.valueOf(smallValue);
          usingLarge=true;
          largeValue=(largeValue).negate();
        } else {
          smallValue=-smallValue;
        }
      }
      return this;
    }
    
    /**
     * Sets this object's value to the current value minus the given FastInteger
     * value.
     * @param val The subtrahend.
     * @return This object.
     */
    public FastInteger Subtract(FastInteger val) {
      if(usingLarge || val.usingLarge){
        BigInteger valValue=val.largeValue;
        largeValue=largeValue.subtract(valValue);
      } else if(((long)val.smallValue < 0 && Long.MAX_VALUE + (long)val.smallValue < smallValue) ||
                ((long)val.smallValue > 0 && Long.MIN_VALUE + (long)val.smallValue > smallValue)){
        // would overflow, convert to large
        largeValue=BigInteger.valueOf(smallValue);
        usingLarge=true;
        largeValue=largeValue.subtract(BigInteger.valueOf(val.smallValue));
      } else{
        smallValue-=val.smallValue;
      }
      return this;
    }
    
    /**
     * Sets this object's value to the current value minus the given integer.
     * @param val The subtrahend.
     * @return This object.
     */
    public FastInteger Subtract(int val) {
      if(usingLarge){
        largeValue=largeValue.subtract(BigInteger.valueOf(val));
      } else if(((long)val < 0 && Long.MAX_VALUE + (long)val < smallValue) ||
                ((long)val > 0 && Long.MIN_VALUE + (long)val > smallValue)){
        // would overflow, convert to large
        largeValue=BigInteger.valueOf(smallValue);
        usingLarge=true;
        largeValue=largeValue.subtract(BigInteger.valueOf(val));
      } else{
        smallValue-=val;
      }
      return this;
    }
    
    public FastInteger Add(FastInteger val) {
      if(usingLarge || val.usingLarge){
        BigInteger valValue=val.largeValue;
        largeValue=largeValue.add(valValue);
      } else if((smallValue < 0 && (long)val.smallValue < Long.MIN_VALUE - smallValue) ||
                (smallValue > 0 && (long)val.smallValue > Long.MAX_VALUE - smallValue)){
        // would overflow, convert to large
        largeValue=BigInteger.valueOf(smallValue);
        usingLarge=true;
        largeValue=largeValue.add(BigInteger.valueOf(val.smallValue));
      } else{
        smallValue+=val.smallValue;
      }
      return this;
    }
    
    public FastInteger Divide(int divisor) {
      if(divisor!=0){
        if(usingLarge){
          largeValue=largeValue.divide(BigInteger.valueOf(divisor));
        } else if(divisor==-1 && smallValue==Long.MIN_VALUE){
          // would overflow, convert to large
          largeValue=BigInteger.valueOf(smallValue);
          usingLarge=true;
          largeValue=largeValue.divide(BigInteger.valueOf(divisor));
        } else{
          smallValue/=divisor;
        }
      }
      return this;
    }
    
    public FastInteger Add(int val) {
      if(val!=0){
        if(usingLarge){
          largeValue=largeValue.add(BigInteger.valueOf(val));
        } else if((smallValue < 0 && (long)val < Long.MIN_VALUE - smallValue) ||
                  (smallValue > 0 && (long)val > Long.MAX_VALUE - smallValue)){
          // would overflow, convert to large
          largeValue=BigInteger.valueOf(smallValue);
          usingLarge=true;
          largeValue=largeValue.add(BigInteger.valueOf(val));
        } else{
          smallValue+=val;
        }
      }
      return this;
    }
    public FastInteger Add(long val) {
      if(val!=0){
        if(usingLarge){
          largeValue=largeValue.add(BigInteger.valueOf(val));
        } else if((smallValue < 0 && val < Long.MIN_VALUE - smallValue) ||
                  (smallValue > 0 && val > Long.MAX_VALUE - smallValue)){
          // would overflow, convert to large
          largeValue=BigInteger.valueOf(smallValue);
          usingLarge=true;
          largeValue=largeValue.add(BigInteger.valueOf(val));
        } else{
          smallValue+=val;
        }
      }
      return this;
    }
    
    public boolean CanFitInInt64() {
      if(usingLarge){
        return (largeValue.compareTo(Int64MinValue)>=0 &&
                largeValue.compareTo(Int64MaxValue)<=0);
      } else {
        return true;
      }
    }
    
    public boolean CanFitInInt32() {
      if(usingLarge){
        return (largeValue.compareTo(Int32MinValue)>=0 &&
                largeValue.compareTo(Int32MaxValue)<=0);
      } else {
        return (smallValue>=Integer.MIN_VALUE &&
                smallValue<=Integer.MAX_VALUE);
      }
    }

    public int signum() {
        return usingLarge ? largeValue.signum() : (
          (smallValue==0) ? 0 : (smallValue<0 ? -1 : 1));
      }
    
    public BigInteger AsBigInteger() {
      return usingLarge ? largeValue : BigInteger.valueOf(smallValue);
    }
  }
