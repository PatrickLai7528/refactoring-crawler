package refactoring.crawler.detection;

import lombok.val;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMember;
import refactoring.crawler.project.IMethod;
import refactoring.crawler.util.Edge;
import refactoring.crawler.util.NamedDirectedMultigraph;
import refactoring.crawler.util.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class MethodDetection extends RefactoringDetection {

    protected void createCallGraph(Node node, NamedDirectedMultigraph graph) {
        List callers = new ArrayList();
        if (this instanceof ChangeMethodSignatureDetection)
            callers = SearchHelper.findMethodCallers(node, true);
        else
            callers = SearchHelper.findMethodCallers(node, false);
        for (Object o : callers) {
//            IMethod element = (IMethod) o;
//            String nodeName = element.getElementName();
//            String qualifiername = element.getDeclaringType()
//                    .getFullyQualifiedName('.');
//            Node caller = graph.findNamedNode(qualifiername + "." + nodeName);
//            if (caller != null) {
//                Edge edge = factory.createEdge(caller, node, Node.METHOD_CALL);
//                graph.addEdge(edge);
//            }
        }
        node.setCreatedCallGraph();

    }

    public List<Edge> filterNamedEdges(Set<Edge> list) {
        val results = new ArrayList<Edge>();
        for (final Edge edge : list) {
            if (Node.Type.METHOD_CALL.equals(edge.getLabel())) {
                results.add(edge);
            }
        }
        return results;
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
        val incomingEdgesOriginal = filterNamedEdges(graph1
                .incomingEdgesOf(original));
        val incomingEdgesVersion = filterNamedEdges(graph2
                .incomingEdgesOf(version));
        incomingEdgesGrade = computeLikelinessIncomingEdges(
                incomingEdgesOriginal, incomingEdgesVersion);
        return incomingEdgesGrade;
    }
}
