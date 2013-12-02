package blak.annotations.json;

import blak.annotations.Java;
import blak.annotations.utils.ALog;
import blak.annotations.utils.CodeModelUtils;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class JsonParseGenerator {
    private static final String TEMP = "temp";
    private static final Pattern JAVA_LANG_PATTERN = Pattern.compile("^java.lang.");
    private static final String JSON_STRING = "jsonString";

    private final JCodeModel mCodeModel;
    private final JClass mStringClass;
    private final JClass mJsonClass;
    private int mAutoincrement;

    public JsonParseGenerator(JCodeModel codeModel) {
        mCodeModel = codeModel;
        mStringClass = mCodeModel.ref(String.class);
        mJsonClass = mCodeModel.ref(JSONObject.class);
    }

    public void createParseString(JDefinedClass clazz, JType rootElementType) {
        JMethod parse = clazz.method(JMod.PUBLIC | JMod.STATIC, rootElementType, JsonProcessor.PARSE);
        JVar jsonString = parse.param(mStringClass, JSON_STRING);
        JBlock body = parse.body();

        CodeModelUtils.ifNullReturnNull(body, jsonString);
        body._if(jsonString.invoke(Java.IS_EMPTY))._then()._return(JExpr._null());

        JClass jsonObjectType = mJsonClass;
        JVar json = body.decl(jsonObjectType, JsonProcessor.JSON, JExpr._new(jsonObjectType).arg(jsonString));

        body._return(JExpr.invoke(JsonProcessor.PARSE).arg(json));
    }

    public JExpression optPrimitiveValue(JExpression json, TypeMirror fieldType, String jsonGetType, JExpression key, String defaultValue) {
        String typeString = fieldType.toString();
        String optMethod = Json.OPT + jsonGetType;
        JExpression defaultExpr = CodeModelUtils.getDefaultValue(typeString, defaultValue);

        JInvocation getValueInvoke = json.invoke(optMethod).arg(key);
        if (defaultExpr != null) {
            getValueInvoke.arg(defaultExpr);
        }

        if (JsonUtils.needsCast(typeString)) {
            return castPrimitive(getValueInvoke, fieldType);
        }

        return getValueInvoke;
    }

    // Except Integer and Character
    private JExpression castPrimitive(JExpression value, TypeMirror type) {
        String typeString = type.toString();
        if (type.getKind() == TypeKind.DECLARED) {
            typeString = JAVA_LANG_PATTERN.matcher(typeString).replaceAll("").toLowerCase();
        }
        JType castType = mCodeModel.ref(typeString);
        return JExpr.cast(castType, value);
    }

    public JExpression optCharValue(JBlock block, JExpression json, JExpression key, String defaultValue) {
        JVar tempString = optTempString(block, json, key);
        JExpression notEmpty = CodeModelUtils.notEmpty(tempString);
        JExpression firstChar = tempString.invoke(Java.CHAR_AT).arg(JExpr.lit(0));
        JExpression defaultChar = CodeModelUtils.getDefaultChar(defaultValue);
        JExpression getChar = JOp.cond(notEmpty, firstChar, defaultChar);
        return getChar;
    }

    public JExpression optEnumValue(JBlock block, JExpression json, TypeMirror type, JExpression key) {
        JVar tempString = optTempString(block, json, key);
        JClass enumType = mCodeModel.ref(type.toString());

        JExpression notEmpty = CodeModelUtils.notEmpty(tempString);
        JExpression enumValue = enumType.staticInvoke(Java.VALUE_OF).arg(tempString);
        JExpression nullValue = JExpr._null();
        JExpression getEnum = JOp.cond(notEmpty, enumValue, nullValue);
        return getEnum;
    }

    public JExpression optParseValue(JExpression json, Element typeElement, JExpression key) {
        JExpression jsonObject = json.invoke(Json.OPT_JSON_OBJECT).arg(key);
        String parserFullName = JsonProcessor.getFullParserName(typeElement);
        JClass parserClass = mCodeModel.ref(parserFullName);
        JExpression parseValue = parserClass.staticInvoke(JsonProcessor.PARSE).arg(jsonObject);
        return parseValue;
    }

    public JExpression optClassValue(JsonProcessor processor, JBlock block, JExpression json, TypeMirror typeMirror, TypeElement fieldTypeElement, JExpression key) {
        String typeString = typeMirror.toString();
        JType fieldType = mCodeModel.ref(typeString);
        String tempName = getTempName();

        JExpressionImpl optJsonObject = json.invoke(Json.OPT_JSON_OBJECT).arg(key);
        JClass jsonObjectType = mCodeModel.ref(JSONObject.class);
        JExpression fieldJson = block.decl(jsonObjectType, tempName + JsonProcessor.JSON_SUFFIX, optJsonObject);

        JBlock ifNotNull = CodeModelUtils.ifNotNull(block, fieldJson);
        processor.setBlock(ifNotNull);
        JVar object = ifNotNull.decl(fieldType, tempName, JExpr._new(fieldType));
        processor.processElements(object, fieldJson, fieldTypeElement);
        return object;
    }

    public void optMethodValue(JsonProcessor processor, JBlock block, JExpression dto, JExpression json, ExecutableElement method, JExpression key, String defaultValue) {
        List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            return;
        }
        VariableElement valueArgElement = parameters.get(0);
        TypeMirror valueType = valueArgElement.asType();

        JExpression value = processor.optFieldValue(json, valueType, key, defaultValue);
        String methodName = method.getSimpleName().toString();
        block.invoke(dto, methodName).arg(value);
    }

    public JExpression optListValue(JsonProcessor processor, JBlock block, JExpression json, TypeMirror typeMirror, JExpression key) {
        JVar jsonArray = block.decl(mCodeModel._ref(JSONArray.class), getTempName(), json.invoke(Json.OPT_JSON_ARRAY).arg(key));
        JBlock ifNotNull = CodeModelUtils.ifNotNull(block, jsonArray);

        String typeString = typeMirror.toString();
        JClass fieldClass = mCodeModel.ref(typeString);
        String concreteTypeString = typeString.replaceAll("^" + List.class.getName(), ArrayList.class.getName());
        JClass concreteClass = mCodeModel.ref(concreteTypeString);
        JVar list = ifNotNull.decl(fieldClass, getTempName(), JExpr._new(concreteClass));

        JForLoop forLoop = ifNotNull._for();
        JVar iVar = forLoop.init(mCodeModel.INT, getTempName(), JExpr.lit(0));
        forLoop.test(JOp.lt(iVar, jsonArray.invoke(Json.LENGTH)));
        forLoop.update(JOp.incr(iVar));
        JBlock forBody = forLoop.body();

        DeclaredType declaredType = (DeclaredType) typeMirror;
        TypeMirror itemType = declaredType.getTypeArguments().get(0);
        processor.setBlock(forBody);
        JExpression jsonItem = processor.optFieldValue(jsonArray, itemType, JExpr.direct(iVar.name()), null);
        forBody.invoke(list, Java.ADD).arg(jsonItem);
        processor.setBlock(ifNotNull);
        return list;
    }

    public JExpression optArrayValue(JsonProcessor processor, JBlock block, JExpression json, ArrayType arrayType, JExpression key) {
        JVar jsonArray = block.decl(mCodeModel._ref(JSONArray.class), getTempName(), json.invoke(Json.OPT_JSON_ARRAY).arg(key));
        JBlock ifNotNull = CodeModelUtils.ifNotNull(block, jsonArray);

        String typeString = arrayType.toString();
        JClass fieldClass = mCodeModel.ref(typeString);
        TypeMirror itemTypeMirror = arrayType.getComponentType();
        JType itemType = mCodeModel.ref(itemTypeMirror.toString());
        JVar array = ifNotNull.decl(fieldClass, getTempName(), JExpr.newArray(itemType, jsonArray.invoke(Json.LENGTH)));

        JForLoop forLoop = ifNotNull._for();
        JVar iVar = forLoop.init(mCodeModel.INT, getTempName(), JExpr.lit(0));
        forLoop.test(JOp.lt(iVar, jsonArray.invoke(Json.LENGTH)));
        forLoop.update(JOp.incr(iVar));
        JBlock forBody = forLoop.body();

        processor.setBlock(forBody);
        JExpression jsonItem = processor.optFieldValue(jsonArray, itemTypeMirror, JExpr.direct(iVar.name()), null);
        forBody.assign(JExpr.component(array, iVar), jsonItem);
        processor.setBlock(ifNotNull);
        return array;
    }

    private JVar optTempString(JBlock block, JExpression json, JExpression key) {
        JExpression optString = json.invoke(Json.OPT_STRING).arg(key);
        JVar tempString = block.decl(mStringClass, getTempName(), optString);
        return tempString;
    }

    public String getTempName() {
        return TEMP + mAutoincrement++;
    }
}
