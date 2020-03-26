package refactoring.crawler.util;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.*;


public class Node {

	public boolean hasCallGraph() {
		return false;
	}

	public void setCreatedCallGraph() {
	}

	public static enum Type {
		PROJECT, PACKAGE, CLASS, METHOD, FIELD, METHOD_CALL, IMPORT, CLASS_REFERENCE, FIELD_REFERENCE
	}


	@Getter
	@Setter
	private String fullyQualifiedName;

	@Setter
	@Getter
	private Node.Type type;

	@Setter
	@Getter
	private int[] shingles;

	@Setter
	@Getter
	private boolean hasCallGraph;

	@Setter
	@Getter
	private String projectName;

	@Setter
	@Getter
	private boolean isAPI = false;

	@Setter
	@Getter
	private String signature;

	@Setter
	@Getter
	private int flags;

	@Setter
	@Getter
	private boolean deprecated = false;

	@Setter
	@Getter
	private boolean isInterface = false;

	@Getter
	@Setter
	private boolean isStatic = false;

	/**
	 * @param fullyQualifiedName fullyQualifiedName
	 * @param type               type
	 */
	public Node(String fullyQualifiedName, Node.Type type) {
		this.fullyQualifiedName = fullyQualifiedName;
		this.type = type;
	}

	public String getSimpleName() {
		int pos = fullyQualifiedName.lastIndexOf(".");
		if (pos != -1) {
			return fullyQualifiedName.substring(pos + 1, fullyQualifiedName
				.length());
		}
		return fullyQualifiedName;
	}

	@Override
	public String toString() {
		if (getSignature() != null)
			return getType() + "= " + getFullyQualifiedName() + getSignature().substring(getSignature().indexOf("("));
		else
			return getType() + "= " + getFullyQualifiedName();
	}
}
