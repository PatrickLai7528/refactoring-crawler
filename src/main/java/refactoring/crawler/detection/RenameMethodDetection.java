package refactoring.crawler.detection;

import org.jgrapht.graph.AbstractBaseGraph;
import refactoring.crawler.util.Edge;
import refactoring.crawler.util.NamedDirectedMultigraph;
import refactoring.crawler.util.Node;

import java.util.*;

public class RenameMethodDetection extends MethodDetection {

	public RenameMethodDetection(NamedDirectedMultigraph oldVersion, NamedDirectedMultigraph newVersion) {
		super(oldVersion, newVersion);
	}

//	@Override
//	public List<Node[]> pruneOriginalCandidates(List<Node[]> candidates) {
//		List<Node[]> prePrunedMethods = super.pruneOriginalCandidatesImpl(candidates);
//		List<Node[]> candidatesWithSameParentClass = new LinkedList<>();
//		for (Node[] pair : prePrunedMethods) {
//			Node original = pair[0];
//			Node version = pair[1];
//			String parentClassOriginal = extractFullyQualifiedParentName(original);
//			String parentClassVersion = extractFullyQualifiedParentName(version);
//			if (isTheSameModuloRename(parentClassOriginal, parentClassVersion)
//				&& (!(original.getSimpleName().equals(version
//				.getSimpleName()))))
//				candidatesWithSameParentClass.add(pair);
//		}
//
//		return candidatesWithSameParentClass;
//	}
//
//	@Override
//	public double computeLikeliness(Node oldVersion, Node newVersion) {
//		return super.analyzeIncomingEdges(oldVersion, newVersion);
//	}

}
