package refactoring.crawler.detection;

import refactoring.crawler.util.Edge;
import refactoring.crawler.util.NamedDirectedMultigraph;
import refactoring.crawler.util.Node;

import java.util.List;
import java.util.Set;

public class MoveMethodDetection extends RefactoringDetection {
    @Override
    public List<Node[]> pruneOriginalCandidates(List<Node[]> candidates) {
        return null;
    }

    @Override
    public double computeLikeliness(Node node1, Node node12) {
        return 0;
    }

    @Override
    public List<Edge> filterNamedEdges(Set<Edge> list) {
        return null;
    }

    @Override
    protected void createCallGraph(Node originalInV2, NamedDirectedMultigraph graph2) {

    }
}
