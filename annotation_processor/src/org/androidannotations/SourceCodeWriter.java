package org.androidannotations;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

// from androidannotations
public class SourceCodeWriter extends CodeWriter {
    private final Filer filer;
    private final Messager message;

    private static final VoidOutputStream VOID_OUTPUT_STREAM = new VoidOutputStream();
    private OriginatingElements originatingElements;

    private static class VoidOutputStream extends OutputStream {
        @Override
        public void write(int arg0) throws IOException {
            // Do nothing
        }
    }

    public SourceCodeWriter(Filer filer, Messager message, OriginatingElements originatingElements) {
        this.filer = filer;
        this.message = message;
        this.originatingElements = originatingElements;
    }

    @Override
    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
        String qualifiedClassName = toQualifiedClassName(pkg, fileName);
        message.printMessage(Kind.NOTE, "Generating source file: " + qualifiedClassName);

        Element[] classOriginatingElements = originatingElements.getClassOriginatingElements(qualifiedClassName);

        try {
            JavaFileObject sourceFile;

            if (classOriginatingElements.length == 0) {
                message.printMessage(Kind.NOTE, "Generating class with no originating element: " + qualifiedClassName);
            }

            sourceFile = filer.createSourceFile(qualifiedClassName, classOriginatingElements);

            return sourceFile.openOutputStream();
        } catch (FilerException e) {
            message.printMessage(Kind.NOTE, "Could not generate source file for " + qualifiedClassName + ", message: " + e.getMessage());
            /*
			 * This exception is expected, when some files are created twice. We
			 * cannot delete existing files, unless using a dirty hack. Files are
			 * created twice when the same file is created from different
			 * annotation rounds. Happens when renaming classes, and for
			 * Background executor. It also probably means I didn't fully
			 * understand how annotation processing works. If anyone can point
			 * me out...
			 */
            return VOID_OUTPUT_STREAM;
        }
    }

    private String toQualifiedClassName(JPackage pkg, String fileName) {
        int suffixPosition = fileName.lastIndexOf('.');
        String className = fileName.substring(0, suffixPosition);

        String qualifiedClassName = pkg.name() + "." + className;
        return qualifiedClassName;
    }

    @Override
    public void close() throws IOException {
    }
}
