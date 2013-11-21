package blak.annotations.json;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    private static final String OPT_BOOLEAN = "optBoolean";
    private static final String OPT_INT = "optInt";
    private static final String OPT_LONG = "optLong";
    private static final String OPT_DOUBLE = "optDouble";
    private static final String OPT_STRING = "optString";

    private static final Map<String, String> OPT_METHODS = new HashMap<String, String>(16);

    static {
        OPT_METHODS.put("int", OPT_INT);
        OPT_METHODS.put("java.lang.Integer", OPT_INT);
        OPT_METHODS.put("boolean", OPT_BOOLEAN);
        OPT_METHODS.put("java.lang.Boolean", OPT_BOOLEAN);
        OPT_METHODS.put("long", OPT_LONG);
        OPT_METHODS.put("java.lang.Long", OPT_LONG);
        OPT_METHODS.put("double", OPT_DOUBLE);
        OPT_METHODS.put("java.lang.Double", OPT_DOUBLE);
        OPT_METHODS.put("java.lang.String", OPT_STRING);
    }

    public static String getOptMethod(String type) {
        return OPT_METHODS.get(type);
    }
}
