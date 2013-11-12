package blak.annotations.simple;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class RepeatProcessor extends AbstractProcessor {
    private static final String GENERATION_SUFFIX = "_";

    private Set<String> mSupportedAnnotationNames;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        printWarning("Root elements");
        for (Element elem : roundEnv.getRootElements()) {
            printWarning(elem);
            TypeElement typeElement = (TypeElement) elem;
            for (Element element : typeElement.getEnclosedElements()) {
                printWarning("   ", element);
            }
        }

        printWarning("Annotated with", Repeat.class);
        for (Element el : roundEnv.getElementsAnnotatedWith(Repeat.class)) {
            printWarning(el);
        }

        //try {
        //    JCodeModel codeModel = new JCodeModel();
        //    Filer filer = processingEnv.getFiler();
        //    Messager messager = processingEnv.getMessager();
        //
        //    SourceCodeWriter sourceCodeWriter = new SourceCodeWriter(filer, messager, new OriginatingElements());
        //    JDefinedClass definedClass = codeModel._class("com.example.MyNewClass");
        //    codeModel.build(sourceCodeWriter, new ResourceCodeWriter(filer));
        //    //JFieldVar field = definedClass.field(JMod.PRIVATE, int.class, "intVar");
        //} catch (JClassAlreadyExistsException e) {
        //    e.printStackTrace();
        //    printWarning(e.getMessage());
        //} catch (IOException e) {
        //    e.printStackTrace();
        //    printWarning(e.getMessage());
        //}

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        if (mSupportedAnnotationNames == null) {
            Class<?>[] annotationClassesArray = {
                    Repeat.class,
            };

            Set<String> set = new HashSet<String>(annotationClassesArray.length);
            for (Class<?> annotationClass : annotationClassesArray) {
                set.add(annotationClass.getName());
            }
            mSupportedAnnotationNames = Collections.unmodifiableSet(set);
        }
        return mSupportedAnnotationNames;
    }

    private void printWarning(Object message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message.toString());
    }

    private void printWarning(Object... messages) {
        printWarning(join(messages));
    }

    public static String join(Object... params) {
        StringBuilder buff = new StringBuilder();
        for (Object object : params) {
            buff.append(object.toString());
            buff.append(" ");
        }
        return buff.toString();
    }
}
