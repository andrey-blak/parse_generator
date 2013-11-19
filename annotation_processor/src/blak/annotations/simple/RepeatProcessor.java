package blak.annotations.simple;

import blak.annotations.utils.ALog;
import blak.annotations.utils.OriginatingElements;
import blak.annotations.utils.ResourceCodeWriter;
import blak.annotations.utils.SourceCodeWriter;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class RepeatProcessor extends AbstractProcessor {
    private static final String GENERATION_SUFFIX = "_";

    private Set<String> mSupportedAnnotationNames;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //roundEnv.getRootElements().contains()
        for (Element element : roundEnv.getElementsAnnotatedWith(RActivity.class)) {
            try {
                TypeElement typeElement = (TypeElement) element;
                String qualifiedName = typeElement.getQualifiedName().toString();
                JCodeModel codeModel = new JCodeModel();

                JDefinedClass clazz = codeModel._class(qualifiedName + GENERATION_SUFFIX);

                // extends
                JClass annotatedActivity = codeModel.directClass(qualifiedName);
                clazz._extends(annotatedActivity);
                //generateExtends();

                generateInit(codeModel, clazz);

                generateClassFile(processingEnv, codeModel);
            } catch (JClassAlreadyExistsException e) {
                e.printStackTrace();
                ALog.print(processingEnv, e.getMessage());
            }
        }

        return true;
    }

    private void generateInit(JCodeModel codeModel, JDefinedClass clazz) {
        JMethod init = clazz.method(JMod.PUBLIC, codeModel.INT, "init");

        init.annotate(Deprecated.class);

        JVar paramAlpha = init.param(codeModel.INT, "alpha");

        JBlock body = init.body();

        body._return(paramAlpha);
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

    private void generateClassFile(ProcessingEnvironment env, JCodeModel codeModel) {
        try {
            Filer filer = processingEnv.getFiler();
            Messager messager = processingEnv.getMessager();
            SourceCodeWriter sourceCodeWriter = new SourceCodeWriter(filer, messager, new OriginatingElements());
            codeModel.build(sourceCodeWriter, new ResourceCodeWriter(filer));
        } catch (IOException e) {
            e.printStackTrace();
            ALog.print(env, e.getMessage());
        }
    }
}
