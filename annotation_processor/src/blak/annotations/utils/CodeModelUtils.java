package blak.annotations.utils;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

public class CodeModelUtils {
    public static void ifNullReturnNull(JBlock block, JExpression expr) {
        block._if(expr.eq(JExpr._null()))
                ._then()
                ._return(JExpr._null());
    }
}
