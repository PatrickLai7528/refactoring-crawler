package refactoring.crawler.detection.methodDetection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import refactoring.crawler.detection.RefactoringDetection;
import refactoring.crawler.detection.SearchHelper;
import refactoring.crawler.graph.Edge;
import refactoring.crawler.graph.MethodNode;
import refactoring.crawler.graph.NamedDirectedMultigraph;
import refactoring.crawler.graph.Node;

public abstract class MethodDetection extends RefactoringDetection {
  public MethodDetection(NamedDirectedMultigraph graph, NamedDirectedMultigraph graph2) {
    super(graph, graph2);
  }

  @Override
  public double computeLikeliness(Node node1, Node node12) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public List<Node[]> pruneOriginalCandidates(List<Node[]> candidates) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isRename() {
    // TODO Auto-generated method stub
    return false;
  }

  public List<Edge> filterNamedEdges(List<Edge> list) {
    List<Edge> results = new ArrayList<>();
    for (Edge value : list) {
      if (Node.Type.METHOD_CALL.equals(value.getLabel())) {
        results.add(value);
      }
    }
    return results;
  }

  public void createCallGraph(Node node, NamedDirectedMultigraph graph) {
    List<String> callers;
    if (this instanceof ChangeMethodSignatureDetection)
      callers = SearchHelper.findMethodCallers(graph, (MethodNode) node, true);
    else callers = SearchHelper.findMethodCallers(graph, (MethodNode) node, false);
    for (String s : callers) {
      Node callerNode = graph.findNamedNode(s);
      if (callerNode != null) {
        graph.addEdge(callerNode, node, new Edge(Node.Type.METHOD_CALL));
      }
    }
    node.setCreatedCallGraph();
  }

  protected void createCallGraph(Node original, Node version) {
    if (!original.hasCallGraph()) {
      createCallGraph(original, graph1);
      original.setCreatedCallGraph();
    }
    if (!version.hasCallGraph()) {
      createCallGraph(version, graph2);
      version.setCreatedCallGraph();
    }
  }

  public double analyzeIncomingEdges(Node original, Node version) {
    double incomingEdgesGrade;
    createCallGraph(original, version);
    List<Edge> incomingEdgesOriginal =
        filterNamedEdges(new LinkedList<>(graph1.incomingEdgesOf(original)));
    List<Edge> incomingEdgesVersion =
        filterNamedEdges(new LinkedList<>(graph2.incomingEdgesOf(version)));
    incomingEdgesGrade =
        computeLikelinessIncomingEdges(incomingEdgesOriginal, incomingEdgesVersion);
    return incomingEdgesGrade;
  }
}
