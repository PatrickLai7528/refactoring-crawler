package refactoring.crawler.detection;

import java.util.*;

import lombok.Getter;
import lombok.Setter;
import refactoring.crawler.detection.methodDetection.MoveMethodDetection;
import refactoring.crawler.graph.ClassNode;
import refactoring.crawler.graph.Edge;
import refactoring.crawler.graph.NamedDirectedMultigraph;
import refactoring.crawler.graph.Node;

public abstract class RefactoringDetection {

	@Getter
	@Setter
	private double threshold;

	protected NamedDirectedMultigraph graph1;

	protected NamedDirectedMultigraph graph2;

	private double lowerThreshold;

	/**
	 * Dictionary contains <Original, Version> pairs for the renaming.
	 */
	private static Dictionary<String, String> renamingDictionary;

	public static Dictionary<String, String> getRenamingDictionary() {
		if (renamingDictionary == null) renamingDictionary = new Hashtable<>();
		return renamingDictionary;
	}

	public RefactoringDetection(NamedDirectedMultigraph graph, NamedDirectedMultigraph graph2) {
		this.graph1 = graph;
		this.graph2 = graph2;
	}

	public abstract double computeLikeliness(Node node1, Node node12);

	public abstract List<Edge> filterNamedEdges(List<Edge> list);

	public abstract List<Node[]> pruneOriginalCandidates(List<Node[]> candidates);

	public abstract void createCallGraph(Node originalInV2, NamedDirectedMultigraph graph2);

	public abstract boolean isRename();

	/**
	 * TEMPLATE METHOD Describes the algorithm for detecting any particular refactoring The original
	 * candidates are prunned (for getting rid of obvious extraneous ones, then the likeliness of each
	 * pair is computed. In the end we eliminate FalsePositives. Subclasses must override
	 * computeLikeliness and pruneOriginalCandidates.
	 */
	public List<Node[]> detectRefactorings(List<Node[]> candidates) {

		List<Node[]> refactoredNodes = new ArrayList<Node[]>();
		List<Node[]> listWithFP = doDetectRefactorings(candidates, refactoredNodes);
		return pruneFalsePositives(listWithFP);
	}

	protected String extractPotentialRename(String parentClassOriginal) {
		String renamedName = getRenamingDictionary().get(parentClassOriginal);
		return renamedName == null ? parentClassOriginal : renamedName;
	}

	private List<Node[]> doDetectRefactorings(List<Node[]> candidates, List<Node[]> refactoredNodes) {
		// List<Node[]> potentialRefactorings = new ArrayList<Node[]>();
		List<Node[]> prunedCandidates = pruneOriginalCandidates(candidates);
		boolean foundNewRefactoring = false;
		for (Node[] pair : prunedCandidates) {
			Node original = pair[0];
			Node version = pair[1];
			double likeliness = computeLikeliness(original, version);
			if (likeliness >= threshold) {
				if (!refactoredNodes.contains(pair)) {
					refactoredNodes.add(pair);
					foundNewRefactoring = true;
				}
				// candidates.remove(pair); acivating this line would fail to
				// detect those cases when two
				// types of refactorings happened to the same node
			}
		}
		if (foundNewRefactoring) {
			doDetectRefactorings(candidates, refactoredNodes);
		}
		return refactoredNodes;
	}

