package blak.annotations.json;

import blak.annotations.utils.ALog;
import blak.annotations.utils.OriginatingElements;
import blak.annotations.utils.ResourceCodeWriter;
import blak.annotations.utils.SourceCodeWriter;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// default name
// default value
// required

public class JsonProcessor extends AbstractProcessor {
    private static final String GENERATION_SUFFIX = "JsonParser";

    private Set<String> mSupportedAnnotationNames;

    // todo duplication
    private static boolean nothingToDo(Collection<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return roundEnv.processingOver() || annotations.isEmpty();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (nothingToDo(annotations, roundEnv)) {
            return false;
        }

        JCodeModel codeModel = new JCodeModel();
        Set<? extends Element> rootElements = roundEnv.getElementsAnnotatedWith(XmlRootElement.class);
        for (Element rootElement : rootElements) {
            try {
                process(codeModel, roundEnv, (TypeElement) rootElement);
            } catch (JClassAlreadyExistsException e) {
                e.printStackTrace();
            }
        }
        generateClassFiles(processingEnv, codeModel);

        return true;
    }

    private void process(JCodeModel codeModel, RoundEnvironment roundEnv, TypeElement rootElement) throws JClassAlreadyExistsException {
        String qualifiedName = rootElement.getQualifiedName().toString();
        JDefinedClass clazz = codeModel._class(qualifiedName + GENERATION_SUFFIX);
        JType rootElementType = codeModel.directClass(qualifiedName);
        JMethod parse = clazz.method(JMod.PUBLIC, rootElementType, "parse");
    }

    // todo duplication
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        if (mSupportedAnnotationNames == null) {
            Class<?>[] annotationClassesArray = {
                    XmlRootElement.class
            };

            Set<String> set = new HashSet<String>(annotationClassesArray.length);
            for (Class<?> annotationClass : annotationClassesArray) {
                set.add(annotationClass.getName());
            }
            mSupportedAnnotationNames = Collections.unmodifiableSet(set);
        }
        return mSupportedAnnotationNames;
    }

    // todo duplication
    private static void generateClassFiles(ProcessingEnvironment env, JCodeModel codeModel) {
        try {
            Filer filer = env.getFiler();
            Messager messager = env.getMessager();
            SourceCodeWriter sourceCodeWriter = new SourceCodeWriter(filer, messager, new OriginatingElements());
            codeModel.build(sourceCodeWriter, new ResourceCodeWriter(filer));
        } catch (IOException e) {
            e.printStackTrace();
            ALog.print(env, e.getMessage());
        }
    }
}
