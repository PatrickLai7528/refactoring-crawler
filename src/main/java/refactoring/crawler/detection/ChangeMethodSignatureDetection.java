package refactoring.crawler.detection;

import refactoring.crawler.util.Edge;
import refactoring.crawler.util.NamedDirectedMultigraph;
import refactoring.crawler.util.Node;

import java.util.*;

public class ChangeMethodSignatureDetection extends MethodDetection {

	/**
	 * @param graph
	 * @param graph2
	 */
	public ChangeMethodSignatureDetection(NamedDirectedMultigraph graph, NamedDirectedMultigraph graph2) {
		super(graph, graph2);
	}

	/**
	 * We need to go from the node to the AST and get the actual method. Then we
	 * will call getSignature() on the IMethod to get the signature. We have to
	 * make sure the call graphs are checked, since we do not want to detect
	 * polymorphism as change method signature.
	 */
	public double computeLikeliness(Node original, Node version) {
		// Need to find out if in V2 there is a node with the same signature
		// as the original
		if (isDeprecatedOrRemoved(new Node[]{original, version}))
			return 1.0;
		else {
			// This is when we have a method overload or deprecated. So when
			// we can check deprecated methods we need to add it here
			return analyzeIncomingEdges(original, version);
		}
	}

	/**
	 * This will handle the same name condition, explained above.
	 */
	public List<Node[]> pruneOriginalCandidates(List<Node[]> candidates) {
		List<Node[]> prunedCandidates = new ArrayList<>();
		for (Node[] pair : candidates) {
			if (pair[0].getSignature().equals(pair[1].getSignature()))
				continue;

			if (!(pair[0].isAPI() && pair[1].isAPI()))
				continue;

			boolean hasSameNameAndSignature = hasTheSameSignatureAndName(pair);

			if (hasSameNameAndSignature)
				continue;

			if (isTheSameModuloRename(pair[0].getFullyQualifiedName(), pair[1]
				.getFullyQualifiedName()))
				prunedCandidates.add(pair);
		}
		return prunedCandidates;
	}

	private boolean hasTheSameSignatureAndName(Node[] pair) {
		// TODO here we have to take into account the RenamigsDictionary
		String parentClassOfVersion = extractFullyQualifiedParentName(pair[1]);
		Node n2ParentInV1 = graph1.findNamedNode(parentClassOfVersion);

		boolean hasSameNameAndSignature = false;

		if (n2ParentInV1 != null) {
			// Calling the overloaded method
			List<Edge> allMethodEdges = filterNamedEdges(new ArrayList<>(graph1.outgoingEdgesOf(n2ParentInV1)), Node.Type.METHOD);
			for (Edge methodEdge : allMethodEdges) {
				Node targetMethod = methodEdge.getTarget();
				if (targetMethod.getSimpleName()
					.equals(pair[1].getSimpleName())
					&& targetMethod.getSignature().equals(
					pair[1].getSignature()))
					hasSameNameAndSignature = true;
			}
		}
		return hasSameNameAndSignature;
	}

	private boolean isDeprecatedOrRemoved(Node[] pair) {
		Node source = pair[0];
		String parentOfOriginal = extractFullyQualifiedParentName(source);
		parentOfOriginal = extractPotentialRename(parentOfOriginal);
		Node parentOfOriginalInV2 = graph2.findNamedNode(parentOfOriginal);
		boolean isDeprecated = false;
		boolean isRemoved = true;
		if (parentOfOriginalInV2 != null) {
			List<Edge> methodEdges = filterNamedEdges(new LinkedList<>(graph2.outgoingEdgesOf(parentOfOriginalInV2)), Node.Type.METHOD);
			for (Edge edge : methodEdges) {
				Node methodNode = edge.getTarget();
				if (methodNode.getSimpleName().equals(source.getSimpleName())
					&& methodNode.getSignature().equals(
					source.getSignature())) {
					isRemoved = false;
					isDeprecated = methodNode.isDeprecated();
				}

			}
		}

		return isDeprecated || isRemoved;
	}

	@Override
	public List<Node[]> pruneFalsePositives(List<Node[]> listWithFP) {
		//  List prunedInParent= super.pruneFalsePositives(listWithFP);
		List<Node[]> goodResults = new ArrayList<Node[]>();

		for (Node[] pair : listWithFP) {
			String signatureN1 = pair[0].getSignature();
			String signatureN2 = pair[1].getSignature();
			if (!isTheSameSignature(signatureN1, signatureN2))
				goodResults.add(pair);
		}
		return goodResults;
	}

	private boolean isTheSameSignature(String signatureN1, String signatureN2) {
		// TODO filters out (IPluginDescriptor) with
		// (org.eclipse.core.runtime.IPluginDescriptor)
		// right now this is checked only for case when there is a one argument
		String simpleName1 = extractSimpleName(signatureN1.substring(1,
			signatureN1.length() - 1));
		String simpleName2 = extractSimpleName(signatureN2.substring(1,
			signatureN2.length() - 1));
		return simpleName1.equals(simpleName2);
	}

	private String extractSimpleName(String fqn) {
		int lastIndex = fqn.lastIndexOf(".");
		if (lastIndex < 0)
			return fqn;
		else
			return fqn.substring(lastIndex + 1);
	}

	public boolean isRename() {
		return false;
	}

}
