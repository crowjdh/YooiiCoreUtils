package com.yooiistudios.coreutils;

import android.util.Log;

import java.lang.reflect.Array;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 7. 5.
 * <br/><br/>
 * HashCodeUtils<br/>
 *  Collected methods which allow easy implementation of <tt>hashCode</tt>.<br/>
 *  (Joshua Bloch's method from <tt>Effective Java</tt><br/>
 *  Copied from: http://www.javapractices.com/topic/TopicAction.do?Id=28<br/>
 * <br/>
 * Example use case:
 * <pre>
 *  public int hashCode(){
 *    int result = HashCodeUtil.SEED;
 *    //collect the contributions of various fields
 *    result = HashCodeUtil.hash(result, fPrimitive);
 *    result = HashCodeUtil.hash(result, fObject);
 *    result = HashCodeUtil.hash(result, fArray);
 *    return result;
 *  }
 * </pre>
 */
public class HashCodeUtils {
    /**
     * An initial value for a <tt>hashCode</tt>, to which is added contributions
     * from fields. Using a non-zero value decreases collisons of <tt>hashCode</tt>
     * values.
     */
    public static final int SEED = 23;

    private static final String TAG = HashCodeUtils.class.getSimpleName();

    /** booleans.  */
    public static int hash(int aSeed, boolean aBoolean) {
        log("boolean...");
        return firstTerm( aSeed ) + (aBoolean ? 1 : 0);
    }

    /*** chars.  */
    public static int hash(int aSeed, char aChar) {
        log("char...");
        return firstTerm(aSeed) + (int)aChar;
    }

    /** ints.  */
    public static int hash(int aSeed , int aInt) {
    /*
    * Implementation Note
    * Note that byte and short are handled by this method, through
    * implicit conversion.
    */
        log("int...");
        return firstTerm(aSeed) + aInt;
    }

    /** longs.  */
    public static int hash(int aSeed , long aLong) {
        log("long...");
        return firstTerm(aSeed)  + (int)(aLong ^ (aLong >>> 32));
    }

    /** floats.  */
    public static int hash(int aSeed , float aFloat) {
        return hash(aSeed, Float.floatToIntBits(aFloat));
    }

    /** doubles. */
    public static int hash(int aSeed , double aDouble) {
        return hash( aSeed, Double.doubleToLongBits(aDouble) );
    }

    /**
     * <tt>aObject</tt> is a possibly-null object field, and possibly an array.
     *
     * If <tt>aObject</tt> is an array, then each element may be a primitive
     * or a possibly-null object.
     */
    public static int hash(int aSeed , Object aObject) {
        int result = aSeed;
        if (aObject == null){
            result = hash(result, 0);
        }
        else if (!isArray(aObject)){
            result = hash(result, aObject.hashCode());
        }
        else {
            int length = Array.getLength(aObject);
            for (int idx = 0; idx < length; ++idx) {
                Object item = Array.get(aObject, idx);
                //if an item in the array references the array itself, prevent infinite looping
                if(! (item == aObject))
                    //recursive call!
                    result = hash(result, item);
            }
        }
        return result;
    }

    // PRIVATE
    private static final int fODD_PRIME_NUMBER = 37;

    private static int firstTerm(int aSeed){
        return fODD_PRIME_NUMBER * aSeed;
    }

    private static boolean isArray(Object aObject){
        return aObject.getClass().isArray();
    }

    private static void log(String aMessage){
        Log.d(TAG, aMessage);
        System.out.println(aMessage);
    }
}
