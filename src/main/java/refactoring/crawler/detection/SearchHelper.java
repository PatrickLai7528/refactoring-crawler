package refactoring.crawler.detection;

import java.util.*;
import java.util.stream.Collectors;

import refactoring.crawler.graph.*;

public class SearchHelper {
	public static List<String> findFieldReferences(FieldNode node) {
		return node.getFieldReferenceToMethod();
	}

	public static List<String> findSuperClassesOf(ClassNode node) {
		return node.getSuperClasses();
	}

	public static List<String> findClassReferences(NamedDirectedMultigraph graph, ClassNode classNode) {
//    return node.getClassesImported();
		List<String> res = new LinkedList<>();
		for (Node n : graph.vertexSet()) {
			if (n.getType() == Node.Type.CLASS) {
				for (String imported : ((ClassNode) n).getClassesImported()) {
					if (imported.equals(classNode.getFullyQualifiedName())) {
						res.add(n.getFullyQualifiedName());
					}
				}
			}
		}
		return res;
	}

	public static List<String> findMethodCallers(
		NamedDirectedMultigraph graph, MethodNode node, boolean withSignature) {
		return graph
			.vertexSet()
			.stream()
			.filter(n -> (n instanceof MethodNode))
			.map(methodNode -> (MethodNode) methodNode)
			.filter(
				methodNode ->
					methodNode
						.getCalledInside()
						.stream()
						.anyMatch(
							calledMethod -> {
								String expect =
									withSignature
										? calledMethod.getFullyQualifiedNameWithSignature()
										: calledMethod.getFullyQualifiedNameWithoutSignature();
								String actual =
									withSignature
										? node.getFullyQualifiedName() + node.getSignature()
										: node.getFullyQualifiedName();
								return expect.equals(actual);
							}))
			.map(
				methodNode -> {
					return withSignature
						? methodNode.getFullyQualifiedName() + methodNode.getSignature()
						: methodNode.getFullyQualifiedName();
				})
			.collect(Collectors.toList());
	}
}
