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
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
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
    public static final String PARSE = "parse";
    private static final String JSON_STRING = "jsonString";
    private static final String JSON = "json";
    private static final String DTO = "dto";
    private static final String NAME = "name";
    private static final String DEFAULT_VALUE = "defaultValue";

    private static final String JSON_SUFFIX = "Json";

    private JCodeModel mCodeModel;
    private JClass mStringClass;
    private JClass mJsonClass;
    private JsonParseGenerator mGenerator;
    private JBlock mBody;

    @Override
    public boolean processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        initCodeModel();
        try {
            Set<? extends Element> rootElements = roundEnv.getElementsAnnotatedWith(XmlRootElement.class);
            for (Element rootElement : rootElements) {
                mGenerator = new JsonParseGenerator(mCodeModel);
                process((TypeElement) rootElement);
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

    private void process(TypeElement rootElement) throws JClassAlreadyExistsException {
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

            String defaultValue = ProcessingUtils.extractValue(mirror, DEFAULT_VALUE, String.class, null);

            switch (element.getKind()) {
                case FIELD:
                    processField(dto, json, element, mirror, defaultValue);
                    break;

                case METHOD:
                    processSetter(dto, json, element, mirror, defaultValue);
                    break;
            }
        }
    }

    private void processField(JExpression dto, JExpression json, Element element, AnnotationMirror mirror, String defaultValue) {
        String fieldName = element.getSimpleName().toString();
        String key = ProcessingUtils.extractValue(mirror, NAME, String.class, fieldName);
        TypeMirror fieldType = element.asType();

        JBlock block = mBody;
        JExpression value = optFieldValue(json, fieldType, key, defaultValue);
        JFieldRef fieldRef = dto.ref(fieldName);
        mBody.assign(fieldRef, value);
        mBody = block;
    }

    private void processSetter(JExpression dto, JExpression json, Element element, AnnotationMirror mirror, String defaultValue) {
        String key = ProcessingUtils.extractValue(mirror, NAME, String.class, null);
        if (key == null) {
            String methodName = element.getSimpleName().toString();
            key = ProcessingUtils.getSetFieldName(methodName);
        }
        optMethodValue(dto, json, element, key, defaultValue);
    }

    private JExpression optFieldValue(JExpression json, TypeMirror fieldType, String key, String defaultValue) {
        String typeString = fieldType.toString();

        if (ProcessingUtils.isChar(typeString)) {
            return mGenerator.optCharValue(mBody, json, key, defaultValue);
        }
        String jsonGetType = JsonUtils.getGetType(typeString);
        if (jsonGetType != null) {
            return mGenerator.optPrimitiveValue(json, fieldType, jsonGetType, key, defaultValue);
        }
        if (ProcessingUtils.isEnum(processingEnv, fieldType)) {
            return mGenerator.optEnumValue(mBody, json, fieldType, key);
        }
        Element typeElement = processingEnv.getTypeUtils().asElement(fieldType);
        if (ProcessingUtils.haveAnnotation(typeElement, XmlRootElement.class)) {
            return mGenerator.optParseValue(processingEnv, json, fieldType, key);
        }
        return optClassValue(json, fieldType, key, typeString);
    }

    private JExpression optClassValue(JExpression json, TypeMirror typeMirror, String key, String typeString) {
        JType fieldType = mCodeModel.ref(typeString);
        String tempName = mGenerator.getTempName();

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

    private void optMethodValue(JExpression dto, JExpression json, Element element, String key, String defaultValue) {
        ExecutableElement method = (ExecutableElement) element;

        List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            return;
        }
        VariableElement valueArgElement = parameters.get(0);
        TypeMirror valueType = valueArgElement.asType();

        JExpression value = optFieldValue(json, valueType, key, defaultValue);
        String methodName = method.getSimpleName().toString();
        mBody.invoke(dto, methodName).arg(value);
    }

    public static String getFullParserName(Element rootElement) {
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
