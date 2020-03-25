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

	public MethodDetection(NamedDirectedMultigraph graph, NamedDirectedMultigraph graph2) {
		super(graph, graph2);
	}

	@Override
	public double computeLikeliness(Node node1, Node node12) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List pruneOriginalCandidates(List candidates) {
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
		for (Edge edge : list) {
			if (Node.Type.METHOD_CALL.equals(edge.getLabel())) {
				results.add(edge);
			}
		}
		return results;
	}

	public void createCallGraph(Node node, NamedDirectedMultigraph graph) {
		List callers;
//		if (this instanceof ChangeMethodSignatureDetection)
//			callers = SearchHelper.findMethodCallers(node,
//				new NullProgressMonitor(), true);
//		else
//			callers = SearchHelper.findMethodCallers(node,
//				new NullProgressMonitor(), false);
//		for (Iterator iter = callers.iterator(); iter.hasNext(); ) {
//			IMember element = (IMember) iter.next();
//			String nodeName = element.getElementName();
//			String qualifiername = element.getDeclaringType()
//				.getFullyQualifiedName('.');
//			Node caller = graph.findNamedNode(qualifiername + "." + nodeName);
//			if (caller != null) {
//				Edge edge = factory.createEdge(caller, node, Node.METHOD_CALL);
//				graph.addEdge(edge);
//			}
//		}
//		node.setCreatedCallGraph();

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
		List<Edge> incomingEdgesOriginal = filterNamedEdges(new ArrayList<>(graph1
			.incomingEdgesOf(original)));
		List<Edge> incomingEdgesVersion = filterNamedEdges(new ArrayList<>(graph2
			.incomingEdgesOf(version)));
//		incomingEdgesGrade = computeLikelinessIncomingEdges(
//			incomingEdgesOriginal, incomingEdgesVersion);
		return 0;
	}

}