	public List<Node[]> pruneFalsePositives(List<Node[]> listWithFP) {
		List<Node[]> nodesToRemove = new ArrayList<Node[]>();
		for (Node[] pair : listWithFP) {
			Node original = pair[0];
			Node version = pair[1];
			Node originalInV2 = findNamedNodeWithSignature(graph2, original);
			if (originalInV2 != null) {
				createCallGraph(originalInV2, graph2);
				List<Edge> origIncomingEdges =
					filterNamedEdges(new ArrayList<>(graph2.incomingEdgesOf(originalInV2)));
				List<Edge> verIncomingEdges =
					filterNamedEdges(new ArrayList<>(graph2.incomingEdgesOf(version)));
				List<Edge> origInVer1IncomingEdges =
					filterNamedEdges(new ArrayList<>(graph1.incomingEdgesOf(original)));

				List<Node> origInV2Callers = getCallers(origIncomingEdges);
				List<Node> verCallers = getCallers(verIncomingEdges);
				List<Node> origInV1Callers = getCallers(origInVer1IncomingEdges);

				// remove those pairs where N1InV2 has at least one call site as N2inV2.
				// since a call site cannot be calling both the old and the new entity at the same time
				for (Node node : verCallers) {
					if (origInV2Callers.contains(node))
						if (!nodesToRemove.contains(pair)) {
							System.out.println("1st Prune in RD:" + pair[0] + pair[1]);
							nodesToRemove.add(pair);
						}
				}

				// check to see whether the N1inV1 has at least one call site as N1inV2. If it has, then the
				// pair
				// is a false positive (since there should be either no more callers for N1inV2 or their
				// call sites
				// should be different
				for (Node node : origInV1Callers) {
					for (Node callingNode : origInV2Callers) {
						if (node.getFullyQualifiedName().equals(callingNode.getFullyQualifiedName())) {
							if (!nodesToRemove.contains(pair)) {
								nodesToRemove.add(pair);
							}
							break;
						}
					}
				}
			}
		}
		for (Node[] pair : nodesToRemove) {
			listWithFP.remove(pair);
			Dictionary<String, String> dictionary = getRenamingDictionary();
			dictionary.remove(pair[0].getFullyQualifiedName());
		}

		pruneOverloadedMethodFP(listWithFP);

		return listWithFP;
	}

	private List<Node> getCallers(List<Edge> incomingEdges) {
		List<Node> callers = new ArrayList<>();
		for (Edge edge : incomingEdges) {
			callers.add(edge.getSource());
		}
		return callers;
	}

	/**
	 * This prunes cases like m(i) -> m'(i) m(i) -> m'(S)
	 *
	 * <p>This method prunes away the pair m(i)->m'(S) since it is likely that this is generated
	 * because of a method overload.
	 *
	 * <p>This method is never called in the ChangeMethodSignature detection.
	 */
	private void pruneOverloadedMethodFP(List<Node[]> listWithFP) {
		List<Node[]> nodesToRemove = new ArrayList<>();
		for (int i = 0; i < listWithFP.size(); i++) {
			boolean hasSameNameAndSignature = false;
			Node[] pair = listWithFP.get(i);
			Node source = pair[0];
			for (int j = i; j < listWithFP.size(); j++) {
				Node[] pair2 = listWithFP.get(j);
				Node source2 = pair2[0];
				if (source.equals(source2)) {
					Node target2 = pair2[1];
					if (source.getSimpleName().equals(target2.getSimpleName()))
						if (signatureEqualsModuloMoveMethod(source, target2)) hasSameNameAndSignature = true;
				}
			}
			if (hasSameNameAndSignature) {
				for (int j = i; j < listWithFP.size(); j++) {
					Node[] pair2 = listWithFP.get(j);
					Node source2 = pair2[0];
					if (source.equals(source2)) {
						Node target2 = pair2[1];
						if (source.getSimpleName().equals(target2.getSimpleName()))
							if (!signatureEqualsModuloMoveMethod(source, target2))
								if (!nodesToRemove.contains(pair2)) nodesToRemove.add(pair2);
					}
				}
			}
		}
		for (Node[] pair : nodesToRemove) {
			System.out.println("2nd REMOVE: " + pair[0] + ", " + pair[1]);
			listWithFP.remove(pair);

			Dictionary<String, String> dictionary = getRenamingDictionary();
			dictionary.remove(pair[0].getFullyQualifiedName());
		}
	}

