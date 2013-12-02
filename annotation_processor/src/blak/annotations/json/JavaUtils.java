package blak.annotations.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaUtils {
    public static String getConcreteClassName(String typeString) {
        typeString = typeString.replaceAll("^" + List.class.getName(), ArrayList.class.getName());
        typeString = typeString.replaceAll("^" + Set.class.getName(), HashSet.class.getName());
        return typeString;
    }
}
