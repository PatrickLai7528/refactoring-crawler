package refactoring.crawler.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.LinkedList;

public class ClassNode extends Node {


	@Getter
	@Setter
	private List<String> classesImported = new LinkedList<>();

	@Getter
	@Setter
	private List<String> superClasses = new LinkedList<>();

	/**
	 * @param fullyQualifiedName fullyQualifiedName
	 */
	public ClassNode(String fullyQualifiedName) {
		super(fullyQualifiedName, Type.CLASS);
	}


}
