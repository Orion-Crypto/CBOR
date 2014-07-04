/*
Written by Peter O. in 2013.
Any copyright is dedicated to the Public Domain.
http://creativecommons.org/publicdomain/zero/1.0/
If you like this, you should donate to Peter O.
at: http://upokecenter.com/d/
 */
using System;

namespace Test {
    /// <summary>The system&apos;s random number generator will be called many times
    /// during testing. Unfortunately it can be very slow. So we use this wrapper
    /// class.</summary>
  public class FastRandom
  {
    private const int ReseedCount = 10000;

    private Random rand;
    private int count;

    private int w = 521288629;
    private int z = 362436069;

    public FastRandom() {
      this.rand = new Random();
      this.count = ReseedCount;
    }

    private int NextValueInternal() {
      int w = this.w, z = this.z;
      // Use George Marsaglia's multiply-with-carry
      // algorithm.
      this.z = z = unchecked((36969 * (z & 65535)) + ((z >> 16) & 0xffff));
      this.w = w = unchecked((18000 * (w & 65535)) + ((z >> 16) & 0xffff));
      return ((z << 16) | (w & 65535)) & 0x7fffffff;
    }

    /// <summary>Generates a random number.</summary>
    /// <param name='v'>The return value will be 0 or greater, and less than this
    /// number.</param>
    /// <returns>A 32-bit signed integer.</returns>
    public int NextValue(int v) {
      if (v <= 0) {
        throw new ArgumentException(
"v (" + v + ") is not greater than " + "0");
      }
      if (v <= 1) {
        return 0;
      }
      if (this.count >= ReseedCount) {
        // Call the default random number generator
        // every once in a while, to reseed
        this.count = 0;
        if (this.rand != null) {
          int seed = this.rand.Next(0x10000);
          seed |= this.rand.Next(0x10000) << 16;
          this.z ^= seed;
          return this.rand.Next(v);
        }
      }
      ++this.count;
      int maxExclusive = (Int32.MaxValue / v) * v;
      while (true) {
        int vi = this.NextValueInternal();
        if (vi < maxExclusive) {
          return vi % v;
        }
      }
    }
  }
}
