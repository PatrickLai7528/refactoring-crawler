package refactoring.crawler.detection.methodDetection;

import java.util.ArrayList;
import java.util.List;
import refactoring.crawler.detection.classDetection.ClassDetection;
import refactoring.crawler.graph.NamedDirectedMultigraph;
import refactoring.crawler.graph.Node;

public class PullUpMethodDetection extends MethodDetection {

  public PullUpMethodDetection(NamedDirectedMultigraph graph, NamedDirectedMultigraph graph2) {
    super(graph, graph2);
  }

  /**
   * We should now check for the same method being in the parent class, thus for the two nodes,
   * check if the version now resides in the superclass of the original method's parent class.
   */
  // TODO: Check why we get a null pointer exception with parentclassver and
  // parent class orig)
  public double computeLikeliness(Node original, Node version) {
    double incomingEdgesGrade = 0.0;
    boolean isSuperclass = false;
    // TODO: Think about possible different cases that this might be
    // an error. pack2.Class1.main vs. pack2.Runner.main, it cannot
    // find it.
    // TODO: Think about the NULL case. Return 0.0 if you find null,
    // since clearly they are not "like" each other.
    String parentClassOriginal = extractFullyQualifiedParentName(original);
    parentClassOriginal = extractPotentialRename(parentClassOriginal);
    String parentClassVersion = extractFullyQualifiedParentName(version);
    Node parentClassOrig = graph2.findNamedNode(parentClassOriginal);
    if (parentClassOrig == null) return 0.0;
    Node parentClassVer = graph2.findNamedNode(parentClassVersion);
    // Now we should check if parentClassOrig is a subclass of
    // parentClassVer
    if (ClassDetection.isSuperClassOf(parentClassVer, parentClassOrig)) isSuperclass = true;

    if (isSuperclass) {
      incomingEdgesGrade = analyzeIncomingEdges(original, version);
      return incomingEdgesGrade;
    } else return 0.0;
  }

  public List<Node[]> pruneOriginalCandidates(List<Node[]> candidates) {
    List<Node[]> prePrunedMethods = super.pruneOriginalCandidatesImpl(candidates);
    List<Node[]> candidatesWithDifferentParentClass = new ArrayList<>();
    for (Node[] pair : prePrunedMethods) {
      Node original = pair[0];
      Node version = pair[1];
      String parentClassOriginal = extractParentSimpleName(original);
      String parentClassVersion = extractParentSimpleName(version);
      if (!isTheSameModuloRename(parentClassOriginal, parentClassVersion)
          && ((original.getSimpleName().equals(version.getSimpleName())))) {
        candidatesWithDifferentParentClass.add(pair);
      }
    }

    return candidatesWithDifferentParentClass;
  }

  @Override
  public boolean isRename() {
    return false;
  }
}
