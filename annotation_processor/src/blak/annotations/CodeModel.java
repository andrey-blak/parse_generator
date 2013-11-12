package blak.annotations;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

public class CodeModel {
    private Set<TypeElement> findRootTypeElements(Set<? extends Element> rootElements) {
        Set<TypeElement> rootTypeElements = new HashSet<TypeElement>();
        for (Element element : rootElements) {
            if (element instanceof TypeElement) {
                rootTypeElements.add((TypeElement) element);
            } else {
                Element enclosingElement = element.getEnclosingElement();
                if (enclosingElement instanceof TypeElement) {
                    rootTypeElements.add((TypeElement) enclosingElement);
                }
            }
        }
        return rootTypeElements;
    }
}
