package blak.annotations.utils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;

public class ALog {
    private ProcessingEnvironment mEnv;

    public ALog(ProcessingEnvironment env) {
        mEnv = env;
    }

    public void print(Object message) {
        mEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message.toString());
    }

    public void print(Object... messages) {
        print(join(messages));
    }

    public void printElements(RoundEnvironment roundEnv, Class<? extends Annotation> annotation) {
        print("Root elements");
        for (Element elem : roundEnv.getRootElements()) {
            print(elem);
            TypeElement typeElement = (TypeElement) elem;
            for (Element element : typeElement.getEnclosedElements()) {
                print("   ", element);
            }
        }

        print("Annotated with", annotation);
        for (Element el : roundEnv.getElementsAnnotatedWith(annotation)) {
            print(el);
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
