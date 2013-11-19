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
    public static final String GENERATION_SUFFIX = "_";

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
        String activityQualifiedName = typeElement.getQualifiedName().toString();
        String subActivityQualifiedName = activityQualifiedName + GENERATION_SUFFIX;
        JDefinedClass generatedClass = codeModel._class(subActivityQualifiedName, ClassType.CLASS);
        EBeanHolder holder = eBeansHolder.create(element, EActivity.class, generatedClass);

        JClass annotatedActivity = codeModel.directClass(activityQualifiedName);

        holder.generatedClass._extends(annotatedActivity);
        holder.contextRef = JExpr._this();
    }
}
