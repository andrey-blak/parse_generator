package blak.annotations.utils;

import javax.lang.model.element.Element;

public class CodeUtils {
    public static String getName(Element element) {
        return element.getSimpleName().toString();
    }
}
