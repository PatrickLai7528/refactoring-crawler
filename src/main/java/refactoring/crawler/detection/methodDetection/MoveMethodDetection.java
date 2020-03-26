package refactoring.crawler.detection.methodDetection;

import refactoring.crawler.RefactoringCrawler;
import refactoring.crawler.detection.classDetection.ClassDetection;
import refactoring.crawler.util.Edge;
import refactoring.crawler.util.NamedDirectedMultigraph;
import refactoring.crawler.util.Node;
import refactoring.crawler.util.RefactoringCategory;

import java.util.*;

public class MoveMethodDetection extends MethodDetection {

	private Node targetClassInVerGraph;

	private Node targetClassInOrigGraph;

	private RefactoringCrawler crawler;

	/**
	 * Checks for MoveMethod
	 * <p>
	 * 1. Check that from the old method, all the references to objects having
	 * the same type as the destination class were removed
	 * <p>
	 * 2. Check that the new target class is either a previous argument or a
	 * field in the old class
	 */
	public MoveMethodDetection(RefactoringCrawler crawler, NamedDirectedMultigraph graph1, NamedDirectedMultigraph graph2) {
		super(graph1, graph2);
		this.crawler = crawler;
	}

	public double computeLikeliness(Node original, Node version) {
		double edgeGrade = 0.0;

		double referenceGrade = 0.0;

		if (isTargetARenameOfSourceClass(original, version))
			return 0.0;

		// FIXME: Potential problem when we subtract 0.01 from reference grade
		referenceGrade = referencesRemoved(original, version);
		edgeGrade = analyzeIncomingEdges(original, version);
		return (edgeGrade + (referenceGrade - 0.01)) / 2.0;
	}

	private boolean isTargetARenameOfSourceClass(Node original, Node version) {
		String sourceInOriginal = extractFullyQualifiedParentName(original);
		String targetInVersion = extractFullyQualifiedParentName(version);
		// treat case 1
		return (isTheSameModuloRename(sourceInOriginal, targetInVersion));
	}

	/**
	 * 1. Check that from the old method, all the references to objects having
	 * the same type as the destination class were removed
	 */
	private double referencesRemoved(Node original, Node version) {
		String targetInVersion = extractFullyQualifiedParentName(version);

		targetClassInVerGraph = graph2.findNamedNode(targetInVersion);
		targetClassInOrigGraph = graph1.findNamedNode(targetInVersion);
		// treat case 2
		if (targetClassInOrigGraph == null) {
			Dictionary<String, String> dictionary = getRenamingDictionary();
			Enumeration<String> keys = dictionary.keys();
			for (; keys.hasMoreElements(); ) {
				String aKey = keys.nextElement();
				String aValue = dictionary.get(aKey);
				if (targetInVersion.equals(aValue)) {
					targetClassInOrigGraph = graph1.findNamedNode(aKey);
				}
			}

			// treat case 3
			if (targetClassInOrigGraph == null)
				return 1.0;

		}

		// treat case 2 and 4
		if (!targetClassInVerGraph.hasCallGraph()) {
			createClassReferenceGraph(targetClassInVerGraph, graph2);
			targetClassInVerGraph.setCreatedCallGraph();
		}
		if (!targetClassInOrigGraph.hasCallGraph()) {
			createClassReferenceGraph(targetClassInOrigGraph, graph1);
			targetClassInOrigGraph.setCreatedCallGraph();
		}

		List<Edge> originalClassReferences = new ArrayList<>(graph1.getAllEdges(original, targetClassInOrigGraph));
		List<Edge> versionClassReferences = new ArrayList<>(graph2.getAllEdges(version,
			targetClassInVerGraph));
		if (originalClassReferences.size() == 0) {
			if (original.isStatic())
				return 1.0;
			if (isTargetClassAFieldInSourceClass(original,
				targetClassInOrigGraph))
				return 1.0;
			if (versionClassReferences.size() == 0)
				return 1.0;
			return 0.0;
		} else
			return Math
				.abs(((originalClassReferences.size() - versionClassReferences
					.size()) / originalClassReferences.size()));
	}

	private boolean isTargetClassAFieldInSourceClass(Node original, Node theTargetClassInOrigGraph) {
		Node parentClass = graph1
			.findNamedNode(extractFullyQualifiedParentName(original));
		List<Edge> edges = new ArrayList<>(graph1.outgoingEdgesOf(parentClass));
		List<Node> fields = new ArrayList<Node>();
		for (Edge value : edges) {
			if (Node.Type.FIELD_REFERENCE.equals(value.getLabel())) {
				fields.add((Node) value.getTarget());
			}
		}

		return fields.contains(theTargetClassInOrigGraph);
	}

