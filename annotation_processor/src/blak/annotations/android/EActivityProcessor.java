/**
 * Copyright (C) 2010-2013 eBusiness Information, Excilys Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package blak.annotations.android;

import blak.annotations.EActivity;
import blak.annotations.utils.ALog;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import static com.sun.codemodel.JExpr._super;

import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;

public class EActivityProcessor implements GeneratingElementProcessor {
    ProcessingEnvironment env;

    public EActivityProcessor(ProcessingEnvironment processingEnv) {
        env = processingEnv;
    }

    @Override
    public String getTarget() {
        return EActivity.class.getName();
    }

    @Override
    public void process(Element element, JCodeModel codeModel, EBeansHolder eBeansHolder) throws Exception {
        TypeElement typeElement = (TypeElement) element;
        String annotatedActivityQualifiedName = typeElement.getQualifiedName().toString();

        ALog.print(env, "EActivityProcessor", annotatedActivityQualifiedName);

        String subActivityQualifiedName = annotatedActivityQualifiedName + ModelConstants.GENERATION_SUFFIX;

        JDefinedClass generatedClass = codeModel._class(subActivityQualifiedName, ClassType.CLASS);

        EBeanHolder holder = eBeansHolder.create(element, EActivity.class, generatedClass);

        JClass annotatedActivity = codeModel.directClass(annotatedActivityQualifiedName);

        holder.generatedClass._extends(annotatedActivity);

        holder.contextRef = JExpr._this();

        JClass bundleClass = holder.classes().BUNDLE;

        // beforeSetContentView
        JMethod init = holder.generatedClass.method(JMod.PRIVATE, codeModel.VOID, "init_");
        holder.initBody = init.body();
        holder.beforeCreateSavedInstanceStateParam = init.param(bundleClass, "savedInstanceState");

        {
            // init if activity
            holder.initIfActivityBody = holder.initBody;
            holder.initActivityRef = JExpr._this();
        }

        // onCreate
        JMethod onCreate = holder.generatedClass.method(JMod.PUBLIC, codeModel.VOID, "onCreate");
        onCreate.annotate(Override.class);
        JVar onCreateSavedInstanceState = onCreate.param(bundleClass, "savedInstanceState");

        boolean usesGreenDroid = false;

        // onCreateBody
        {
            JBlock onCreateBody = onCreate.body();

            JVar previousNotifier = holder.replacePreviousNotifier(onCreateBody);

            onCreateBody.invoke(init).arg(onCreateSavedInstanceState);

            onCreateBody.invoke(_super(), onCreate).arg(onCreateSavedInstanceState);

            holder.resetPreviousNotifier(onCreateBody, previousNotifier);

            //List<JFieldRef> fieldRefs = annotationHelper.extractAnnotationFieldRefs(holder, element, getTarget(), rClass.get(Res.LAYOUT), false);
            //
            //JFieldRef contentViewId;
            //if (fieldRefs.size() == 1) {
            //    contentViewId = fieldRefs.get(0);
            //} else {
            //    contentViewId = null;
            //}

            //if (contentViewId != null) {
            //    // GreenDroid support
            //    if (usesGreenDroid) {
            //        onCreateBody.invoke("setActionBarContentView").arg(contentViewId);
            //    } else {
            //        onCreateBody.invoke("setContentView").arg(contentViewId);
            //    }
            //}
        }

        // Overriding setContentView (with layout id param)
        JClass layoutParamsClass = holder.classes().VIEW_GROUP_LAYOUT_PARAMS;

        String setContentViewMethodName;
        if (usesGreenDroid) {
            setContentViewMethodName = "setActionBarContentView";
        } else {
            setContentViewMethodName = "setContentView";
        }

        //setContentViewMethod(setContentViewMethodName, codeModel, holder, new JType[] { codeModel.INT }, new String[] { "layoutResID" });
        //setContentViewMethod(setContentViewMethodName, codeModel, holder, new JType[] { holder.classes().VIEW, layoutParamsClass }, new String[] { "view", "params" });
        //setContentViewMethod(setContentViewMethodName, codeModel, holder, new JType[] { holder.classes().VIEW }, new String[] { "view" });

        // Handling onBackPressed
        //Element declaredOnBackPressedMethod = getOnBackPressedMethod(typeElement);
        //if (declaredOnBackPressedMethod != null) {
        //
        //    eBeansHolder.generateApiClass(declaredOnBackPressedMethod, SdkVersionHelper.class);
        //
        //    JMethod onKeyDownMethod = holder.generatedClass.method(PUBLIC, codeModel.BOOLEAN, "onKeyDown");
        //    onKeyDownMethod.annotate(Override.class);
        //    JVar keyCodeParam = onKeyDownMethod.param(codeModel.INT, "keyCode");
        //    JClass keyEventClass = holder.classes().KEY_EVENT;
        //    JVar eventParam = onKeyDownMethod.param(keyEventClass, "event");
        //
        //    JClass versionHelperClass = holder.refClass(SdkVersionHelper.class);
        //
        //    JInvocation sdkInt = versionHelperClass.staticInvoke("getSdkInt");
        //
        //    JBlock onKeyDownBody = onKeyDownMethod.body();
        //
        //    onKeyDownBody._if( //
        //            sdkInt.lt(JExpr.lit(5)) //
        //                    .cand(keyCodeParam.eq(keyEventClass.staticRef("KEYCODE_BACK"))) //
        //                    .cand(eventParam.invoke("getRepeatCount").eq(JExpr.lit(0)))) //
        //            ._then() //
        //            .invoke("onBackPressed");
        //
        //    onKeyDownBody._return( //
        //            JExpr._super().invoke(onKeyDownMethod) //
        //                    .arg(keyCodeParam) //
        //                    .arg(eventParam));
        //
        //}
        //
        //boolean addFragmentIntent = androidManifest.getMinSdkVersion() >= MIN_SDK_WITH_FRAGMENT_SUPPORT;
        //aptCodeModelHelper.addActivityIntentBuilder(codeModel, holder, annotationHelper, addFragmentIntent);

    }
}
