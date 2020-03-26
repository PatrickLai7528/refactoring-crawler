package refactoring.crawler.detection.fieldDetection;

import java.util.ArrayList;
import java.util.List;
import refactoring.crawler.graph.NamedDirectedMultigraph;
import refactoring.crawler.graph.Node;

public class MoveFieldDetection extends FieldDetection {

    /*
    * We already have Class - Field edges. So we need to make sure that they
    * are different parents, and also make sure that their call graph is still
    * the same.
    */

    public MoveFieldDetection(NamedDirectedMultigraph graph, NamedDirectedMultigraph graph2) {
        super(graph, graph2);
    }

    public double computeLikeliness(Node original, Node version) {
        return analyzeIncomingEdges(original, version);
    }

    public List<Node[]> pruneOriginalCandidates(List<Node[]> candidates) {
        List<Node[]> prePrunedFields = super.pruneOriginalCandidatesImpl(candidates);
        List<Node[]> candidatesWithDifferentParentClass = new ArrayList<>();
        for (Node[] pair : prePrunedFields) {
            Node original = pair[0];
            Node version = pair[1];
            String parentClassOriginal = extractFullyQualifiedParentName(original);
            String parentClassVersion = extractFullyQualifiedParentName(version);
            boolean isModRen = isTheSameModuloRename(parentClassOriginal, parentClassVersion);

            if (!isModRen && ((original.getSimpleName().equals(version.getSimpleName())))) {
                candidatesWithDifferentParentClass.add(pair);
            }
        }
        return candidatesWithDifferentParentClass;
    }
}
