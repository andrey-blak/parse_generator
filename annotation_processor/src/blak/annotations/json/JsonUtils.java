package blak.annotations.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class JsonUtils {
    private static final String BOOLEAN = "Boolean";
    private static final String INT = "Int";
    private static final String LONG = "Long";
    private static final String DOUBLE = "Double";
    private static final String STRING = "String";

    private static final Map<String, String> METHODS = new HashMap<String, String>(15);
    private static final Collection<String> CAST_TYPES = new HashSet<String>();

    static {
        METHODS.put(boolean.class.getName(), BOOLEAN);
        METHODS.put(Boolean.class.getName(), BOOLEAN);

        METHODS.put(int.class.getName(), INT);
        METHODS.put(Integer.class.getName(), INT);
        METHODS.put(byte.class.getName(), INT);
        METHODS.put(Byte.class.getName(), INT);
        METHODS.put(short.class.getName(), INT);
        METHODS.put(Short.class.getName(), INT);

        METHODS.put(long.class.getName(), LONG);
        METHODS.put(Long.class.getName(), LONG);

        METHODS.put(double.class.getName(), DOUBLE);
        METHODS.put(Double.class.getName(), DOUBLE);
        METHODS.put(float.class.getName(), DOUBLE);
        METHODS.put(Float.class.getName(), DOUBLE);

        METHODS.put(String.class.getName(), STRING);

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
