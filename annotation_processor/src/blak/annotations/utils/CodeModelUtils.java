package blak.annotations.utils;

import blak.annotations.Java;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

public class CodeModelUtils {
    private static final char NULL_CHAR = '\0';

    public static void ifNullReturnNull(JBlock block, JExpression expr) {
        block._if(expr.eq(JExpr._null()))
                ._then()
                ._return(JExpr._null());
    }

    public static JExpression getDefaultValue(String typeString, String defaultValue) {
        if (defaultValue == null) {
            return null;
        }

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

    public static JExpression getDefaultChar(String defaultValue) {
        return (defaultValue != null) ?
                getDefaultValue(char.class.getName(), defaultValue) :
                JExpr.lit(NULL_CHAR);
    }

    public static JExpression notEmpty(JExpression stringVar) {
        return stringVar.invoke(Java.IS_EMPTY).not();
    }

    public static JBlock ifNotNull(JBlock block, JExpression expression) {
        return block._if(expression.ne(JExpr._null()))._then();
    }
}