	/**
	 * This takes into account the possible renaming in the parent of the node
	 *
	 * @param g        is the Version2 graph
	 * @param original is a node from Version1
	 * @return
	 */
	private Node findNamedNodeWithSignature(NamedDirectedMultigraph g, Node original) {
		Dictionary<String, String> dictionary = getRenamingDictionary();
		String fqnParent = extractFullyQualifiedParentName(original);
		String possiblyRenamedFQN = dictionary.get(fqnParent);
		if (possiblyRenamedFQN != null) fqnParent = possiblyRenamedFQN;
		Node parentNode = g.findNamedNode(fqnParent);

		if (parentNode != null) {
			List<Edge> parentEdges = new LinkedList<>(g.outgoingEdgesOf(parentNode));
			List<Edge> filteredEdges = new LinkedList<>(filterNamedEdges(parentEdges, Node.Type.METHOD));
			for (Iterator iter = filteredEdges.iterator(); iter.hasNext(); ) {
				Edge edge = (Edge) iter.next();
				Node child = (Node) edge.getTarget();
				if (original.getSimpleName().equals(child.getSimpleName()))
					if (original.getSignature() != null) {
						// This handles the method nodes
						if (original.getSignature().equals(child.getSignature())) return child;
					} else
						// Classes and packages
						return child;
			}
		}
		return null;
	}

	protected List<Edge> filterNamedEdges(List<Edge> list, Node.Type label) {
		List<Edge> results = new ArrayList<>();
		for (Edge edge : list) {
			if (label.equals(edge.getLabel())) {
				results.add(edge);
			}
		}
		return results;
	}

	protected String extractFullyQualifiedParentName(Node original) {
		String originalName = original.getFullyQualifiedName();
		return extractFullyQualifiedParentName(originalName);
	}

	public String extractFullyQualifiedParentName(String originalName) {
		String fq_parentName = "";
		int lastIndex = originalName.lastIndexOf(".");
		if (lastIndex > 0) fq_parentName = originalName.substring(0, lastIndex);
		return fq_parentName;
	}

	/**
	 * eg. IWorkbenchPage.openEditor(IFile) is signatureEqualsModuloMoveMethod
	 * IDE.openEditor(IWorkbenchPage, IFile)
	 */
	private boolean signatureEqualsModuloMoveMethod(Node source, Node target) {
		boolean retval = false;

		if (source.getSignature() == null) return false;

		retval = source.getSignature().equals(target.getSignature());

		if (!retval && (this instanceof MoveMethodDetection)) {
			String sourceParent = extractParentSimpleName(source);
			StringTokenizer sourceTokenizer = new StringTokenizer(source.getSignature(), "( , )");
			StringTokenizer targetTokenizer = new StringTokenizer(target.getSignature(), "( , )");
			String[] sourceTokens = new String[sourceTokenizer.countTokens()];
			String[] targetTokens = new String[targetTokenizer.countTokens()];

			for (int i = 0; i < sourceTokens.length; i++) {
				sourceTokens[i] = sourceTokenizer.nextToken();
			}

			for (int i = 0; i < targetTokens.length; i++) {
				targetTokens[i] = targetTokenizer.nextToken();
			}

			if (targetTokens.length == sourceTokens.length + 1) {
				if (!targetTokens[0].trim().equals(sourceParent)) return false;
				else {
					for (int i = 0; i < sourceTokens.length; i++) {
						if (!sourceTokens[i].trim().equals(targetTokens[i + 1].trim())) return false;
					}
					retval = true;
				}
			}
		}
		return retval;
	}

	/**
	 * This helper method takes a string containing the dot separated name of a node and it returns
	 * the substring from the beginning up to the last dot (e.g. for pack1.class1.method1 it returns
	 * class1)
	 */
	protected String extractParentSimpleName(Node original) {
		String originalName = original.getFullyQualifiedName();
		String parentName = originalName.substring(0, originalName.lastIndexOf("."));
		parentName = parentName.substring(parentName.lastIndexOf(".") + 1);
		return parentName;
	}

	public double computeLikelinessIncomingEdges(List<Edge> edges1, List<Edge> edges2) {
		double count = 0;

		Edge[] arrEdge2 = edges2.toArray(new Edge[0]);

		for (Edge edge1 : edges1) {
			Node node1 = edge1.getSource();
			for (int i = 0; i < arrEdge2.length; i++) {
				Edge edge2 = arrEdge2[i];
				if (edge2 != null) {
					Node node2 = (Node) edge2.getSource();
					if (isTheSameModuloRename(node1.getFullyQualifiedName(), node2.getFullyQualifiedName())) {
						count++;
						// we mark this edge as already counted so that we don't
						// count it
						// twice when there are multiple edges between two nodes
						arrEdge2[i] = null;
					}
				}
			}
		}

		double fraction1 = (edges1.size() == 0 ? 0 : count / edges1.size());
		double fraction2 = edges2.size() == 0 ? 0 : count / edges2.size();

		return (fraction1 + fraction2) / 2.0;
	}

