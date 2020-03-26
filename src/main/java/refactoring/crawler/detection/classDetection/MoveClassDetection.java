package refactoring.crawler.detection.classDetection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import refactoring.crawler.graph.Edge;
import refactoring.crawler.graph.NamedDirectedMultigraph;
import refactoring.crawler.graph.Node;

public class MoveClassDetection extends ClassDetection {

  public MoveClassDetection(NamedDirectedMultigraph graph, NamedDirectedMultigraph graph2) {
    super(graph, graph2);
  }

  public double computeLikeliness(Node nodeOriginal, Node nodeVersion) {
    double edgeGrade;
    createCallGraph(nodeOriginal, nodeVersion);
    List<Edge> incomingEdgesOriginal =
        filterNamedEdges(new LinkedList<>(graph1.incomingEdgesOf(nodeOriginal)));
    List<Edge> incomingEdgesVersion =
        filterNamedEdges(new LinkedList<>(graph2.incomingEdgesOf(nodeVersion)));
    edgeGrade = computeLikelinessIncomingEdges(incomingEdgesOriginal, incomingEdgesVersion);
    return edgeGrade;
  }

  public List<Node[]> pruneOriginalCandidates(List<Node[]> candidates) {
    List<Node[]> prePrunedClasses = super.pruneOriginalCandidatesImpl(candidates);
    List<Node[]> candidatesInDifferentPackages = new ArrayList<>();
    for (Node[] pair : prePrunedClasses) {
      Node original = pair[0];
      Node version = pair[1];
      String parentPackageOriginal = extractFullyQualifiedParentName(original);
      String parentPackageVersion = extractFullyQualifiedParentName(version);
      if (!isTheSameModuloRename(parentPackageOriginal, parentPackageVersion)
          && ((original.getSimpleName().equals(version.getSimpleName())))) {
        candidatesInDifferentPackages.add(pair);
      }
    }
    return candidatesInDifferentPackages;
  }

  public boolean isRename() {
    return false;
  }
}
