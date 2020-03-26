package refactoring.crawler.util;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;

public class ShinglesUtil {

	private int w = 2;
	private int sClass = 10;
	private int sMethod = 8;
	private double classThreshold = 0.0;

	@Getter
	@Setter
	private double methodThreshold = 0.0;


	private ShinglesStrategy shinglesStrategy;

	private NamedDirectedMultigraph oldVersionGraph;

	private NamedDirectedMultigraph newVersionGraph;

	private List<Node[]> similarMethods = new LinkedList<>();
	private List<Node[]> similarClasses = new LinkedList<>();


	private List<Node> oldVersionPackageList;
	private List<Node> oldVersionClassList;
	private List<Node> oldVersionMethodList;
	private List<Node> oldVersionFieldList;

	private List<Node> newVersionPackageList;
	private List<Node> newVersionClassList;
	private List<Node> newVersionMethodList;
	private List<Node> newVersionFieldList;

	public ShinglesUtil() {
		this.shinglesStrategy = new DefaultStrategy();
//		this.shinglesStrategy = new FactorOf2Strategy();
	}

	public List<String> tokenizer(String s) {
		List<String> list = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(s, " \t \n . \r \" ");
		while (st.hasMoreElements()) {
			list.add(st.nextToken());
		}
		return list;
	}

	private List<List<String>> computeSlidingWindowTokens(List<String> tokenList) {

		List<List<String>> bagOfTokensList = new ArrayList<>();
		ListIterator<String> iter = tokenList.listIterator();
		while (iter.nextIndex() <= (tokenList.size() - w))
		// W=2, iterIndex = size - W identifies the last window
		{
			List<String> tempList = new ArrayList<>();
			for (int i = 1; i <= w; i++) // W=3
			{
				tempList.add(iter.next());
			}
			bagOfTokensList.add(tempList);
			for (int i = 1; i <= w - 1; i++)
				// i<= W-1
				iter.previous();
		}
		return bagOfTokensList;
	}

	private int findNumberOfLines(String str) {
		int retval = 0;
		char[] c = str.toCharArray();
		for (char value : c) {
			if (value == '\n')
				retval++;
		}
		return retval;
	}

	public int[] computeMethodShingles(String str) {
		return computeMethodShingles(str, w, this.shinglesStrategy.upperBoundLimitForShinglesBag(findNumberOfLines(str), this.sMethod));
	}

	public int[] computeMethodShingles(String str, int window, int upperBoundLimit) {
		/*
		 * We are now introducing the idea of finding the number of lines in the
		 * method, and incorporate that into the calculation of shingles, so
		 * that, if there are more lines, then there are going to be more
		 * shingles associated with the method. However, a 1-1 correspondance
		 * will be misleading, thus another method is required.
		 */
		BloomFilter bloomFilter = new BloomFilter();
		List<String> tokenList = tokenizer(str);
		List<List<String>> bagOfWindowedTokens = computeSlidingWindowTokens(tokenList);

		int[] shinglesValues = new int[bagOfWindowedTokens.size()];
		int numberOfWindowedTokens = 0;

		for (List<String> tempList : bagOfWindowedTokens) {
			StringBuilder tokensInOneWindow = new StringBuilder();
			for (int i = 0; i < window; i++) {
				tokensInOneWindow.append(tempList.get(i));
				if (i != window - 1)
					tokensInOneWindow.append(" ");
			}
			int shingle = bloomFilter.hashRabin(tokensInOneWindow.toString());
			shinglesValues[numberOfWindowedTokens] = shingle;
			numberOfWindowedTokens++;
		}

		Arrays.sort(shinglesValues);

		int correctNumberOfShingles = Math.min(upperBoundLimit,
			numberOfWindowedTokens);

		int[] retVal = new int[correctNumberOfShingles];
		System.arraycopy(shinglesValues, 0, retVal, 0, correctNumberOfShingles);
		return retVal;
	}

	public void initialize(NamedDirectedMultigraph oldVersionGraph, NamedDirectedMultigraph newVersionGraph) {
		this.oldVersionGraph = oldVersionGraph;
		this.newVersionGraph = newVersionGraph;

		this.oldVersionPackageList = new ArrayList<>();
		this.oldVersionClassList = new ArrayList<>();
		this.oldVersionMethodList = new ArrayList<>();
		this.oldVersionFieldList = new ArrayList<>();

		this.newVersionPackageList = new ArrayList<>();
		this.newVersionClassList = new ArrayList<>();
		this.newVersionMethodList = new ArrayList<>();
		this.newVersionFieldList = new ArrayList<>();

		initializeElementsLists(oldVersionGraph, oldVersionMethodList, oldVersionClassList,
			oldVersionPackageList, oldVersionFieldList);

		initializeElementsLists(newVersionGraph, newVersionMethodList, newVersionClassList, newVersionPackageList, newVersionFieldList);
	}

