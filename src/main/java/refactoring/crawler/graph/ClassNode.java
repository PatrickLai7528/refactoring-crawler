package refactoring.crawler.graph;

import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class ClassNode extends Node {

    @Getter @Setter private List<String> classesImported = new LinkedList<>();

    @Getter @Setter private List<String> superClasses = new LinkedList<>();

    /** @param fullyQualifiedName fullyQualifiedName */
    public ClassNode(String fullyQualifiedName) {
        super(fullyQualifiedName, Type.CLASS);
    }
}
