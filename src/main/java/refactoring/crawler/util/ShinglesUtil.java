package refactoring.crawler.util;

import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import java.util.List;
import java.util.ArrayList;

public class ShinglesUtil {
    private ShinglesStrategy shinglesStrategy;

    private DirectedMultigraph<Node, Edge> oldVersionGraph;

    private DirectedMultigraph<Node, Edge> newVersionGraph;

    private List<Node[]> similarMethods;

    private List<Node> oldVersionPackageList;
    private List<Node> oldVersionClassList;
    private List<Node> oldVersionMethodList;
    private List<Node> oldVersionFieldList;

    private List<Node> newVersionPackageList;
    private List<Node> newVersionClassList;
    private List<Node> newVersionMethodList;
    private List<Node> newVersionFieldList;

    private double methodThreshold;

    public ShinglesUtil() {
        this.shinglesStrategy = new FactorOf2Strategy();
    }

    public int[] computeMethodShingles(String str) {
        return null;
    }

    public void initialize(DirectedMultigraph<Node, Edge> oldVersionGraph, DirectedMultigraph<Node, Edge> newVersionGraph) {
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

    /**
     * For the passed graph, fills the respective arrays for packages, classes,
     * methods and fields by using a breadth first iterator.
     *
     * @param graph
     * @param methods
     * @param classes
     * @param packages
     * @param fields
     */
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
        int[] tempArr = (int[]) arr2.clone();
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
}
