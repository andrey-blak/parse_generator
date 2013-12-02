package blak.annotations.json;

import blak.annotations.BaseProcessor;
import blak.annotations.utils.CodeModelUtils;
import blak.annotations.utils.ProcessingUtils;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import org.androidannotations.ErrorHelper;
import org.androidannotations.ProcessingException;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;

// TODO
// collections
// map (from jsonObject keySet)
// generics
// check boxify/unboxify (from JType)
// validation

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class JsonProcessor extends BaseProcessor {
    private static final String JSON_PARSER = "JsonParser";
    public static final String PARSE = "parse";

    public static final String JSON = "json";
    private static final String DTO = "dto";
    private static final String NAME = "name";
    private static final String DEFAULT_VALUE = "defaultValue";

    public static final String JSON_SUFFIX = "Json";

    private JCodeModel mCodeModel;
    private JsonParseGenerator mGenerator;
    private JBlock mBlock;

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
    }

    public JBlock getBlock() {
        return mBlock;
    }

    public void setBlock(JBlock block) {
        mBlock = block;
    }

    private void process(TypeElement rootElement) throws JClassAlreadyExistsException {
        String parserFullName = getFullParserName(rootElement);
        JDefinedClass clazz = mCodeModel._class(parserFullName);

        JClass rootElementType = mCodeModel.ref(rootElement.getQualifiedName().toString());

        mGenerator.createParseString(clazz, rootElementType);
        createParseJson(clazz, rootElementType, rootElement);
    }

    private void createParseJson(JDefinedClass clazz, JType rootElementType, TypeElement rootElement) {
        JMethod parse = clazz.method(JMod.PUBLIC | JMod.STATIC, rootElementType, PARSE);
        JClass jsonObjectType = mCodeModel.ref(Json.JSON_OBJECT);
        JVar json = parse.param(jsonObjectType, JSON);
        mBlock = parse.body();

        CodeModelUtils.ifNullReturnNull(mBlock, json);

        JVar dto = mBlock.decl(rootElementType, DTO, JExpr._new(rootElementType));

        processElements(dto, json, rootElement);

        mBlock._return(dto);
    }

    public void processElements(JExpression dto, JExpression json, TypeElement rootElement) {
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
        String keyString = ProcessingUtils.extractValue(mirror, NAME, String.class, fieldName);
        JExpression key = JExpr.lit(keyString);
        TypeMirror fieldType = element.asType();

        JBlock block = mBlock;
        JExpression value = optFieldValue(json, fieldType, key, defaultValue);
        JFieldRef fieldRef = dto.ref(fieldName);
        mBlock.assign(fieldRef, value);
        mBlock = block;
    }

    private void processSetter(JExpression dto, JExpression json, Element element, AnnotationMirror mirror, String defaultValue) {
        String keyString = ProcessingUtils.extractValue(mirror, NAME, String.class, null);
        if (keyString == null) {
            String methodName = element.getSimpleName().toString();
            keyString = ProcessingUtils.getSetFieldName(methodName);
        }
        ExecutableElement method = (ExecutableElement) element;
        JExpression key = JExpr.lit(keyString);
        mGenerator.optMethodValue(this, mBlock, dto, json, method, key, defaultValue);
    }

    public JExpression optFieldValue(JExpression json, TypeMirror fieldType, JExpression key, String defaultValue) {
        String typeString = fieldType.toString();

        if (ProcessingUtils.isChar(typeString)) {
            return mGenerator.optCharValue(mBlock, json, key, defaultValue);
        }
        String jsonGetType = JsonUtils.getGetType(typeString);
        if (jsonGetType != null) {
            return mGenerator.optPrimitiveValue(json, fieldType, jsonGetType, key, defaultValue);
        }
        if (ProcessingUtils.isEnum(processingEnv, fieldType)) {
            return mGenerator.optEnumValue(mBlock, json, fieldType, key);
        }

        Types typeUtils = processingEnv.getTypeUtils();
        TypeElement fieldTypeElement = (TypeElement) typeUtils.asElement(fieldType);

        if (ProcessingUtils.isAssignableFrom(typeUtils, fieldType, List.class)) {
            return mGenerator.optListValue(this, mBlock, json, fieldType, key);
        }
        if (ProcessingUtils.isAssignableFrom(typeUtils, fieldType, Set.class)) {
            return mGenerator.optListValue(this, mBlock, json, fieldType, key);
        }

        if (fieldType instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) fieldType;
            return mGenerator.optArrayValue(this, mBlock, json, arrayType, key);
        }

        if (ProcessingUtils.haveAnnotation(fieldTypeElement, XmlRootElement.class)) {
            return mGenerator.optParseValue(json, fieldTypeElement, key);
        }

        return mGenerator.optClassValue(this, mBlock, json, fieldType, fieldTypeElement, key);
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
