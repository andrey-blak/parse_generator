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
package org.androidannotations;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHelper {
    public static String getErrorMessage(ProcessingEnvironment processingEnv, ProcessingException e) {
        String errorMessage = e.getCause().getClass().getName() + "\n";
        errorMessage += "Stacktrace: " + stackTraceToString(e.getCause());

        Element element = e.getElement();
        if (element != null) {
            errorMessage += "Thrown from: " + elementContainer(element) + "\n";
            errorMessage += "Element (" + element.getClass().getSimpleName() + "): " + elementFullString(processingEnv, element) + "\n";
        }

        return errorMessage;
    }

    private static String elementFullString(ProcessingEnvironment processingEnv, Element element) {
        Elements elementUtils = processingEnv.getElementUtils();
        CharArrayWriter writer = new CharArrayWriter();
        elementUtils.printElements(writer, element);
        return writer.toString();
    }

    private static String elementContainer(Element element) {
        Element enclosingElement = element.getEnclosingElement();
        return enclosingElement != null ? enclosingElement.toString() : "";
    }

    private static String stackTraceToString(Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        return writer.toString();
    }
}
