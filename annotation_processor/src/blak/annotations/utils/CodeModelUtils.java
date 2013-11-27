package blak.annotations.utils;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class CodeModelUtils {
    public static void ifNullReturnNull(JBlock block, JExpression expr) {
        block._if(expr.eq(JExpr._null()))
                ._then()
                ._return(JExpr._null());
    }

    public static JExpression getDefaultValue(TypeMirror typeMirror, String defaultValue) {
        if (defaultValue == null) {
            return null;
        }

        String typeString = typeMirror.toString();
        if (ProcessingUtils.isString(typeString)) {
            String quotified = JExpr.quotify('\"', defaultValue);
            return JExpr.direct(quotified);
        }
        if (ProcessingUtils.isChar(typeString)) {
            String quotified = JExpr.quotify('\'', defaultValue);
            return JExpr.direct(quotified);
        }

        return JExpr.direct(defaultValue);
    }
}
