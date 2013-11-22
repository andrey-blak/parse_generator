package blak.annotations.simple;

import blak.annotations.BaseProcessor;
import blak.annotations.EActivity;
import blak.annotations.ViewById;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class RepeatProcessor extends BaseProcessor {
    private static final String GENERATION_SUFFIX = "_";

    @Override
    public boolean processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        printElements(annotations, roundEnv);

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

                buildModel(codeModel);
            } catch (JClassAlreadyExistsException e) {
                e.printStackTrace();
                mLog.print(e.getMessage());
            }
        }

        return false;
    }

    public void printElements(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> eactivities = roundEnv.getElementsAnnotatedWith(EActivity.class);
        Set<? extends Element> ractivities = roundEnv.getElementsAnnotatedWith(RActivity.class);
        Set<? extends Element> viewidElements = roundEnv.getElementsAnnotatedWith(ViewById.class);
        Set<? extends Element> repeatElements = roundEnv.getElementsAnnotatedWith(Repeat.class);

        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {

                Element enclosingElement = element.getEnclosingElement();

                for (AnnotationMirror annotationMirror : enclosingElement.getAnnotationMirrors()) {
                    DeclaredType annotationType = annotationMirror.getAnnotationType();
                    TypeElement annotationElement = (TypeElement) annotationType.asElement();
                    if (annotationElement.getQualifiedName().contentEquals(RActivity.class.getCanonicalName())) {
                        mLog.print(enclosingElement, "annotated with RActivity");
                        break;
                    }
                }

                mLog.print(enclosingElement.getSimpleName(), enclosingElement.getAnnotationMirrors());
                if (eactivities.contains(enclosingElement)) {
                    mLog.print("eactivities", element.getSimpleName(), annotation.getSimpleName(), enclosingElement.getSimpleName());
                }

                if (ractivities.contains(enclosingElement)) {
                    mLog.print("ractivities", element.getSimpleName(), annotation.getSimpleName(), enclosingElement.getSimpleName());
                }

                if (viewidElements.contains(enclosingElement)) {
                    mLog.print("viewidElements", element.getSimpleName(), annotation.getSimpleName(), enclosingElement.getSimpleName());
                }

                if (repeatElements.contains(enclosingElement)) {
                    mLog.print("repeatElements", element.getSimpleName(), annotation.getSimpleName(), enclosingElement.getSimpleName());
                }
            }
        }
    }

    private void generateInit(JCodeModel codeModel, JDefinedClass clazz) {
        JMethod init = clazz.method(JMod.PUBLIC, codeModel.INT, "init");
        init.annotate(Deprecated.class);
        JVar paramAlpha = init.param(codeModel.INT, "alpha");
        JBlock body = init.body();
        body._return(paramAlpha);
    }

    @Override
    protected Class<?>[] getSupportedAnnotations() {
        return new Class<?>[]{
                Repeat.class,
                EActivity.class,
                ViewById.class,
        };
    }
}
