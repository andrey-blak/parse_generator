package blak.annotations.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class JsonUtils {
    private static final Map<String, String> METHODS = new HashMap<String, String>(15);
    private static final Collection<String> CAST_TYPES = new HashSet<String>();

    static {
        METHODS.put(boolean.class.getName(), Json.BOOLEAN);
        METHODS.put(Boolean.class.getName(), Json.BOOLEAN);

        METHODS.put(int.class.getName(), Json.INT);
        METHODS.put(Integer.class.getName(), Json.INT);
        METHODS.put(byte.class.getName(), Json.INT);
        METHODS.put(Byte.class.getName(), Json.INT);
        METHODS.put(short.class.getName(), Json.INT);
        METHODS.put(Short.class.getName(), Json.INT);

        METHODS.put(long.class.getName(), Json.LONG);
        METHODS.put(Long.class.getName(), Json.LONG);

        METHODS.put(double.class.getName(), Json.DOUBLE);
        METHODS.put(Double.class.getName(), Json.DOUBLE);
        METHODS.put(float.class.getName(), Json.DOUBLE);
        METHODS.put(Float.class.getName(), Json.DOUBLE);

        METHODS.put(String.class.getName(), Json.STRING);

        CAST_TYPES.add(byte.class.getName());
        CAST_TYPES.add(Byte.class.getName());
        CAST_TYPES.add(short.class.getName());
        CAST_TYPES.add(Short.class.getName());
        CAST_TYPES.add(float.class.getName());
        CAST_TYPES.add(Float.class.getName());
    }

    public static String getGetType(String type) {
        return METHODS.get(type);
    }

    public static boolean needsCast(String type) {
        return CAST_TYPES.contains(type);
    }
}
