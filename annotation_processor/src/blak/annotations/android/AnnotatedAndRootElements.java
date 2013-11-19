package blak.annotations.android;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class AnnotatedAndRootElements {
    public final Element annotatedElement;
    public final TypeElement rootTypeElement;

    public AnnotatedAndRootElements(Element annotatedElement, TypeElement rootTypeElement) {
        this.annotatedElement = annotatedElement;
        this.rootTypeElement = rootTypeElement;
    }
}