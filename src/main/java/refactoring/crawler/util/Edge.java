package refactoring.crawler.util;

import lombok.Getter;
import lombok.Setter;
import org.jgrapht.graph.DefaultEdge;

public class Edge extends DefaultEdge {

    @Getter
    @Setter
    private Node.Type label;

    public Edge(Node.Type label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
    }

    @Override
    public Node getSource() {
        return (Node) super.getSource();
    }

    @Override
    public Node getTarget() {
        return (Node) super.getTarget();
    }
}
