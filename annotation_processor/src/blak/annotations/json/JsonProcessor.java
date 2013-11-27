package blak.annotations.json;

import blak.annotations.BaseProcessor;
import blak.annotations.Java;
import blak.annotations.utils.CodeModelUtils;
import blak.annotations.utils.ProcessingUtils;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import org.androidannotations.ErrorHelper;
import org.androidannotations.ProcessingException;
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
// required
// arrays & collections (maps?)
// generics
// validation
// extract constants

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class JsonProcessor extends BaseProcessor {
    private static final String JSON_PARSER = "JsonParser";
    private static final String PARSE = "parse";
    private static final String JSON_STRING = "jsonString";
    private static final String JSON = "json";
    private static final String DTO = "dto";
    private static final String NAME = "name";
    private static final String DEFAULT_VALUE = "defaultValue";
    private static final char NULL_CHAR = '\0';
    private static final String JSON_SUFFIX = "Json";
    private static final String TEMP = "temp"; // Nns

    private JCodeModel mCodeModel;
    private JBlock mBody;
    private int mAutoincrement;

    private JClass mStringClass;
    private JClass mJsonClass;

    @Override
    public boolean processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        initCodeModel();
        try {
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

    private void initCodeModel() {
        mCodeModel = new JCodeModel();
        mStringClass = mCodeModel.ref(String.class);
        mJsonClass = mCodeModel.ref(JSONObject.class);
    }

    private void process(RoundEnvironment roundEnv, TypeElement rootElement) throws JClassAlreadyExistsException {
        String parserFullName = getFullParserName(rootElement);
        JDefinedClass clazz = mCodeModel._class(parserFullName);

        JClass rootElementType = mCodeModel.ref(rootElement.getQualifiedName().toString());

        createParseString(clazz, rootElementType);
        createParseJson(clazz, rootElementType, rootElement);
    }

    private void createParseString(JDefinedClass clazz, JType rootElementType) {
        JMethod parse = clazz.method(JMod.PUBLIC | JMod.STATIC, rootElementType, PARSE);
        JVar jsonString = parse.param(mStringClass, JSON_STRING);
        JBlock body = parse.body();

        CodeModelUtils.ifNullReturnNull(body, jsonString);
        body._if(jsonString.invoke(Java.IS_EMPTY))._then()._return(JExpr._null());

        JClass jsonObjectType = mJsonClass;
        JVar json = body.decl(jsonObjectType, JSON, JExpr._new(jsonObjectType).arg(jsonString));

        body._return(JExpr.invoke(PARSE).arg(json));
    }

    private void createParseJson(JDefinedClass clazz, JType rootElementType, TypeElement rootElement) {
        JMethod parse = clazz.method(JMod.PUBLIC | JMod.STATIC, rootElementType, PARSE);
        JClass jsonObjectType = mJsonClass;
        JVar json = parse.param(jsonObjectType, JSON);
        mBody = parse.body();

        CodeModelUtils.ifNullReturnNull(mBody, json);

        JVar dto = mBody.decl(rootElementType, DTO, JExpr._new(rootElementType));

        processElements(dto, json, rootElement);

        mBody._return(dto);
    }

    private void processElements(JExpression dto, JExpression json, TypeElement rootElement) {
        for (Element element : rootElement.getEnclosedElements()) {
            AnnotationMirror mirror = ProcessingUtils.findAnnotationValue(element, XmlElement.class);
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
        String key = ProcessingUtils.extractValue(mirror, NAME, String.class, fieldName);
        TypeMirror fieldType = element.asType();
        String defaultValue = ProcessingUtils.extractValue(mirror, DEFAULT_VALUE, String.class, null);

        JBlock block = mBody;
        JExpression value = optFieldValue(json, fieldType, element, key, defaultValue);
        JFieldRef fieldRef = dto.ref(fieldName);
        mBody.assign(fieldRef, value);
        mBody = block;
    }

    private void processSetter(JExpression dto, JExpression json, Element element, AnnotationMirror mirror) {
        String key = ProcessingUtils.extractValue(mirror, NAME, String.class, null);
        if (key == null) {
            String methodName = element.getSimpleName().toString();
            key = ProcessingUtils.getSetFieldName(methodName);
        }
        String defaultValue = ProcessingUtils.extractValue(mirror, DEFAULT_VALUE, String.class, null);
        optMethodValue(dto, json, element, key, defaultValue);
    }

    private JExpression optFieldValue(JExpression json, TypeMirror fieldType, Element field, String key, String defaultValue) {
        String typeString = fieldType.toString();

        if (ProcessingUtils.isChar(typeString)) {
            return optCharValue(json, fieldType, key, defaultValue);
        }
        String jsonGetType = JsonUtils.getGetType(typeString);
        if (jsonGetType != null) {
            return optPrimitiveValue(json, fieldType, key, typeString, jsonGetType, defaultValue);
        }
        if (ProcessingUtils.isEnum(processingEnv, fieldType)) {
            return optEnumValue(json, fieldType, key);
        }
        Element typeElement = processingEnv.getTypeUtils().asElement(fieldType);
        AnnotationMirror xmlRootElementAnnotation = ProcessingUtils.findAnnotationValue(typeElement, XmlRootElement.class);
        if (xmlRootElementAnnotation != null) {
            return optParseValue(json, fieldType, key);
        }
        return optClassValue(json, fieldType, key, typeString);
    }

    private JExpression optPrimitiveValue(JExpression json, TypeMirror fieldType, String key, String typeString, String jsonGetType, String defaultValue) {
        String optMethod = Json.OPT + jsonGetType;
        JExpression defaultExpr = CodeModelUtils.getDefaultValue(fieldType, defaultValue);

        JInvocation getValueInvoke = json.invoke(optMethod).arg(key);
        if (defaultExpr != null) {
            getValueInvoke.arg(defaultExpr);
        }

        JExpression value;
        if (JsonUtils.needsCast(typeString)) {
            if (fieldType.getKind() == TypeKind.DECLARED) {
                typeString = ProcessingUtils.getWrappedType(typeString);
            }
            JType castType = mCodeModel.ref(typeString);
            value = JExpr.cast(castType, getValueInvoke);
        } else {
            value = getValueInvoke;
        }

        return value;
    }

    private JExpression optCharValue(JExpression json, TypeMirror fieldType, String key, String defaultValue) {
        JExpression getString = json.invoke(Json.OPT_STRING).arg(key);

        JVar charString = mBody.decl(mStringClass, getTempName(), getString);

        JExpression notEmpty = charString.invoke(Java.IS_EMPTY).not();
        JExpression firstChar = charString.invoke(Java.CHAR_AT).arg(JExpr.lit(0));

        JExpression defaultChar;
        if (defaultValue != null) {
            defaultChar = CodeModelUtils.getDefaultValue(fieldType, defaultValue);
        } else {
            defaultChar = JExpr.lit(NULL_CHAR);
        }

        JExpression getChar = JOp.cond(notEmpty, firstChar, defaultChar);
        return getChar;
    }

    private JExpression optEnumValue(JExpression json, TypeMirror fieldType, String key) {
        JClass enumType = mCodeModel.ref(fieldType.toString());

        JExpression optString = json.invoke(Json.OPT_STRING).arg(key);
        JVar enumString = mBody.decl(mStringClass, getTempName(), optString);

        JExpression notEmpty = enumString.invoke(Java.IS_EMPTY).not();
        JExpression enumValue = enumType.staticInvoke(Java.VALUE_OF).arg(enumString);
        JExpression nullValue = JExpr._null();
        JExpression getEnum = JOp.cond(notEmpty, enumValue, nullValue);

        return getEnum;
    }

    private JExpression optClassValue(JExpression json, TypeMirror typeMirror, String key, String typeString) {
        JType fieldType = mCodeModel.ref(typeString);
        String tempName = getTempName();

        JExpressionImpl optJsonObject = json.invoke(Json.OPT_JSON_OBJECT).arg(key);
        JClass jsonObjectType = mJsonClass;
        JExpression fieldJson = mBody.decl(jsonObjectType, tempName + JSON_SUFFIX, optJsonObject);

        TypeElement fieldTypeElement = (TypeElement) processingEnv.getTypeUtils().asElement(typeMirror);

        JBlock ifNotNull = mBody._if(fieldJson.ne(JExpr._null()))._then();
        mBody = ifNotNull;
        JVar object = mBody.decl(fieldType, tempName, JExpr._new(fieldType));
        processElements(object, fieldJson, fieldTypeElement);
        return object;
    }

    private JExpression optParseValue(JExpression json, TypeMirror fieldType, String key) {
        JExpressionImpl jsonObject = json.invoke(Json.OPT_JSON_OBJECT).arg(key);
        Element typeElement = processingEnv.getTypeUtils().asElement(fieldType);
        String parserFullName = getFullParserName(typeElement);
        JClass parserClass = mCodeModel.ref(parserFullName);
        JExpression parseValue = parserClass.staticInvoke(PARSE).arg(jsonObject);
        return parseValue;
    }

    private void optMethodValue(JExpression dto, JExpression json, Element element, String key, String defaultValue) {
        ExecutableElement method = (ExecutableElement) element;

        List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            return;
        }
        VariableElement valueArgElement = parameters.get(0);
        TypeMirror valueType = valueArgElement.asType();

        JExpression value = optFieldValue(json, valueType, valueArgElement, key, defaultValue);
        String methodName = method.getSimpleName().toString();
        mBody.invoke(dto, methodName).arg(value);
    }

    private String getTempName() {
        return TEMP + mAutoincrement++;
    }

    private String getFullParserName(Element rootElement) {
        String packageName = ProcessingUtils.getPackage(rootElement).toString();
        String simpleName = rootElement.getSimpleName().toString();
        String parserFullName = packageName + "." + simpleName + JSON_PARSER;
        return parserFullName;
    }

    @Override
    protected Class<?>[] getSupportedAnnotations() {
        return new Class[]{
                XmlRootElement.class
        };
    }
}
