package blak.annotations.android;

import blak.annotations.utils.OriginatingElements;
import com.sun.codemodel.JCodeModel;

import java.util.Set;

public class ProcessResult {
    public final JCodeModel codeModel;
    public final OriginatingElements originatingElements;
    public final Set<Class<?>> apiClassesToGenerate;

    public ProcessResult(JCodeModel codeModel, OriginatingElements originatingElements, Set<Class<?>> apiClassesToGenerate) {
        this.codeModel = codeModel;
        this.originatingElements = originatingElements;
        this.apiClassesToGenerate = apiClassesToGenerate;
    }
}