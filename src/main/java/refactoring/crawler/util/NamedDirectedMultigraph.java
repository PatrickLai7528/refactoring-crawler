package refactoring.crawler.util;

import lombok.Getter;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class NamedDirectedMultigraph extends DirectedMultigraph<Node, Edge> {

	@Getter
	private Map<String, Node> namedVertexMap = new HashMap<>();

	public NamedDirectedMultigraph() {
		super(Edge.class);
	}

	public NamedDirectedMultigraph(Supplier<Node> vertexSupplier, Supplier<Edge> edgeSupplier, boolean weighted) {
		super(vertexSupplier, edgeSupplier, weighted);
	}

	public boolean addNamedVertex(Node v) {
		if (!addVertex(v))
			return false;
		namedVertexMap.put(v.getFullyQualifiedName(), v);
		return true;
	}

	public Node findNamedNode(String name) {
		return namedVertexMap.get(name);
	}
}
