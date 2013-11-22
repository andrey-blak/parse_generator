package blak.annotations.json;

import blak.annotations.BaseProcessor;
import blak.annotations.utils.CodeUtils;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO
// refactor - add holder
// class parser
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
    private static final String JSON_PARSER = "JsonParser";
    private static final String OPT = "opt";
    private static final String PARSE = "parse";
    private static final String JSON_STRING = "jsonString";
    private static final String JSON = "json";
    private static final String DTO = "dto";
    private static final String NAME = "name";
    private static final String OPT_STRING = "optString";
    private static final String STRING = "String";
    private static final String IS_EMPTY = "isEmpty";
    private static final String CHAR_AT = "charAt";
    private static final char NULL_CHAR = '\0';
    private static final String OPT_JSON_OBJECT = "optJSONObject";
    private static final String JSON1 = "Json";

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
        JDefinedClass clazz = codeModel._class(qualifiedName + JSON_PARSER);

        String simpleName = CodeUtils.getName(rootElement);
        JType rootElementType = codeModel.directClass(simpleName);

        JMethod parse = clazz.method(JMod.PUBLIC, rootElementType, PARSE);
        JVar jsonString = parse.param(codeModel.ref(STRING), JSON_STRING);
        JBlock body = parse.body();

        JClass jsonObjectType = codeModel.ref(JSONObject.class);

        JVar json = body.decl(jsonObjectType, JSON, JExpr._new(jsonObjectType).arg(jsonString));
        JVar dto = body.decl(rootElementType, DTO, JExpr._new(rootElementType));

        processElements(codeModel, body, dto, json, rootElement);

        body._return(dto);
    }

    private void processElements(JCodeModel codeModel, JBlock body, JExpression dto, JExpression json, Element rootElement) {
        for (Element element : rootElement.getEnclosedElements()) {
            AnnotationMirror mirror = findAnnotationValue(element, XmlElement.class);
            if (mirror == null) {
                continue;
            }

            if (element.getKind() == ElementKind.FIELD) {
                String fieldName = CodeUtils.getName(element);
                String key = extractValue(mirror, NAME, String.class, fieldName);

                optFieldValue(codeModel, body, dto, json, element, key);
            }
        }
    }

    private void optFieldValue(JCodeModel codeModel, JBlock body, JExpression dto, JExpression json, Element field, String key) {
        String typeString = field.asType().toString();

        if (char.class.getName().equals(typeString) || Character.class.getName().equals(typeString)) {
            optCharValue(codeModel, body, dto, json, field, key);
        } else {
            String jsonGetType = JsonUtils.getGetType(typeString);
            if (jsonGetType != null) {
                optPrimitiveValue(codeModel, body, dto, json, field, key, typeString, jsonGetType);
            } else {
                JBlock block = body.block();
                optClassValue(codeModel, block, dto, json, field, key, typeString);
            }
        }
    }

    private void optPrimitiveValue(JCodeModel codeModel, JBlock body, JExpression dto, JExpression json, Element field, String key, String typeString, String jsonGetType) {
        String getMethod = OPT + jsonGetType;

        JExpressionImpl getValue = json.invoke(getMethod).arg(key);
        if (JsonUtils.needsCast(typeString)) {
            TypeMirror typeMirror = field.asType();
            JType castType;
            if (typeMirror.getKind() == TypeKind.DECLARED) {
                // todo get wrapped type
                typeString = typeString.toLowerCase();
            }
            castType = codeModel.ref(typeString);
            getValue = JExpr.cast(castType, getValue);
        }

        String fieldName = CodeUtils.getName(field);
        JFieldRef fieldRef = dto.ref(fieldName);
        body.assign(fieldRef, getValue);
    }

    private void optCharValue(JCodeModel codeModel, JBlock body, JExpression dto, JExpression json, Element field, String key) {
        JBlock block = body.block();
        String fieldName = CodeUtils.getName(field);
        JExpression getString = json.invoke(OPT_STRING).arg(key);
        JVar charString = block.decl(codeModel.ref(STRING), fieldName + STRING, getString);

        JExpression notEmpty = charString.invoke(IS_EMPTY).not();
        JExpression firstChar = charString.invoke(CHAR_AT).arg(JExpr.lit(0));
        JExpression nullChar = JExpr.lit(NULL_CHAR);
        JExpression getChar = JOp.cond(notEmpty, firstChar, nullChar);
        JFieldRef fieldRef = dto.ref(fieldName);
        block.assign(fieldRef, getChar);
    }

    private void optClassValue(JCodeModel codeModel, JBlock block, JExpression dto, JExpression json, Element field, String key, String typeString) {
        String fieldName = CodeUtils.getName(field);
        JFieldRef fieldRef = dto.ref(fieldName);
        TypeMirror typeMirror = field.asType();

        JType fieldType = codeModel.directClass(typeMirror.toString());
        block.assign(fieldRef, JExpr._new(fieldType));

        JExpressionImpl getString = json.invoke(OPT_JSON_OBJECT).arg(key);
        JClass jsonObjectType = codeModel.ref(JSONObject.class);
        JVar fieldJson = block.decl(jsonObjectType, fieldName + JSON1, getString);

        Element fieldTypeElement = processingEnv.getTypeUtils().asElement(typeMirror);
        processElements(codeModel, block, fieldRef, fieldJson, fieldTypeElement);
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

    private static <T> T extractValue(AnnotationMirror annotationMirror, String valueName, Class<T> expectedType, T defValue) {
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
