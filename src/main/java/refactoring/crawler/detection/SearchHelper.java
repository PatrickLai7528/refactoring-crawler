package refactoring.crawler.detection;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import lombok.Getter;
import org.eclipse.core.runtime.IProgressMonitor;
import refactoring.crawler.util.*;

import java.util.*;
import java.util.stream.Collectors;

public class SearchHelper {
	public static List<String> findFieldReferences(FieldNode node) {
		return node.getFieldReferenceToMethod();
	}

	public static List<String> findSuperClassesOf(ClassNode node) {
		return node.getSuperClasses();
	}

	public static List<String> findClassReferences(NamedDirectedMultigraph graph, ClassNode node) {
		return node.getClassesImported();
	}

	public static List<String> findMethodCallers(NamedDirectedMultigraph graph, MethodNode node, boolean withSignature) {
		return graph.vertexSet()
			.stream()
			.filter(n -> (n instanceof MethodNode))
			.map(methodNode -> (MethodNode) methodNode)
			.filter(methodNode ->
				methodNode.getCalledInside()
					.stream()
					.anyMatch(calledMethod -> {
						String expect = withSignature ? calledMethod.getFullyQualifiedNameWithSignature() : calledMethod.getFullyQualifiedNameWithoutSignature();
						String actual = withSignature ? node.getFullyQualifiedName() + node.getSignature() : node.getFullyQualifiedName();
						return expect.equals(actual);
					})
			)
			.map(methodNode -> {
				return withSignature ? methodNode.getFullyQualifiedName() + methodNode.getSignature() : methodNode.getFullyQualifiedName();
			})
			.collect(Collectors.toList());
	}

}
