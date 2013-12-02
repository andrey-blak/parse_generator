package com.example.annotations;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import java.io.File;
import static java.lang.System.out;

public class CodeGeneration {
    private static final String GENERATION_SUFFIX = "_";

    public static void main(String[] args) throws Exception {
        String qualifiedName = "com.example.annotations.Code";

        JCodeModel codeModel = new JCodeModel();
        JDefinedClass clazz = codeModel._class(qualifiedName + GENERATION_SUFFIX);

        generateExtends(codeModel, clazz, qualifiedName);
        generateInit(codeModel, clazz);

        out.println(codeModel.ref(java.lang.Boolean[].class));
        out.println(codeModel.ref("java.lang.Boolean[]"));

        File destDir = new File("custom_annotations_sample/gen");
        destDir.mkdirs();
        codeModel.build(destDir);
    }

    private static void generateExtends(JCodeModel codeModel, JDefinedClass clazz, String qualifiedName) {
        //JClass annotatedActivity = codeModel.directClass(qualifiedName);
        //clazz._extends(annotatedActivity);
    }

    private static void generateInit(JCodeModel codeModel, JDefinedClass clazz) {
        JMethod init = clazz.getMethod("init", new JType[]{codeModel.INT});
        if (init != null) {
            return;
        }

        init = clazz.method(JMod.PUBLIC, codeModel.INT, "init");

        init.annotate(Deprecated.class);

        JVar value = init.param(codeModel.CHAR, "value");

        JClass stringType = codeModel.ref(String.class);

        JBlock body = init.body();
        JVar var = body.decl(stringType, "var");
        var.init(JExpr._new(stringType));
        body.assign(var, JExpr.direct("\"TestString\""));

        JExpression result = JExpr.direct("var.charAt(100)");
        body._return(result);
    }
}