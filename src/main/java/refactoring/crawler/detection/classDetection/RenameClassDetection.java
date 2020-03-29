package refactoring.crawler.detection.classDetection;

import java.util.ArrayList;
import java.util.List;
import refactoring.crawler.graph.Edge;
import refactoring.crawler.graph.NamedDirectedMultigraph;
import refactoring.crawler.graph.Node;

public class RenameClassDetection extends ClassDetection {

  public RenameClassDetection(NamedDirectedMultigraph graph1, NamedDirectedMultigraph graph2) {
    super(graph1, graph2);
  }

  public double computeLikeliness(Node nodeOriginal, Node nodeVersion) {
    return doEdgeAnalysis(nodeOriginal, nodeVersion);
  }

  /**
   * Calls createCallGraph in ClassDetection Calls filterNamedEdges in ClassDetection Calls
   * computeLikelinessIncomingEdges in RefactoringDetection
   *
   * @param nodeOriginal
   * @param nodeVersion
   * @return
   */
  private double doEdgeAnalysis(Node nodeOriginal, Node nodeVersion) {
    double edgeGrade;
    createCallGraph(nodeOriginal, nodeVersion);
    List<Edge> incomingEdgesOriginal =
        filterNamedEdges(new ArrayList<>(graph1.incomingEdgesOf(nodeOriginal)));
    List<Edge> incomingEdgesVersion =
        filterNamedEdges(new ArrayList<>(graph2.incomingEdgesOf(nodeVersion)));
    edgeGrade = computeLikelinessIncomingEdges(incomingEdgesOriginal, incomingEdgesVersion);
    return edgeGrade;
  }

  public List<Node[]> pruneOriginalCandidates(List<Node[]> candidates) {
    List<Node[]> prePruned = super.pruneOriginalCandidatesImpl(candidates);
    List<Node[]> candidatesWithSameParentPackage = new ArrayList<>();
    for (Node[] pair : prePruned) {
      Node original = pair[0];
      Node version = pair[1];
      String parentPackageOriginal = extractParentSimpleName(original);
      String parentPackageVersion = extractParentSimpleName(version);
      if (isTheSameModuloRename(parentPackageOriginal, parentPackageVersion)
          && (!(original.getSimpleName().equals(version.getSimpleName())))) {
        candidatesWithSameParentPackage.add(pair);
      }
    }

    return candidatesWithSameParentPackage;
  }

  @Override
  public boolean isRename() {
    return true;
  }
}
