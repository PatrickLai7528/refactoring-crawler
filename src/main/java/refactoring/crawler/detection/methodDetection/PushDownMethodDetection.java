package refactoring.crawler.detection.methodDetection;

import java.util.ArrayList;
import java.util.List;
import refactoring.crawler.detection.classDetection.ClassDetection;
import refactoring.crawler.graph.NamedDirectedMultigraph;
import refactoring.crawler.graph.Node;

public class PushDownMethodDetection extends MethodDetection {

  public PushDownMethodDetection(NamedDirectedMultigraph graph, NamedDirectedMultigraph graph2) {
    super(graph, graph2);
  }

  public double computeLikeliness(Node original, Node version) {
    boolean superClassGrade = false;
    String parentClassOriginal = extractFullyQualifiedParentName(original);
    String parentClassVersion = extractFullyQualifiedParentName(version);
    parentClassOriginal = extractPotentialRename(parentClassOriginal);
    Node parentClassOrig = graph2.findNamedNode(parentClassOriginal);
    if (parentClassOrig == null) return 0.0;
    Node parentClassVer = graph2.findNamedNode(parentClassVersion);
    // Now we should check if parentClassVer is a subclass of
    // parentClassOrig
    if (parentClassOriginal.contains("Priority") || parentClassOriginal.contains("Level"))
      System.out.println("stop");
    if (ClassDetection.isSuperClassOf(parentClassOrig, parentClassVer)) superClassGrade = true;
    if (superClassGrade) {
      return (analyzeIncomingEdges(original, version));
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