	protected boolean isTheSameModuloRename(String original, String version) {
		Dictionary<String, String> dictionary = getRenamingDictionary();
		if (version.equals(dictionary.get(original))) return true;
		if (original.lastIndexOf(".") == -1 || version.lastIndexOf(".") == -1)
			return original.equals(version);
		else if (original
			.substring(original.lastIndexOf("."))
			.equals(version.substring(version.lastIndexOf("."))))
			return isTheSameModuloRename(
				extractFullyQualifiedParentName(original), extractFullyQualifiedParentName(version));
		else return false;
	}

	public List<Node[]> pruneOriginalCandidatesImpl(List<Node[]> candidates) {
		List<Node[]> prunedCandidates = new ArrayList<>();
		for (Node[] pair : candidates) {
			if (!(pair[0].getFullyQualifiedName().equals(pair[1].getFullyQualifiedName()))) {

				if (pair[0].isAPI() && pair[1].isAPI()) {
					Node n2inV1 = graph1.findNamedNode(pair[1].getFullyQualifiedName());

					if ((n2inV1 == null)) {
						prunedCandidates.add(pair);
					}
				}
			}
		}
		return prunedCandidates;
	}

	// TODO this is bug pruned
	protected void createClassReferenceGraph(Node originalNode, NamedDirectedMultigraph graph) {
		//		try {
		List<String> results = SearchHelper.findClassReferences(graph, (ClassNode) originalNode);
		results.forEach(
			result -> {
				if (graph.hasNamedNode(result))
					graph.addEdge(
						graph.findNamedNode(result), originalNode, new Edge(Node.Type.CLASS_REFERENCE));
			});
		// Possible change to methods that instantiate classes
		// from class -> class edges.
		//			for (String result : results) {
		////				IJavaElement resultNode = (IJavaElement) result;
		//				Node resultNode = graph.findNamedNode(result);
		//				String callingNode = null;
		//				if (resultNode instanceof IMethod) {
		//					IMethod rsm1 = (IMethod) resultNode;
		//					callingNode = rsm1.getDeclaringType()
		//						.getFullyQualifiedName('.');
		//					callingNode += "." + rsm1.getElementName();
		//				} else if (resultNode instanceof IType) {
		//					IType rst = (IType) resultNode;
		//					callingNode = rst.getFullyQualifiedName('.');
		//				} else if (resultNode instanceof IField) {
		//					IField rsf1 = (IField) resultNode;
		//					// Workaround
		//					callingNode = rsf1.getDeclaringType()
		//						.getFullyQualifiedName('.');
		//					callingNode += ".";
		//					callingNode += rsf1.getElementName();
		//				} else if (resultNode instanceof Initializer) {
		//					Initializer initializer = (Initializer) resultNode;
		//					VariableDeclarationFragment fieldDeclarationFragment = (VariableDeclarationFragment)
		// ASTNodes
		//						.getParent(initializer,
		//							VariableDeclarationFragment.class);
		//					SimpleName simpleName = fieldDeclarationFragment.getName();
		//					IType parentType = (IType) ASTNodes.getParent(initializer,
		//						IType.class);
		//					callingNode = parentType.getFullyQualifiedName('.');
		//					callingNode += "." + simpleName.getFullyQualifiedName();
		//				}
		//
		//				// TODO treat the case when resultNode is instance of
		//				// ImportDeclaration
		//				// TODO treat the case when resultNode is instance of
		//				// Initializer
		//				// this appears in Loj4j1.3.0 in class LogManager, references to
		//				// Level
		//				if (callingNode == null) {
		//					System.out.print("");
		//				}
		//				if (callingNode != null) {
		//					Node callerNode = graph.findNamedNode(callingNode);
		//					if (callerNode != null)
		//						graph.addEdge(callerNode, originalNode,
		//							Node.CLASS_REFERENCE);
		//				}
		//
		//			}
		//
		//		} catch (CoreException e) {
		//			JavaPlugin.log(e);
		//		}
	}
}
