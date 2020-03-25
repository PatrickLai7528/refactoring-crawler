package refactoring.crawler.detection;

import lombok.Getter;
import lombok.Setter;
import refactoring.crawler.util.Edge;
import refactoring.crawler.util.NamedDirectedMultigraph;
import refactoring.crawler.util.Node;

import java.util.*;

public abstract class RefactoringDetection {

    @Getter
    @Setter
    private double threshold;

    protected NamedDirectedMultigraph graph1;
    protected NamedDirectedMultigraph graph2;


    public abstract List<Node[]> pruneOriginalCandidates(List<Node[]> candidates);

    public abstract double computeLikeliness(Node node1, Node node12);

    public abstract List<Edge> filterNamedEdges(Set<Edge> list);

    protected abstract void createCallGraph(Node originalInV2, NamedDirectedMultigraph graph2);

    private static Dictionary<String, String> renamingDictionary;

    public static Dictionary<String, String> getRenamingDictionary() {
        if (renamingDictionary == null)
            renamingDictionary = new Hashtable<>();
        return renamingDictionary;
    }

    public List<Node[]> detectRefactorings(List<Node[]> candidates) {
        List<Node[]> refactoredNodes = new ArrayList<>();
        List<Node[]> listWithFP = doDetectRefactorings(candidates, refactoredNodes);
        return pruneFalsePositives(listWithFP);
    }

    /**
     * A default implementation that prunes all those candidates that have the
     * same qualified name. Subclasses might reuse this when they implement the
     * abstract pruneOriginalCanditates, or they can augment to this initial
     * implementation.
     *
     * @param candidates
     * @return
     */
    public List<Node[]> pruneOriginalCandidatesImpl(List<Node[]> candidates) {
        List<Node[]> prunedCandidates = new ArrayList<>();
        for (Node[] pair : candidates) {
            if (!(pair[0].getFullyQualifiedName().equals(pair[1]
                    .getFullyQualifiedName()))) {

                if (pair[0].isAPI() && pair[1].isAPI()) {
                    Node n2inV1 = graph1.findNamedNode(pair[1]
                            .getFullyQualifiedName());

                    if ((n2inV1 == null)) {
                        prunedCandidates.add(pair);
                    }
                }
            }
        }
        return prunedCandidates;
    }

    /**
     * The client is assumed to be passing it's parent to this method to
     * determine if they are Modulo Renames of each other.
     *
     * @param original
     * @param version
     * @return
     */
    protected boolean isTheSameModuloRename(String original, String version) {
        Dictionary<String, String> dictionary = getRenamingDictionary();
        if (version.equals(dictionary.get(original)))
            return true;
        if (original.lastIndexOf(".") == -1 || version.lastIndexOf(".") == -1)
            return original.equals(version);
        else if (original.substring(original.lastIndexOf(".")).equals(
                version.substring(version.lastIndexOf("."), version.length())))
            return isTheSameModuloRename(
                    extractFullyQualifiedParentName(original),
                    extractFullyQualifiedParentName(version));
        else
            return false;
    }

