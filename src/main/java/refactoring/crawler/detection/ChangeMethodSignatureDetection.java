package refactoring.crawler.detection;

import refactoring.crawler.util.Edge;
import refactoring.crawler.util.NamedDirectedMultigraph;
import refactoring.crawler.util.Node;

import java.util.List;
import java.util.Set;

public class ChangeMethodSignatureDetection extends MethodDetection {
	public ChangeMethodSignatureDetection(NamedDirectedMultigraph graph, NamedDirectedMultigraph graph2) {
		super(graph, graph2);
	}
}
