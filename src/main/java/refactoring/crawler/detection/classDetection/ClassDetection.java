package refactoring.crawler.detection.classDetection;

import org.eclipse.jdt.core.IType;
import refactoring.crawler.detection.RefactoringDetection;
import refactoring.crawler.detection.SearchHelper;
import refactoring.crawler.util.ClassNode;
import refactoring.crawler.util.Edge;
import refactoring.crawler.util.NamedDirectedMultigraph;
import refactoring.crawler.util.Node;

import java.util.ArrayList;
import java.util.List;

public class ClassDetection extends RefactoringDetection {

	public ClassDetection(NamedDirectedMultigraph graph, NamedDirectedMultigraph graph2) {
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
			if (Node.Type.CLASS_REFERENCE.equals(value.getLabel())) {
				results.add(value);
			}
		}
		return results;
	}

	/**
	 * We need to find all the places that the original and version classes are
	 * instantiated. We will incorporate this into the likeliness grade.
	 */
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

	public void createCallGraph(Node node, NamedDirectedMultigraph graph) {
		createClassReferenceGraph(node, graph);
		node.setCreatedCallGraph();
	}

	/**
	 * Accepts two class nodes, and determines if first parameter is a
	 * superclass of the second parameter.
	 */
	public static boolean isSuperClassOf(Node node1, Node node2) {
		List<String> superClasses = SearchHelper.findSuperClassesOf((ClassNode) node2);
		for (String superClass : superClasses) {
			if (superClass.equals(node1.getFullyQualifiedName()))
				return true;
		}
		return false;
//		return false;
	}


}
