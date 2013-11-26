package blak.annotations.json;

import blak.annotations.BaseProcessor;
import blak.annotations.android.ErrorHelper;
import blak.annotations.android.ProcessingException;
import blak.annotations.utils.CodeModelUtils;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;

// TODO
// default name
// default value
// required
// arrays & collections (maps?)
// generics
// getChar(Json)
// getClass(Json)
// validation
// junit | functional tests

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
    private static final String JSON_SUFFIX = "Json";
    private static final String TEMP = "temp";
    private static final String VALUE_OF = "valueOf";

    private JCodeModel mCodeModel;
    private JBlock mBody;
    private int mAutoincrement;

    @Override
    public boolean processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            mCodeModel = new JCodeModel();
            Set<? extends Element> rootElements = roundEnv.getElementsAnnotatedWith(XmlRootElement.class);
            for (Element rootElement : rootElements) {
                mAutoincrement = 0;
                process(roundEnv, (TypeElement) rootElement);
            }
            buildModel(mCodeModel);
        } catch (Exception e) {
            String errorMessage = ErrorHelper.getErrorMessage(processingEnv, new ProcessingException(e, null));
            mLog.print(errorMessage);
        }

        return false;
    }

    private void process(RoundEnvironment roundEnv, TypeElement rootElement) throws JClassAlreadyExistsException {
        String packageName = CodeUtils.getPackage(rootElement).toString();
        String simpleName = rootElement.getSimpleName().toString();

        JDefinedClass clazz = mCodeModel._class(packageName + "." + simpleName + JSON_PARSER);

        JClass rootElementType = mCodeModel.ref(rootElement.getQualifiedName().toString());

        createParseString(clazz, rootElementType);
        createParseJson(clazz, rootElementType, rootElement);
    }

    private void createParseString(JDefinedClass clazz, JType rootElementType) {
        JMethod parse = clazz.method(JMod.PUBLIC | JMod.STATIC, rootElementType, PARSE);
        JVar jsonString = parse.param(mCodeModel.ref(STRING), JSON_STRING);
        JBlock body = parse.body();

        CodeModelUtils.ifNullReturnNull(body, jsonString);
        body._if(jsonString.invoke(IS_EMPTY))._then()._return(JExpr._null());

        JClass jsonObjectType = mCodeModel.ref(JSONObject.class);
        JVar json = body.decl(jsonObjectType, JSON, JExpr._new(jsonObjectType).arg(jsonString));

        body._return(JExpr.invoke(PARSE).arg(json));
    }

    private void createParseJson(JDefinedClass clazz, JType rootElementType, TypeElement rootElement) {
        JMethod parse = clazz.method(JMod.PUBLIC | JMod.STATIC, rootElementType, PARSE);
        JClass jsonObjectType = mCodeModel.ref(JSONObject.class);
        JVar json = parse.param(jsonObjectType, JSON);
        mBody = parse.body();

        CodeModelUtils.ifNullReturnNull(mBody, json);

        JVar dto = mBody.decl(rootElementType, DTO, JExpr._new(rootElementType));

        processElements(dto, json, rootElement);

        mBody._return(dto);
    }

    private void processElements(JExpression dto, JExpression json, TypeElement rootElement) {
        for (Element element : rootElement.getEnclosedElements()) {
            AnnotationMirror mirror = CodeUtils.findAnnotationValue(element, XmlElement.class);
            if (mirror == null) {
                continue;
            }

            switch (element.getKind()) {
                case FIELD:
                    processField(dto, json, element, mirror);
                    break;

                case METHOD:
                    processSetter(dto, json, element, mirror);
                    break;
            }
        }
    }

    private void processField(JExpression dto, JExpression json, Element element, AnnotationMirror mirror) {
        String fieldName = element.getSimpleName().toString();
        String key = CodeUtils.extractValue(mirror, NAME, String.class, fieldName);
        TypeMirror fieldType = element.asType();
        JExpression value = optFieldValue(json, fieldType, element, key);
        JFieldRef fieldRef = dto.ref(fieldName);
        mBody.assign(fieldRef, value);
    }

    private void processSetter(JExpression dto, JExpression json, Element element, AnnotationMirror mirror) {
        String key = CodeUtils.extractValue(mirror, NAME, String.class, null);
        if (key == null) {
            String methodName = element.getSimpleName().toString();
            key = CodeUtils.getSetFieldName(methodName);
        }
        optMethodValue(dto, json, element, key);
    }

    private JExpression optFieldValue(JExpression json, TypeMirror fieldType, Element field, String key) {
        String typeString = fieldType.toString();

        if (char.class.getName().equals(typeString) || Character.class.getName().equals(typeString)) {
            return optCharValue(json, key);
        } else {
            String jsonGetType = JsonUtils.getGetType(typeString);
            if (jsonGetType != null) {
                return optPrimitiveValue(json, fieldType, key, typeString, jsonGetType);
            } else if (CodeUtils.isEnum(processingEnv, fieldType)) {
                return optEnumValue(json, fieldType, key);
            } else {
                Element typeElement = processingEnv.getTypeUtils().asElement(fieldType);
                AnnotationMirror xmlRootElementAnnotation = CodeUtils.findAnnotationValue(typeElement, XmlRootElement.class);
                if (xmlRootElementAnnotation != null) {
                    return optParseValue(json, fieldType, key);
                } else {
                    return optClassValue(json, fieldType, field, key, typeString);
                }
            }
        }
    }

    private JExpression optPrimitiveValue(JExpression json, TypeMirror fieldType, String key, String typeString, String jsonGetType) {
        String getMethod = OPT + jsonGetType;

        JExpressionImpl getValue = json.invoke(getMethod).arg(key);
        if (JsonUtils.needsCast(typeString)) {
            if (fieldType.getKind() == TypeKind.DECLARED) {
                typeString = CodeUtils.getWrappedType(typeString);
            }
            JType castType = mCodeModel.ref(typeString);
            getValue = JExpr.cast(castType, getValue);
        }
        return getValue;
    }

    private JExpression optCharValue(JExpression json, String key) {
        JExpression getString = json.invoke(OPT_STRING).arg(key);

        JVar charString = mBody.decl(mCodeModel.ref(STRING), getTempName(), getString);

        JExpression notEmpty = charString.invoke(IS_EMPTY).not();
        JExpression firstChar = charString.invoke(CHAR_AT).arg(JExpr.lit(0));
        JExpression nullChar = JExpr.lit(NULL_CHAR);
        JExpression getChar = JOp.cond(notEmpty, firstChar, nullChar);
        return getChar;
    }

    private JExpression optEnumValue(JExpression json, TypeMirror fieldType, String key) {
        JClass enumType = mCodeModel.ref(fieldType.toString());

        JExpression optString = json.invoke(OPT_STRING).arg(key);
        JVar enumString = mBody.decl(mCodeModel.ref(STRING), getTempName(), optString);

        JExpression notEmpty = enumString.invoke(IS_EMPTY).not();
        JExpression enumValue = enumType.staticInvoke(VALUE_OF).arg(enumString);
        JExpression nullValue = JExpr._null();
        JExpression getEnum = JOp.cond(notEmpty, enumValue, nullValue);

        return getEnum;
    }

    private JExpression optClassValue(JExpression json, TypeMirror typeMirror, Element field, String key, String typeString) {
        JType fieldType = mCodeModel.ref(typeString);
        String tempName = getTempName();
        JExpression object = mBody.decl(mCodeModel.ref(typeString), tempName, JExpr._new(fieldType));

        JExpressionImpl optJsonObject = json.invoke(OPT_JSON_OBJECT).arg(key);
        JClass jsonObjectType = mCodeModel.ref(JSONObject.class);
        JExpression fieldJson = mBody.decl(jsonObjectType, tempName + JSON_SUFFIX, optJsonObject);

        TypeElement fieldTypeElement = (TypeElement) processingEnv.getTypeUtils().asElement(typeMirror);

        JBlock ifNotNull = mBody._if(fieldJson.ne(JExpr._null()))._then();
        JBlock body = mBody;
        mBody = ifNotNull;
        processElements(object, fieldJson, fieldTypeElement);
        mBody = body;

        return object;
    }

    private JExpression optParseValue(JExpression json, TypeMirror fieldType, String key) {
        JExpressionImpl jsonObject = json.invoke(OPT_JSON_OBJECT).arg(key);

        Element typeElement = processingEnv.getTypeUtils().asElement(fieldType);
        String packageName = CodeUtils.getPackage((TypeElement) typeElement).toString();
        String simpleName = typeElement.getSimpleName().toString();

        JClass parserClass = mCodeModel.ref(packageName + "." + simpleName + JSON_PARSER);
        JExpression parseValue = parserClass.staticInvoke(PARSE).arg(jsonObject);
        return parseValue;
    }

    private void optMethodValue(JExpression dto, JExpression json, Element element, String key) {
        ExecutableElement method = (ExecutableElement) element;

        List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            return;
        }
        VariableElement valueArgElement = parameters.get(0);
        TypeMirror valueType = valueArgElement.asType();

        JExpression value = optFieldValue(json, valueType, valueArgElement, key);
        String methodName = method.getSimpleName().toString();
        mBody.invoke(dto, methodName).arg(value);
    }

    private String getTempName() {
        return TEMP + mAutoincrement++;
    }

    @Override
    protected Class<?>[] getSupportedAnnotations() {
        return new Class[]{
                XmlRootElement.class
        };
    }
}
