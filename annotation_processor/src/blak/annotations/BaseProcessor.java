package blak.annotations;

import blak.annotations.utils.ALog;
import blak.annotations.utils.OriginatingElements;
import blak.annotations.utils.ResourceCodeWriter;
import blak.annotations.utils.SourceCodeWriter;
import com.sun.codemodel.JCodeModel;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public abstract class BaseProcessor extends AbstractProcessor {
    private Set<String> mSupportedAnnotationNames;

    protected ALog mLog;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mLog = new ALog(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (nothingToDo(annotations, roundEnv)) {
            return false;
        }

        return processAnnotations(annotations, roundEnv);
    }

    protected static boolean nothingToDo(Collection<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return roundEnv.processingOver() || annotations.isEmpty();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        if (mSupportedAnnotationNames == null) {
            Class<?>[] annotations = getSupportedAnnotations();

            Set<String> set = new HashSet<String>(annotations.length);
            for (Class<?> annotationClass : annotations) {
                set.add(annotationClass.getName());
            }
            mSupportedAnnotationNames = Collections.unmodifiableSet(set);
        }
        return mSupportedAnnotationNames;
    }

    protected void buildModel(JCodeModel codeModel) {
        try {
            Filer filer = processingEnv.getFiler();
            Messager messager = processingEnv.getMessager();
            SourceCodeWriter sourceCodeWriter = new SourceCodeWriter(filer, messager, new OriginatingElements());
            codeModel.build(sourceCodeWriter, new ResourceCodeWriter(filer));
        } catch (IOException e) {
            e.printStackTrace();
            mLog.print(e.getMessage());
        }
    }

    protected abstract Class<?>[] getSupportedAnnotations();

    protected abstract boolean processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);
}
