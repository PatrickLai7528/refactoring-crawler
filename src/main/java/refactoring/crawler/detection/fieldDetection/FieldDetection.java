package refactoring.crawler.detection.fieldDetection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import refactoring.crawler.RefactoringCrawler;
import refactoring.crawler.detection.RefactoringDetection;
import refactoring.crawler.detection.SearchHelper;
import refactoring.crawler.util.Edge;
import refactoring.crawler.util.FieldNode;
import refactoring.crawler.util.NamedDirectedMultigraph;
import refactoring.crawler.util.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FieldDetection extends RefactoringDetection {

	public FieldDetection(NamedDirectedMultigraph graph, NamedDirectedMultigraph graph2) {
		super(graph, graph2);
	}

	@Override
	public double computeLikeliness(Node node1, Node node12) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see edu.uiuc.detectRefactorings.detection.RefactoringDetection#pruneOriginalCandidates(java.util.List)
	 */
	@Override
	public List<Node[]> pruneOriginalCandidates(List<Node[]> candidates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createCallGraph(Node originalInV2, NamedDirectedMultigraph graph2) {

	}

	@Override
	public List<Edge> filterNamedEdges(List<Edge> list) {
		List<Edge> results = new ArrayList<>();
		for (Edge value : list) {
			if (Node.Type.FIELD_REFERENCE.equals(value.getLabel())) {
				results.add(value);
			}
		}
		return results;
	}

	@Override
	public boolean isRename() {
		return false;
	}

	protected void createFieldReferenceGraph(Node originalNode, NamedDirectedMultigraph graph) {
		final List<String> results = SearchHelper.findFieldReferences((FieldNode) originalNode);

		for (String result : results) {
//				IMember resultNode = (IMember) result;
//				String callingNode = null;
//				if (resultNode instanceof IMethod) {
//					IMethod rsm1 = (IMethod) resultNode;
//					callingNode = rsm1.getDeclaringType()
//						.getFullyQualifiedName('.');
//					callingNode += "." + rsm1.getElementName();
//				} else if (resultNode instanceof Initializer) {
//					Initializer initializer = (Initializer) resultNode;
//					VariableDeclarationFragment fieldDeclarationFragment = (VariableDeclarationFragment) ASTNodes
//						.getParent(initializer,
//							VariableDeclarationFragment.class);
//					SimpleName simpleName = fieldDeclarationFragment.getName();
//					callingNode = resultNode.getDeclaringType()
//						.getFullyQualifiedName('.');
//					callingNode += "." + simpleName.getFullyQualifiedName();
//				}

			Node callerNode = graph.findNamedNode(result);
			if (callerNode != null)
				graph.addEdge(callerNode, originalNode,
					new Edge(Node.Type.FIELD_REFERENCE));
		}

	}

	public void createCallGraph(Node original, Node version) {
		if (!original.hasCallGraph()) {
			createFieldReferenceGraph(original, graph1);
			original.setCreatedCallGraph();
		}
		if (!version.hasCallGraph()) {
			createFieldReferenceGraph(version, graph2);
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
		incomingEdgesGrade = computeLikelinessIncomingEdges(
			incomingEdgesOriginal, incomingEdgesVersion);
		return incomingEdgesGrade;
	}
}
