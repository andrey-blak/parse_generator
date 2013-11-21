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
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
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
// char
// methods
// class
// enum
// default name
// default value
// required
// arrays & collections (maps?)
// generics

public class JsonProcessor extends BaseProcessor {
    private static final String GENERATION_SUFFIX = "JsonParser";

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

    private void optFieldValue(JCodeModel codeModel, JBlock body, JVar dto, JVar json, Element field, String key) {
        String fieldName = field.getSimpleName().toString();
        JFieldRef fieldRef = dto.ref(fieldName);

        TypeMirror typeMirror = field.asType();
        String typeString = typeMirror.toString();
        String jsonGetType = JsonUtils.getGetType(typeString);
        String opt = "opt";
        if (jsonGetType == null) {
            jsonGetType = "Object";
        }
        String getMethod = opt + jsonGetType;

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

        body.assign(fieldRef, getValue);
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
