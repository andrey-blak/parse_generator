package blak.annotations.json;

import blak.annotations.BaseProcessor;
import blak.annotations.utils.ALog;
import blak.annotations.utils.OriginatingElements;
import blak.annotations.utils.ResourceCodeWriter;
import blak.annotations.utils.SourceCodeWriter;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import org.json.JSONObject;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// TODO
// class
// refactor - add holder
// methods
// enum
// default name
// default value
// required
// arrays & collections (maps?)
// generics
// char

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class JsonProcessor extends BaseProcessor {
    private static final String GENERATION_SUFFIX = "JsonParser";
    private static final String OPT = "opt";

    @Override
    public boolean processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        JCodeModel codeModel = new JCodeModel();
        Set<? extends Element> rootElements = roundEnv.getElementsAnnotatedWith(XmlRootElement.class);
        for (Element rootElement : rootElements) {
            try {
                process(codeModel, roundEnv, (TypeElement) rootElement);
            } catch (JClassAlreadyExistsException e) {
                e.printStackTrace();
            }
        }
        buildModel(codeModel);

        return false;
    }

    private void process(JCodeModel codeModel, RoundEnvironment roundEnv, TypeElement rootElement) throws JClassAlreadyExistsException {
        String qualifiedName = rootElement.getQualifiedName().toString();
        JDefinedClass clazz = codeModel._class(qualifiedName + GENERATION_SUFFIX);

        String simpleName = rootElement.getSimpleName().toString();
        JType rootElementType = codeModel.directClass(simpleName);

        JMethod parse = clazz.method(JMod.PUBLIC, rootElementType, "parse");
        JVar jsonString = parse.param(String.class, "jsonString");
        JBlock body = parse.body();

        JClass jsonObjectType = codeModel.ref(JSONObject.class);

        JVar json = body.decl(jsonObjectType, "json", JExpr._new(jsonObjectType).arg(jsonString));
        JVar dto = body.decl(rootElementType, "dto", JExpr._new(rootElementType));

        for (Element element : rootElement.getEnclosedElements()) {
            AnnotationMirror mirror = findAnnotationValue(element, XmlElement.class);
            if (mirror == null) {
                continue;
            }

            if (element.getKind() == ElementKind.FIELD) {
                String fieldName = element.getSimpleName().toString();
                String key = extractValue(mirror, "name", String.class, fieldName);

                optFieldValue(codeModel, body, dto, json, element, key);
            }
        }

        body._return(dto);
    }

    private void optFieldValue(JCodeModel codeModel, JBlock body, JExpression dto, JExpression json, Element field, String key) {
        TypeMirror typeMirror = field.asType();
        String typeString = typeMirror.toString();

        if ("char".equals(typeString) || "java.lang.Char".equals(typeString)) {
            optCharValue(codeModel, body, dto, json, field, key);
        } else {
            String jsonGetType = JsonUtils.getGetType(typeString);
            if (jsonGetType != null) {
                optPrimitiveValue(codeModel, body, dto, json, field, key, typeMirror, typeString, jsonGetType);
            } else {
                optClassValue(codeModel, body, dto, json, field, key, typeMirror, typeString);
            }
        }
    }

    private void optPrimitiveValue(JCodeModel codeModel, JBlock body, JExpression dto, JExpression json, Element field, String key, TypeMirror typeMirror, String typeString, String jsonGetType) {
        String getMethod = OPT + jsonGetType;

        JExpressionImpl getValue = json.invoke(getMethod).arg(key);
        if (JsonUtils.needsCast(typeString)) {
            JType castType;
            if (typeMirror.getKind() == TypeKind.DECLARED) {
                // todo temp
                castType = codeModel.ref(typeString.toLowerCase());
            } else {
                castType = codeModel.ref(typeString);
            }
            getValue = JExpr.cast(castType, getValue);
        }

        String fieldName = field.getSimpleName().toString();
        JFieldRef fieldRef = dto.ref(fieldName);
        body.assign(fieldRef, getValue);
    }

    private void optCharValue(JCodeModel codeModel, JBlock body, JExpression dto, JExpression json, Element field, String key) {
        JBlock block = body.block();
        String fieldName = field.getSimpleName().toString();
        JExpression getString = json.invoke("optString").arg(key);
        JVar charString = block.decl(codeModel.ref("String"), fieldName + "String", getString);

        JExpression notEmpty = charString.invoke("isEmpty").not();
        JExpression firstChar = charString.invoke("charAt").arg(JExpr.lit(0));
        JExpression nullChar = JExpr.lit('\0');
        JExpression getChar = JOp.cond(notEmpty, firstChar, nullChar);
        JFieldRef fieldRef = dto.ref(fieldName);
        block.assign(fieldRef, getChar);
    }

    private void optClassValue(JCodeModel codeModel, JBlock body, JExpression dto, JExpression json, Element field, String key, TypeMirror typeMirror, String typeString) {
        String fieldName = field.getSimpleName().toString();
        JFieldRef fieldRef = dto.ref(fieldName);

        mLog.print(processingEnv.getTypeUtils().asElement(typeMirror));
        mLog.print(processingEnv.getTypeUtils().asElement(typeMirror).getEnclosedElements());

        JType fieldType = codeModel.directClass(typeMirror.toString());
        body.assign(fieldRef, JExpr._new(fieldType));

        //dto.menuItem = new DtoMenu.DtoMenuItem();
        //JSONObject menuItemJson = json.getJSONObject("popup");
        //dto.menuItem.onclick = menuItemJson.getString("menuItem");
    }

    private static <T> AnnotationMirror findAnnotationValue(Element element, Class<T> annotationClass) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement) annotationType.asElement();
            if (annotationElement.getQualifiedName().contentEquals(annotationClass.getCanonicalName())) {
                return annotationMirror;
            }
        }
        return null;
    }

    private <T> T extractValue(AnnotationMirror annotationMirror, String valueName, Class<T> expectedType, T defValue) {
        Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<ExecutableElement, AnnotationValue>(annotationMirror.getElementValues());
        for (Map.Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(valueName)) {
                Object value = entry.getValue().getValue();
                return expectedType.cast(value);
            }
        }
        return defValue;
    }

    @Override
    protected Class<?>[] getSupportedAnnotations() {
        return new Class[]{
                XmlRootElement.class
        };
    }
}
