package blak.annotations.json;

import blak.annotations.Java;
import blak.annotations.utils.CodeModelUtils;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.regex.Pattern;

class JsonParseGenerator {
    private static final String TEMP = "temp";
    private static final Pattern JAVA_LANG_PATTERN = Pattern.compile("^java.lang.");

    private final JCodeModel mCodeModel;
    private final JClass mStringClass;
    private int mAutoincrement;

    public JsonParseGenerator(JCodeModel codeModel) {
        mCodeModel = codeModel;
        mStringClass = mCodeModel.ref(String.class);
    }

    public JExpression optPrimitiveValue(JExpression json, TypeMirror fieldType, String jsonGetType, String key, String defaultValue) {
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

    public JExpression optCharValue(JBlock block, JExpression json, String key, String defaultValue) {
        JVar tempString = optTempString(block, json, key);
        JExpression notEmpty = CodeModelUtils.notEmpty(tempString);
        JExpression firstChar = tempString.invoke(Java.CHAR_AT).arg(JExpr.lit(0));
        JExpression defaultChar = CodeModelUtils.getDefaultChar(defaultValue);
        JExpression getChar = JOp.cond(notEmpty, firstChar, defaultChar);
        return getChar;
    }

    public JExpression optEnumValue(JBlock block, JExpression json, TypeMirror type, String key) {
        JVar tempString = optTempString(block, json, key);
        JClass enumType = mCodeModel.ref(type.toString());

        JExpression notEmpty = CodeModelUtils.notEmpty(tempString);
        JExpression enumValue = enumType.staticInvoke(Java.VALUE_OF).arg(tempString);
        JExpression nullValue = JExpr._null();
        JExpression getEnum = JOp.cond(notEmpty, enumValue, nullValue);
        return getEnum;
    }

    public JExpression optParseValue(ProcessingEnvironment processingEnv, JExpression json, TypeMirror typeMirror, String key) {
        JExpression jsonObject = json.invoke(Json.OPT_JSON_OBJECT).arg(key);
        Element typeElement = processingEnv.getTypeUtils().asElement(typeMirror);
        String parserFullName = JsonProcessor.getFullParserName(typeElement);
        JClass parserClass = mCodeModel.ref(parserFullName);
        JExpression parseValue = parserClass.staticInvoke(JsonProcessor.PARSE).arg(jsonObject);
        return parseValue;
    }

    private JVar optTempString(JBlock block, JExpression json, String key) {
        JExpression optString = json.invoke(Json.OPT_STRING).arg(key);
        JVar tempString = block.decl(mStringClass, getTempName(), optString);
        return tempString;
    }

    public String getTempName() {
        return TEMP + mAutoincrement++;
    }
}
