package refactoring.crawler.util;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import refactoring.crawler.graph.Node;

public class RefactoringCategory {

    @Getter @Setter private String name;

    @Getter @Setter private List<Node[]> refactoringPairs;

    public Node[][] getElements() {
        Node[][] elements = new Node[refactoringPairs.size()][];
        int i = 0;
        for (Node[] pair : refactoringPairs) {
            elements[i++] = pair;
        }
        return elements;
    }

    public String toString() {
        return name + printElements(getElements());
    }

    private String printElements(Node[][] array) {
        StringBuilder res = new StringBuilder();
        for (Node[] nodes : array) {
            res.append("[").append(nodes[0]).append(",").append(nodes[1]).append("]");
        }
        return res.toString();
    }
}
