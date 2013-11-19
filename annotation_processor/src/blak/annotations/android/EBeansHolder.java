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

import blak.annotations.utils.OriginatingElements;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import javax.lang.model.element.Element;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EBeansHolder {


    private final Map<Element, EBeanHolder> eBeanHolders = new HashMap<Element, EBeanHolder>();

    private final JCodeModel codeModel;

    private final Map<String, JClass> loadedClasses = new HashMap<String, JClass>();

    private final Set<Class<?>> apiClassesToGenerate = new HashSet<Class<?>>();

    private final OriginatingElements originatingElements = new OriginatingElements();

    public EBeansHolder(JCodeModel codeModel) {
        this.codeModel = codeModel;
        refClass(CanonicalNameConstants.STRING);
        preloadJavaLangClasses();
    }

    private void preloadJavaLangClasses() {
        loadedClasses.put(String.class.getName(), refClass(String.class));
        loadedClasses.put(Object.class.getName(), refClass(Object.class));
    }

    public EBeanHolder create(Element element, Class<? extends Annotation> eBeanAnnotation, JDefinedClass generatedClass) {

        String qualifiedName = generatedClass.fullName();

        originatingElements.add(qualifiedName, element);

        EBeanHolder activityHolder = new EBeanHolder(this, eBeanAnnotation, generatedClass);
        eBeanHolders.put(element, activityHolder);
        return activityHolder;
    }

    public EBeanHolder getEBeanHolder(Element element) {
        return eBeanHolders.get(element);
    }

    public JClass refClass(Class<?> clazz) {
        return codeModel.ref(clazz);
    }

    public JClass refClass(String fullyQualifiedClassName) {

        int arrayCounter = 0;
        while (fullyQualifiedClassName.endsWith("[]")) {
            arrayCounter++;
            fullyQualifiedClassName = fullyQualifiedClassName.substring(0, fullyQualifiedClassName.length() - 2);
        }

        JClass refClass = loadedClasses.get(fullyQualifiedClassName);

        if (refClass == null) {
            refClass = codeModel.directClass(fullyQualifiedClassName);
            loadedClasses.put(fullyQualifiedClassName, refClass);
        }

        for (int i = 0; i < arrayCounter; i++) {
            refClass = refClass.array();
        }

        return refClass;
    }

    public JDefinedClass definedClass(String fullyQualifiedClassName) {
        JDefinedClass refClass = (JDefinedClass) loadedClasses.get(fullyQualifiedClassName);
        if (refClass == null) {
            try {
                refClass = codeModel._class(fullyQualifiedClassName);
            } catch (JClassAlreadyExistsException e) {
                refClass = (JDefinedClass) refClass(fullyQualifiedClassName);
            }
            loadedClasses.put(fullyQualifiedClassName, refClass);
        }
        return refClass;
    }

    public JCodeModel codeModel() {
        return codeModel;
    }

    public OriginatingElements getOriginatingElements() {
        return originatingElements;
    }

    public Set<Class<?>> getApiClassesToGenerate() {
        return apiClassesToGenerate;
    }

    public void generateApiClass(Element originatingElement, Class<?> apiClass) {
        originatingElements.add(apiClass.getCanonicalName(), originatingElement);
        apiClassesToGenerate.add(apiClass);
    }
}