    public List<Node[]> pruneFalsePositives(List<Node[]> listWithFP) {
        List<Node[]> nodesToRemove = new ArrayList<Node[]>();
        for (Node[] pair : listWithFP) {
            Node original = pair[0];
            Node version = pair[1];
            Node originalInV2 = findNamedNodeWithSignature(graph2, original);
            if (originalInV2 != null) {
                createCallGraph(originalInV2, graph2);
                List<Edge> origIncomingEdges = filterNamedEdges(graph2
                        .incomingEdgesOf(originalInV2));
                List<Edge> verIncomingEdges = filterNamedEdges(graph2
                        .incomingEdgesOf(version));
                List<Edge> origInVer1IncomingEdges = filterNamedEdges(graph1
                        .incomingEdgesOf(original));


                List<Node> origInV2Callers = getCallers(origIncomingEdges);
                List<Node> verCallers = getCallers(verIncomingEdges);
                List<Node> origInV1Callers = getCallers(origInVer1IncomingEdges);

                // remove those pairs where N1InV2 has at least one call site as N2inV2.
                // since a call site cannot be calling both the old and the new entity at the same time
                for (Iterator iterator = verCallers.iterator(); iterator
                        .hasNext(); ) {
                    Node node = (Node) iterator.next();
                    if (origInV2Callers.contains(node))
                        if (!nodesToRemove.contains(pair)) {
                            System.out.println("1st Prune in RD:" + pair[0] + pair[1]);
                            nodesToRemove.add(pair);
                        }
                }

                //check to see whether the N1inV1 has at least one call site as N1inV2. If it has, then the pair
                //is a false positive (since there should be either no more callers for N1inV2 or their call sites
                // should be different
                for (Iterator iterator = origInV1Callers.iterator(); iterator
                        .hasNext(); ) {
                    Node node = (Node) iterator.next();
                    for (Iterator iterator1 = origInV2Callers.iterator(); iterator1
                            .hasNext(); ) {
                        Node callingNode = (Node) iterator1.next();
                        if (node.getFullyQualifiedName().equals(callingNode.getFullyQualifiedName())) {
                            if (!nodesToRemove.contains(pair)) {
                                System.out.println("2nd Prune in RD:" + pair[0] + pair[1]);
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


    private boolean signatureEqualsModuloMoveMethod(Node source, Node target) {
        boolean retval = false;

        if (source.getSignature() == null)
            return false;

        retval = source.getSignature().equals(target.getSignature());

        if (!retval && (this instanceof MoveMethodDetection)) {
            String sourceParent = extractParentSimpleName(source);
            StringTokenizer sourceTokenizer = new StringTokenizer(source
                    .getSignature(), "( , )");
            StringTokenizer targetTokenizer = new StringTokenizer(target
                    .getSignature(), "( , )");
            String[] sourceTokens = new String[sourceTokenizer.countTokens()];
            String[] targetTokens = new String[targetTokenizer.countTokens()];

            for (int i = 0; i < sourceTokens.length; i++) {
                sourceTokens[i] = sourceTokenizer.nextToken();
            }

            for (int i = 0; i < targetTokens.length; i++) {
                targetTokens[i] = targetTokenizer.nextToken();
            }

            if (targetTokens.length == sourceTokens.length + 1) {
                if (!targetTokens[0].trim().equals(sourceParent))
                    return false;
                else {
                    for (int i = 0; i < sourceTokens.length; i++) {
                        if (!sourceTokens[i].trim().equals(
                                targetTokens[i + 1].trim()))
                            return false;
                    }
                    retval = true;
                }
            }
        }
        return retval;
    }

    /**
     * This helper method takes a string containing the dot separated name of a
     * node and it returns the subtring from the beginning up to the last dot
     * (e.g. for pack1.class1.method1 it returns class1)
     *
     * @param original
     * @return
     */
    protected String extractParentSimpleName(Node original) {
        String originalName = original.getFullyQualifiedName();
        String parentName = originalName.substring(0, originalName
                .lastIndexOf("."));
        parentName = parentName.substring(parentName.lastIndexOf(".") + 1);
        return parentName;
    }

    private void pruneOverloadedMethodFP(List<Node[]> listWithFP) {
        List<Node[]> nodesToRemove = new LinkedList<>();
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
                        if (signatureEqualsModuloMoveMethod(source, target2))
                            hasSameNameAndSignature = true;
                }
            }
            if (hasSameNameAndSignature) {
                for (int j = i; j < listWithFP.size(); j++) {
                    Node[] pair2 = (Node[]) listWithFP.get(j);
                    Node source2 = pair2[0];
                    if (source.equals(source2)) {
                        Node target2 = pair2[1];
                        if (source.getSimpleName().equals(
                                target2.getSimpleName()))
                            if (!signatureEqualsModuloMoveMethod(source, target2))
                                if (!nodesToRemove.contains(pair2))
                                    nodesToRemove.add(pair2);
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


    private Node findNamedNodeWithSignature(NamedDirectedMultigraph g, Node original) {
        Dictionary<String, String> dictionary = getRenamingDictionary();
        String fqnParent = extractFullyQualifiedParentName(original);
        String possiblyRenamedFQN = dictionary.get(fqnParent);
        if (possiblyRenamedFQN != null)
            fqnParent = possiblyRenamedFQN;
        // TODO implement find name node
        Node parentNode = g.findNamedNode(fqnParent);

        if (parentNode != null) {
            List<Edge> parentEdges = new LinkedList<>(g.outgoingEdgesOf(parentNode));
            List filteredEdges = filterNamedEdges(parentEdges, Node.Type.METHOD);
            for (Iterator iter = filteredEdges.iterator(); iter.hasNext(); ) {
                Edge edge = (Edge) iter.next();
                Node child = (Node) edge.getTarget();
                if (original.getSimpleName().equals(child.getSimpleName()))
                    if (original.getSignature() != null) {
                        // This handles the method nodes
                        if (original.getSignature()
                                .equals(child.getSignature()))
                            return child;
                    } else
                        // Classes and packages
                        return child;
            }
        }
        return null;
    }

    protected List<Edge> filterNamedEdges(List<Edge> list, Node.Type label) {
        List<Edge> results = new ArrayList<>();
        for (Edge o : list) {
            if (label.equals(o.getLabel())) {
                results.add(o);
            }
        }
        return results;
    }

    protected String extractFullyQualifiedParentName(Node original) {
        String originalName = original.getFullyQualifiedName();
        return extractFullyQualifiedParentName(originalName);
    }

    public String extractFullyQualifiedParentName(String originalName) {
        String fqParentName = "";
        int lastIndex = originalName.lastIndexOf(".");
        if (lastIndex > 0)
            fqParentName = originalName.substring(0, lastIndex);
        return fqParentName;
    }

    private List<Node> getCallers(List incomingEdges) {
        List<Node> callers = new ArrayList<Node>();
        for (Object incomingEdge : incomingEdges) {
            Edge edge = (Edge) incomingEdge;
            callers.add(edge.getSource());
        }
        return callers;
    }

    private List<Node[]> doDetectRefactorings(List<Node[]> candidates, List<Node[]> refactoredNodes) {
        List<Node[]> prunedCandidates = pruneOriginalCandidates(candidates);
        boolean foundNewRefactoring = false;
        for (Object prunedCandidate : prunedCandidates) {
            Node[] pair = (Node[]) prunedCandidate;
            Node original = pair[0];
            Node version = pair[1];
            double likeliness = computeLikeliness(original, version);
            if (likeliness >= threshold) {
                if (!refactoredNodes.contains(pair)) {
                    refactoredNodes.add(pair);
                    foundNewRefactoring = true;
                }
            }
        }

        if (foundNewRefactoring) {
            doDetectRefactorings(candidates, refactoredNodes);
        }
        return refactoredNodes;
    }

    public double computeLikelinessIncomingEdges(List<Edge> edges1, List<Edge> edges2) {
        double count = 0;

        Edge[] arrEdge2 = edges2.toArray(new Edge[edges2.size()]);

        for (Edge edge1 : edges1) {
            Node node1 = edge1.getSource();
            for (int i = 0; i < arrEdge2.length; i++) {
                Edge edge2 = arrEdge2[i];
                if (edge2 != null) {
                    Node node2 = (Node) edge2.getSource();
                    if (isTheSameModuloRename(node1.getFullyQualifiedName(),
                            node2.getFullyQualifiedName())) {
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
}
