package blak.annotations.utils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.beans.Introspector;
import java.util.HashMap;
import java.util.Map;

public class ProcessingUtils {
    public static Element getPackage(Element element) {
        Element packageElement = element.getEnclosingElement();
        while (packageElement.getEnclosingElement() != null) {
            packageElement = packageElement.getEnclosingElement();
        }
        return packageElement;
    }

    public static String getSetFieldName(String methodName) {
        if (methodName.startsWith("set")) {
            methodName = methodName.replaceAll("^set", "");
            methodName = Introspector.decapitalize(methodName);
        }
        return methodName;
    }

    public static boolean isEnum(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        String enumType = Enum.class.getName() + "<" + typeMirror + ">";
        for (TypeMirror type : processingEnv.getTypeUtils().directSupertypes(typeMirror)) {
            if (type.toString().equals(enumType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isChar(TypeMirror type) {
        return isChar(type.toString());
    }

    public static boolean isChar(String type) {
        return type.equals(char.class.getName()) || type.equals(Character.class.getName());
    }

    public static boolean isString(String typeString) {
        return typeString.equals(String.class.getName());
    }

    public static boolean haveAnnotation(Element element, Class annotationClass) {
        return (findAnnotationValue(element, annotationClass) != null);
    }

    public static <T> AnnotationMirror findAnnotationValue(Element element, Class<T> annotationClass) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement) annotationType.asElement();
            if (annotationElement.getQualifiedName().contentEquals(annotationClass.getCanonicalName())) {
                return annotationMirror;
            }
        }
        return null;
    }

    public static <T> T extractValue(AnnotationMirror annotationMirror, CharSequence valueName, Class<T> expectedType, T defValue) {
        Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<ExecutableElement, AnnotationValue>(annotationMirror.getElementValues());
        for (Map.Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(valueName)) {
                Object value = entry.getValue().getValue();
                return expectedType.cast(value);
            }
        }
        return defValue;
    }
}
