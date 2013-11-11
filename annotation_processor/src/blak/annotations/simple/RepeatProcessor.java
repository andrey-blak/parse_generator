package blak.annotations.simple;

import blak.annotations.OriginatingElements;
import blak.annotations.ResourceCodeWriter;
import blak.annotations.SourceCodeWriter;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class RepeatProcessor extends AbstractProcessor {
    private Set<String> supportedAnnotationNames;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        JCodeModel codeModel = new JCodeModel();

        try {
            Filer filer = processingEnv.getFiler();
            Messager messager = processingEnv.getMessager();

            SourceCodeWriter sourceCodeWriter = new SourceCodeWriter(filer, messager, new OriginatingElements());
            JDefinedClass definedClass = codeModel._class("com.example.MyNewClass");
            codeModel.build(sourceCodeWriter, new ResourceCodeWriter(filer));
            //JFieldVar field = definedClass.field(JMod.PRIVATE, int.class, "intVar");
        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
            printWarning(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            printWarning(e.getMessage());
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        if (supportedAnnotationNames == null) {
            Class<?>[] annotationClassesArray = {
                    Repeat.class,
            };

            Set<String> set = new HashSet<String>(annotationClassesArray.length);
            for (Class<?> annotationClass : annotationClassesArray) {
                set.add(annotationClass.getName());
            }
            supportedAnnotationNames = Collections.unmodifiableSet(set);
        }
        return supportedAnnotationNames;
    }

    private void printWarning(Object message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message.toString());
    }
}
