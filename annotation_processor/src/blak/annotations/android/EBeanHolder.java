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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

import static com.sun.codemodel.JExpr._this;

public class EBeanHolder {

    public final JDefinedClass generatedClass;

    public JExpression contextRef;

    private final EBeansHolder eBeansHolder;
    public final Class<? extends Annotation> eBeanAnnotation;

    private JExpression notifier;

    public EBeanHolder(EBeansHolder eBeansHolder, Class<? extends Annotation> eBeanAnnotation, JDefinedClass generatedClass) {
        this.eBeansHolder = eBeansHolder;
        this.eBeanAnnotation = eBeanAnnotation;
        this.generatedClass = generatedClass;
    }

    public JCodeModel codeModel() {
        return eBeansHolder.codeModel();
    }

    public JClass refClass(String fullyQualifiedClassName) {
        return eBeansHolder.refClass(fullyQualifiedClassName);
    }

    public JClass refClass(Class<?> clazz) {
        return eBeansHolder.refClass(clazz);
    }

    public JDefinedClass definedClass(String fullyQualifiedClassName) {
        return eBeansHolder.definedClass(fullyQualifiedClassName);
    }

    public void generateApiClass(Element originatingElement, Class<?> apiClass) {
        eBeansHolder.generateApiClass(originatingElement, apiClass);
    }

    public void invokeViewChanged(JBlock block) {
        block.invoke(notifier, "notifyViewChanged").arg(_this());
    }
}
