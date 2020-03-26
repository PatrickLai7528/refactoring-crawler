package refactoring.crawler.graph;

import lombok.Getter;
import lombok.Setter;
import org.jgrapht.graph.DefaultEdge;

public class Edge extends DefaultEdge {

  @Getter @Setter private Node.Type label;

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

  public Node oppositeVertex(Node n) {
    Node source = this.getSource();
    Node target = this.getTarget();
    return n.equals(source) ? target : source;
  }
}