	public List<Node[]> pruneOriginalCandidates(List<Node[]> candidates) {
		List<Node[]> prePrunedMethods = super.pruneOriginalCandidatesImpl(candidates);
		List<Node[]> candidatesWithDifferentParentClass = new ArrayList<>();
		for (Node[] pair : prePrunedMethods) {
			Node original = pair[0];
			Node version = pair[1];

			// Prune toString since the SearchEngine finds all the toString()
			// methods, even those that are called from different classes
			if ("toString".equals(original.getSimpleName()))
				continue;

			String parentClassOriginal = extractFullyQualifiedParentName(original);
			String parentClassVersion = extractFullyQualifiedParentName(version);
			if (!isTheSameModuloRename(parentClassOriginal, parentClassVersion)
				&& ((original.getSimpleName().equals(version
				.getSimpleName())))) {
				candidatesWithDifferentParentClass.add(pair);
			}
		}
		return candidatesWithDifferentParentClass;
	}

	@Override
	public boolean isRename() {
		return false;
	}

	/**
	 * Overriden here to prune false positives due to overlapping PullUp and
	 * PushDown detection
	 */
	public List<Node[]> pruneFalsePositives(List<Node[]> listWithFP) {
		List<Node[]> withoutFP = removePairsDetectedInPUM_PDM(listWithFP);
		withoutFP = addPairsFromMMtoPUM_PDM(withoutFP);
		return withoutFP;
	}

	private List<Node[]> addPairsFromMMtoPUM_PDM(List<Node[]> withoutFP) {
		boolean needsOneMorePass = false;
		List<Node[]> addToPUM = new ArrayList<>();
		List<Node[]> addToPDM = new ArrayList<>();
		for (Node[] pair : withoutFP) {
			String parentClassOfM1 = extractFullyQualifiedParentName(pair[0]);
			String parentClassOfM2 = extractFullyQualifiedParentName(pair[1]);
			Node sourceClass = graph2.findNamedNode(parentClassOfM1);
			Node destinationClass = graph2.findNamedNode(parentClassOfM2);
			if (sourceClass != null && destinationClass != null) {
				if (ClassDetection
					.isSuperClassOf(sourceClass, destinationClass))
					addToPDM.add(pair);
				else if (ClassDetection.isSuperClassOf(destinationClass,
					sourceClass))
					addToPUM.add(pair);
			}
		}

		List<RefactoringCategory> refactoringsList = this.crawler.getRefactoringCategories();

		RefactoringCategory pulledUpCategory = null;
		RefactoringCategory pushedDownCategory = null;
		// TODO this only checks whether we already have such a category
		// created. It might be
		// that such a category has not been created previously (because no
		// results were found
		// for that category. In this case, will need to create a brand new
		// Category object.
		for (RefactoringCategory category : refactoringsList) {
			if (category.getName().equals("PulledUpMethods"))
				pulledUpCategory = category;

			else if (category.getName().equals("PushedDownMethods"))
				pushedDownCategory = category;
		}

		for (Node[] pair : addToPDM) {
			if (pushedDownCategory != null)
				pushedDownCategory.getRefactoringPairs().add(pair);
			withoutFP.remove(pair);
			needsOneMorePass = true;
		}

		for (Node[] pair : addToPUM) {
			if (pulledUpCategory != null)
				pulledUpCategory.getRefactoringPairs().add(pair);
			withoutFP.remove(pair);
			needsOneMorePass = true;
		}
		if (needsOneMorePass)
			return pruneFalsePositives(withoutFP);
		else return withoutFP;
	}

	private List<Node[]> removePairsDetectedInPUM_PDM(List<Node[]> listWithFP) {
		List<Node[]> prunedList = super.pruneFalsePositives(listWithFP);
		List<Node[]> pairsToRemove = new ArrayList<>();
		List<RefactoringCategory> refactoringsList = this.crawler.getRefactoringCategories();
		for (RefactoringCategory category : refactoringsList) {
			if (category.getName().equals("PulledUpMethods")
				|| category.getName().equals("PushedDownMethods")) {
				for (Node[] pair : category.getRefactoringPairs()) {
					for (Node[] prunedPair : prunedList) {
						// The OR below takes care about n->1 and 1->n
						// overlappings
						// between PullUp/PushDown and MoveMethod
						if (prunedPair[0] == pair[0]
							|| prunedPair[1] == pair[1])
							pairsToRemove.add(prunedPair);
					}
				}
			}
		}
		for (Node[] pair : pairsToRemove) {
			prunedList.remove(pair);
		}
		return prunedList;
	}
}
