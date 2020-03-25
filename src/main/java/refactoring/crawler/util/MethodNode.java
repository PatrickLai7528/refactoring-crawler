package refactoring.crawler.util;

import jdk.nashorn.internal.codegen.CompilerConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MethodNode extends Node {

	@AllArgsConstructor
	public static class CalledMethod {
		@Getter
		@Setter
		private String fullyQualifiedNameWithoutSignature;

		@Getter
		@Setter
		private String fullyQualifiedNameWithSignature;
	}

	@Setter
	@Getter
	private List<CalledMethod> calledInside = new LinkedList<>();

	/**
	 * @param fullyQualifiedName fullyQualifiedName
	 */
	public MethodNode(String fullyQualifiedName) {
		super(fullyQualifiedName, Type.METHOD);
	}
}
