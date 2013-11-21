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
        METHODS.put("boolean", BOOLEAN);
        METHODS.put("java.lang.Boolean", BOOLEAN);

        METHODS.put("int", INT);
        METHODS.put("java.lang.Integer", INT);
        METHODS.put("byte", INT);
        METHODS.put("java.lang.Byte", INT);
        METHODS.put("short", INT);
        METHODS.put("java.lang.Short", INT);

        METHODS.put("long", LONG);
        METHODS.put("java.lang.Long", LONG);

        METHODS.put("double", DOUBLE);
        METHODS.put("java.lang.Double", DOUBLE);
        METHODS.put("float", DOUBLE);
        METHODS.put("java.lang.Float", DOUBLE);

        METHODS.put("java.lang.String", STRING);

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
