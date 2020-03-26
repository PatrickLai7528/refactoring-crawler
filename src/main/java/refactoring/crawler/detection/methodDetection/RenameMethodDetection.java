package refactoring.crawler.detection.methodDetection;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import refactoring.crawler.graph.NamedDirectedMultigraph;
import refactoring.crawler.graph.Node;

public class RenameMethodDetection extends MethodDetection {

  public RenameMethodDetection(
      NamedDirectedMultigraph oldVersion, NamedDirectedMultigraph newVersion) {
    super(oldVersion, newVersion);
  }

  /**
   * @param candidates List containing clone methods
   * @return A List containing only the candidate methods that are in the same class
   */
  public List<Node[]> pruneOriginalCandidates(List<Node[]> candidates) {
    List<Node[]> prePrunedMethods = super.pruneOriginalCandidatesImpl(candidates);
    List<Node[]> candidatesWithSameParentClass = new ArrayList();
    for (Node[] pair : prePrunedMethods) {
      Node original = pair[0];
      Node version = pair[1];
      String parentClassOriginal = extractFullyQualifiedParentName(original);
      String parentClassVersion = extractFullyQualifiedParentName(version);
      if (isTheSameModuloRename(parentClassOriginal, parentClassVersion)
          && (!(original.getSimpleName().equals(version.getSimpleName()))))
        candidatesWithSameParentClass.add(pair);
    }

    return candidatesWithSameParentClass;
  }

  public double computeLikeliness(Node original, Node version) {
    // createCallGraph(original, version);
    // return computeLikelinessConsideringEdges(original, version);
    return analyzeIncomingEdges(original, version);
  }

  @Override
  public boolean isRename() {
    return true;
  }

  /**
   * Prune further for cases that have n-to-1 mappings. (eg. {start, end, pointAt} ->
   * getStartConnector) in JHD5.3 )
   */
  public List<Node[]> pruneFalsePositives(List<Node[]> listWithFP) {
    List<Node[]> prunedList = super.pruneFalsePositives(listWithFP);
    for (int i = 0; i < prunedList.size(); i++) {
      Node[] pair = prunedList.get(i);
      Node target = pair[1];
      String targetName = target.getSimpleName().toLowerCase().trim();
      List<Node[]> allPairsWithSameTarget = new ArrayList<Node[]>();
      for (Node[] nodes : prunedList) {
        Node potentialTarget = (nodes)[1];
        if (target == potentialTarget) allPairsWithSameTarget.add(nodes);
      }
      if (allPairsWithSameTarget.size() > 1) {
        for (Object o : allPairsWithSameTarget) {
          Node[] sameTargetPair = (Node[]) o;
          Node sourceNode = sameTargetPair[0];
          String sourceName = sourceNode.getSimpleName().toLowerCase().trim();
          // Changed from || to && and changed the !='s to =='s
          if ((!targetName.contains(sourceName)) && (!sourceName.contains(targetName))) {
            prunedList.remove(sameTargetPair);
            Dictionary<String, String> dictionary = getRenamingDictionary();
            dictionary.remove(sourceNode.getFullyQualifiedName());
          }
        }
      }
    }
    return prunedList;
  }
}
