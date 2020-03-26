package refactoring.crawler.graph;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class FieldNode extends Node {

  @Getter @Setter private List<String> fieldReferenceToMethod;

  /** @param fullyQualifiedName fullyQualifiedName */
  public FieldNode(String fullyQualifiedName) {
    super(fullyQualifiedName, Type.FIELD);
  }
}