	private void initializeElementsLists(DirectedMultigraph<Node, Edge> graph, List<Node> methods,
	                                     List<Node> classes, List<Node> packages, List<Node> fields) {
		// Create a BreadthFirstIterator for the graph
		BreadthFirstIterator<Node, Edge> bfi = new BreadthFirstIterator<>(graph);
		while (bfi.hasNext()) {
			Node n = bfi.next();
			if (n.getType().equals(Node.Type.CLASS)) {
				classes.add(n);
			} else if (n.getType().equals(Node.Type.PACKAGE)) {
				packages.add(n);
			} else if (n.getType().equals(Node.Type.METHOD)) {
				methods.add(n);
			} else if (n.getType().equals(Node.Type.FIELD)) {
				fields.add(n);
			}
		}
	}

	public List<Node[]> findSimilarMethods() {
		if (this.similarMethods.isEmpty()) {
			List<Node[]> similarMethods = new ArrayList<>();
			for (Node m : this.oldVersionMethodList) {
				if (!m.isAPI())
					continue;

				for (Node m2 : this.newVersionMethodList) {
					if (!m2.isAPI())
						continue;

					if (howMuchAlike(m.getShingles(), m2.getShingles()) > methodThreshold) {
						Node[] arr = {m, m2};
						// if (!isThisArrayInTheList(simMet, arr))
						similarMethods.add(arr);
					}
				}
			}
			this.similarMethods = similarMethods;
		}
		return this.similarMethods;
	}

	public double howMuchAlike(int[] arr1, int[] arr2) {
		double finalGrade, similarityFromArr1ToArr2, similarityFromArr2ToArr1;
		similarityFromArr1ToArr2 = howMuchIs1Like2(arr1, arr2);
		similarityFromArr2ToArr1 = howMuchIs1Like2(arr2, arr1);
		finalGrade = (similarityFromArr1ToArr2 + similarityFromArr2ToArr1) / 2.0;
		return finalGrade;
	}

	private double howMuchIs1Like2(int[] arr1, int[] arr2) {
		int[] tempArr = arr2.clone();
		double grade = 0.0;
		for (int value : arr1) {
			for (int j = 0; j < tempArr.length; j++) {
				if (value == tempArr[j]) {
					grade += 1.0 / arr1.length;
					tempArr[j] = Integer.MIN_VALUE;
					break;
				}
			}
		}
		return grade;
	}


	/**
	 * @param classes
	 * @param graph   <br>
	 *                For each class nodes in classes, find the shingles by
	 *                concatenating shingles in methods of its subtree. The
	 *                parameter s_class will determine the maximum size of shingles
	 */
	private void computeClassShingles(List<Node> classes, NamedDirectedMultigraph graph) {
		for (Node clasz : classes) {
			// We will keep the number of methods for the class with the
			// numberOfMethods variable.
			int numberOfMethods = 0;
			List<Edge> outEdges = new ArrayList<>(graph.outgoingEdgesOf(clasz));
			int methodsTotalShingleSize = 0;
			for (Edge e : outEdges) {
				Node neighbor = e.oppositeVertex(clasz);
				if (neighbor.getType().toString().compareToIgnoreCase(Node.Type.METHOD.toString()) == 0) {
					methodsTotalShingleSize += neighbor.getShingles().length;
					// Here we update the method count.
					numberOfMethods++;
				}
			}

			// fill allShinglesFromMethods with shingles from all the methods in
			// the class
			int[] allShinglesFromMethods = new int[methodsTotalShingleSize];
			Arrays.fill(allShinglesFromMethods, Integer.MAX_VALUE);
			int index = 0;
			for (Edge e : outEdges) {
				Node neighbor = e.oppositeVertex(clasz);
				if (neighbor.getType().toString().compareToIgnoreCase(Node.Type.METHOD.toString()) == 0) {
					for (int j = 0; j < neighbor.getShingles().length; j++) {
						allShinglesFromMethods[index] = neighbor.getShingles()[j];
						index++;
					}
				}
			}

			int upperBoundForClassShingles = this.shinglesStrategy
				.upperBoundForClassShingles(numberOfMethods, sClass);
			Arrays.sort(allShinglesFromMethods);

			upperBoundForClassShingles = Math.min(upperBoundForClassShingles,
				allShinglesFromMethods.length);

			int[] retVal = new int[upperBoundForClassShingles];
			if (upperBoundForClassShingles >= 0)
				System.arraycopy(allShinglesFromMethods, 0, retVal, 0, upperBoundForClassShingles);
			clasz.setShingles(retVal);
		}
	}


	public List<Node[]> findSimilarClasses() {
		if (this.similarClasses.isEmpty()) {
			computeClassShingles(oldVersionClassList, oldVersionGraph);
			computeClassShingles(newVersionClassList, newVersionGraph);
			List<Node[]> simClass = new ArrayList<>();
			for (Node c : this.oldVersionClassList) {
				if (!c.isAPI())
					continue;

				for (Node c2 : this.oldVersionClassList) {
					if (!c2.isAPI())
						continue;

					if (howMuchAlike(c.getShingles(), c2.getShingles()) > classThreshold) {
						Node[] arr = {c, c2};
						simClass.add(arr);
					}
				}
			}
			this.similarClasses = simClass;
		}
		return this.similarClasses;
	}

	public List<Node[]> findPullUpMethodCandidates() {
		return findSimilarMethods();
	}

	public List<Node[]> findPushDownMethodCandidates() {
		return findSimilarMethods();
	}
}
