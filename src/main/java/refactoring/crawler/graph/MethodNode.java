package refactoring.crawler.graph;

import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class MethodNode extends Node {

  @AllArgsConstructor
  public static class CalledMethod {
    @Getter @Setter private String fullyQualifiedNameWithoutSignature;

    @Getter @Setter private String fullyQualifiedNameWithSignature;
  }

  @Setter @Getter private List<CalledMethod> calledInside = new LinkedList<>();

  /** @param fullyQualifiedName fullyQualifiedName */
  public MethodNode(String fullyQualifiedName) {
    super(fullyQualifiedName, Type.METHOD);
  }
}
