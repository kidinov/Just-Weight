package org.kidinov.justweight.util;

/**
 * Created by akid on 17/04/15.
 */
public class ArrayUtil {

    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];

    public static float[] toPrimitive(final Float[] array, final float valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_FLOAT_ARRAY;
        }
        final float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            final Float b = array[i];
            result[i] = (b == null ? valueForNull : b.floatValue());
        }
        return result;
    }
}
