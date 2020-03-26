package refactoring.crawler.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class FieldNode extends Node {

	@Getter
	@Setter
	private List<String> fieldReferenceToMethod;

	/**
	 * @param fullyQualifiedName fullyQualifiedName
	 */
	public FieldNode(String fullyQualifiedName) {
		super(fullyQualifiedName, Type.FIELD);
	}
}
