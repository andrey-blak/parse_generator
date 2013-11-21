package blak.annotations.android;

import blak.annotations.ViewById;
import blak.annotations.utils.ALog;
import com.sun.codemodel.JCodeModel;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public class ViewByIdProcessor implements DecoratingElementProcessor {
    private ProcessingEnvironment env;
    private ALog mLog;

    public ViewByIdProcessor(ProcessingEnvironment processingEnv) {
        env = processingEnv;
        mLog = new ALog(processingEnv);
    }

    @Override
    public String getTarget() {
        return ViewById.class.getName();
    }

    @Override
    public void process(Element element, JCodeModel codeModel, EBeanHolder holder) {
        String fieldName = element.getSimpleName().toString();

        TypeMirror uiFieldTypeMirror = element.asType();
        String typeQualifiedName = uiFieldTypeMirror.toString();

        mLog.print("ViewByIdProcessor", fieldName, holder.generatedClass);
    }
}
