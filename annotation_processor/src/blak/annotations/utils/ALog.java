package blak.annotations.utils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;

public class ALog {
    public static void print(ProcessingEnvironment env, Object message) {
        env.getMessager().printMessage(Diagnostic.Kind.NOTE, message.toString());
    }

    public static void print(ProcessingEnvironment env, Object... messages) {
        print(env, join(messages));
    }

    public static void printElements(ProcessingEnvironment env, RoundEnvironment roundEnv, Class<? extends Annotation> annotation) {
        print(env, "Root elements");
        for (Element elem : roundEnv.getRootElements()) {
            print(env, elem);
            TypeElement typeElement = (TypeElement) elem;
            for (Element element : typeElement.getEnclosedElements()) {
                print(env, "   ", element);
            }
        }

        print(env, "Annotated with", annotation);
        for (Element el : roundEnv.getElementsAnnotatedWith(annotation)) {
            print(env, el);
        }
    }

    public static String join(Object... params) {
        StringBuilder buff = new StringBuilder();
        for (Object object : params) {
            if (object == null) {
                buff.append("null ");
                continue;
            }

            buff.append(object.toString());
            buff.append(" ");
        }
        return buff.toString();
    }
}
