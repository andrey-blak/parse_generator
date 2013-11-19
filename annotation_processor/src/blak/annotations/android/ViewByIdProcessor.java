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

import blak.annotations.ViewById;
import blak.annotations.utils.ALog;
import com.sun.codemodel.JCodeModel;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public class ViewByIdProcessor implements DecoratingElementProcessor {
    ProcessingEnvironment env;

    public ViewByIdProcessor(ProcessingEnvironment processingEnv) {
        env = processingEnv;
    }

    @Override
    public String getTarget() {
        return ViewById.class.getName();
    }

    @Override
    public void process(Element element, JCodeModel codeModel, EBeanHolder holder) {
        String fieldName = element.getSimpleName().toString();

        TypeMirror uiFieldTypeMirror = element.asType();
        String typeQualifiedName = uiFieldTypeMirror.toString();

        ALog.print(env, "ViewByIdProcessor", fieldName, holder.generatedClass);
    }
}
