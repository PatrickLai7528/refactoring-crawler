package refactoring.crawler.util;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import refactoring.crawler.graph.*;
import refactoring.crawler.shingles.ShinglesUtil;

public class SourceNavigator {

  @Getter private NamedDirectedMultigraph graph;

  private ShinglesUtil shinglesUtil;

  private String projectName;

  private int packageCounter = 0;

  private int allMethodsCounter = 0;

  private int allClassCounter = 0;

  private int apiClassCounter = 0;

  private int apiMethodCounter = 0;

  public static boolean useJavadocComments = false;

  public SourceNavigator() {
    graph = new NamedDirectedMultigraph();
  }

  public void setShinglesUtil(ShinglesUtil shinglesUtil) {
    this.shinglesUtil = shinglesUtil;
  }

  public void browseProject(
      @NonNull String projectName, @NonNull List<CompilationUnit> compilationUnits) {
    this.projectName = projectName;
    val projectNode = new Node(projectName, Node.Type.PROJECT);
    this.graph.addNamedVertex(projectNode);
    compilationUnits.forEach(
        cu -> {
          val packageDeclaration = cu.getPackageDeclaration();
          val importDeclarations = cu.getImports();

          val classesImported = new LinkedList<String>();

          importDeclarations.forEach(
              importDeclaration -> {
                classesImported.add(importDeclaration.getName().asString());
              });

          if (packageDeclaration.isPresent()) {
            val packageName = packageDeclaration.get().getNameAsString();
            val packageNode = new Node(packageName, Node.Type.PACKAGE);
            packageNode.setProjectName(projectName);
            this.graph.addNamedVertex(packageNode);
            this.graph.addEdge(projectNode, packageNode, new Edge(Node.Type.PACKAGE));
            cu.getTypes()
                .forEach(
                    type -> {
                      if (type.isClassOrInterfaceDeclaration()) {
                        val classOrInterfaceDeclaration = type.asClassOrInterfaceDeclaration();
                        val className = classOrInterfaceDeclaration.getFullyQualifiedName();
                        if (className.isPresent()) {
                          val classNode = new ClassNode(className.get());
                          classNode.setProjectName(projectName);

                          List<String> extendedClasses =
                              classOrInterfaceDeclaration
                                  .getExtendedTypes()
                                  .stream()
                                  .map(extendType -> extendType.resolve().getQualifiedName())
                                  .collect(Collectors.toList());

                          classNode.setStatic(classOrInterfaceDeclaration.isStatic());
                          classNode.setSuperClasses(extendedClasses);
                          classNode.setClassesImported(classesImported);
                          classNode.setDeprecated(
                              classOrInterfaceDeclaration
                                  .getAnnotationByClass(Deprecated.class)
                                  .isPresent());
                          classNode.setInterface(classOrInterfaceDeclaration.isInterface());

                          this.allClassCounter += 1;
                          if (classOrInterfaceDeclaration.isProtected()
                              || classOrInterfaceDeclaration.isPublic()) {
                            classNode.setAPI(true);
                            this.apiClassCounter += 1;
                          }
                          graph.addNamedVertex(classNode);
                          graph.addEdge(packageNode, classNode, new Edge(Node.Type.CLASS));

                          // methods
                          List<MethodDeclaration> methodDeclarations =
                              classOrInterfaceDeclaration.getMethods();

                          // filed fqn -> methods fqn
                          Map<String, List<String>> fieldReferenceToMethods = new HashMap<>();

                          methodDeclarations.forEach(
                              method -> {
                                String statementBody = "";
                                val methodBody = method.getBody();

                                if (methodBody.isPresent()) {
                                  if (classNode.isInterface() || (useJavadocComments)) {
                                    statementBody = methodBody.get().toString().trim();
                                  } else
                                    statementBody =
                                        statementBody(methodBody.get().toString()).trim();
                                  int[] shingles =
                                      shinglesUtil.computeMethodShingles(statementBody);
                                  String qualifiedName =
                                      classNode.getFullyQualifiedName()
                                          + "."
                                          + method.getNameAsString();
                                  MethodNode methodNode = new MethodNode(qualifiedName);

                                  List<String> methodReferencedFields =
                                      method
                                          .findAll(FieldAccessExpr.class)
                                          .stream()
                                          .map(
                                              fieldAccessExpr ->
                                                  classNode.getFullyQualifiedName()
                                                      + "."
                                                      + fieldAccessExpr.resolve().getName())
                                          .collect(Collectors.toList());

                                  methodReferencedFields.forEach(
                                      field -> {
                                        if (fieldReferenceToMethods.containsKey(field)) {
                                          List<String> methods = fieldReferenceToMethods.get(field);
                                          methods.add(qualifiedName);
                                          fieldReferenceToMethods.put(field, methods);
                                        }
                                      });

                                  List<MethodNode.CalledMethod> calledMethodList =
                                      method
                                          .findAll(MethodCallExpr.class)
                                          .stream()
                                          .map(MethodCallExpr::resolve)
                                          .map(
                                              resolved ->
                                                  new MethodNode.CalledMethod(
                                                      resolved.getQualifiedName(),
                                                      resolved.getQualifiedSignature()))
                                          .collect(Collectors.toList());

                                  methodNode.setCalledInside(calledMethodList);

                                  allMethodsCounter += 1;
                                  if (method.isPublic() || method.isProtected()) {
                                    methodNode.setAPI(true);
                                    this.apiMethodCounter += 1;
                                  }
                                  if (method.getAnnotationByClass(Deprecated.class).isPresent())
                                    methodNode.setDeprecated(true);
                                  methodNode.setStatic(method.isStatic());
                                  methodNode.setProjectName(projectName);
                                  methodNode.setShingles(shingles);
                                  //									methodNode.setFlags(method.getFlags());
                                  methodNode.setSignature(method.getSignature().toString());
                                  graph.addNamedVertex(methodNode);
                                  graph.addEdge(classNode, methodNode, new Edge(Node.Type.METHOD));
                                }
                              });

                          // fields
                          List<FieldDeclaration> fieldDeclarations =
                              classOrInterfaceDeclaration.getFields();
                          fieldDeclarations.forEach(
                              field -> {
                                int[] shingles =
                                    this.shinglesUtil.computeMethodShingles(
                                        field.getVariables().toString());
                                String fullyQualifiedName =
                                    classNode.getFullyQualifiedName()
                                        + "."
                                        + field.getVariables().toString();
                                FieldNode fieldNode = new FieldNode(fullyQualifiedName);
                                fieldNode.setFieldReferenceToMethod(
                                    fieldReferenceToMethods.get(fullyQualifiedName));
                                fieldNode.setProjectName(this.projectName);
                                fieldNode.setShingles(shingles);
                                //								fieldNode.setFlags(field.getFlags());
                                fieldNode.setStatic(field.isStatic());
                                fieldNode.setSignature(field.getCommonType().asString());
                                fieldNode.setDeprecated(
                                    field.getAnnotationByClass(Deprecated.class).isPresent());
                                graph.addNamedVertex(fieldNode);
                                graph.addEdge(classNode, fieldNode, new Edge(Node.Type.FIELD));
                              });
                        }
                      }
                    });
          }
        });

    //		val packages = compilationUnits.stream()
    //			.map(CompilationUnit::getPackageDeclaration)
    //			.filter(Optional::isPresent)
    //			.collect(Collectors.toList());
    ////        this.graph.addVertex(projectNode);
    //		this.graph.addNamedVertex(projectNode);
    //		packages.forEach(p -> p.ithis.browseInPackageRoot(p, projectNode));
  }

  //	private void browseInPackageRoot(@NonNull IPackage iPackage, @NonNull Node projectNode) {
  //		val childrenPackages = iPackage.getChildrenPackages();
  //		childrenPackages.forEach(childrenPackage -> {
  //			Node packageNode = new Node(childrenPackage.getElementName(), Node.Type.PACKAGE);
  //			packageNode.setProjectName(this.projectName);
  ////            graph.addVertex(packageNode);
  //			graph.addNamedVertex(packageNode);
  ////            graph.addEdge(projectNode, packageNode);
  //			graph.addEdge(projectNode, packageNode, new Edge(Node.Type.PACKAGE));
  //			this.browseInPackage(childrenPackage, packageNode);
  //			this.packageCounter += 1;
  //		});
  //	}

  //	private void browseInPackage(@NonNull IPackage pckg, @Nonnull Node packageNode) {
  //		val classes = pckg.getClasses();
  //		classes.forEach(clz -> {
  //			val classNode = new Node(clz.getFullyQualifiedName(), Node.Type.CLASS);
  //			classNode.setProjectName(this.projectName);
  //			classNode.setFlags(clz.getFlags());
  ////            graph.addVertex(classNode);
  ////            graph.addEdge(packageNode, classNode);
  //			graph.addNamedVertex(classNode);
  //			graph.addEdge(packageNode, classNode, new Edge(Node.Type.CLASS));
  //			this.browseClass(clz, classNode);
  //			classNode.setDeprecated(clz.getIsDeprecated());
  //			classNode.setInterface(clz.getIsInterface());
  //			this.allClassCounter += 1;
  //			if (clz.isProtected() || clz.isPublic()) {
  //				classNode.setAPI(true);
  //				this.apiClassCounter += 1;
  //			}
  //		});
  //	}

  //	private void browseClass(@NonNull IClass clz, @NonNull Node classNode) {
  //		val methods = clz.getMethods();
  //		browseMethods(methods, classNode);
  //		val fields = clz.getFields();
  //		browseFields(fields, classNode);
  //	}
  //
  //	private void browseMethods(@NonNull List<IMethod> methods, @NonNull Node classNode) {
  //		methods.forEach(method -> {
  //			String statementBody = "";
  //			if (classNode.isInterface() || (useJavadocComments)) {
  //				statementBody = method.getSource().trim();
  //			} else
  //				statementBody = statementBody(method.getSource()).trim();
  //			int[] shingles = shinglesUtil.computeMethodShingles(statementBody);
  //			String qualifiedName = classNode.getFullyQualifiedName() + "."
  //				+ method.getElementName();
  //			Node methodNode = new Node(qualifiedName, Node.Type.METHOD);
  //
  //			allMethodsCounter += 1;
  //			if (method.getIsPublic() || method.getIsProtected()) {
  //				methodNode.setAPI(true);
  //				this.apiMethodCounter += 1;
  //			}
  //			if (method.getIsDeprecated())
  //				methodNode.setDeprecated(true);
  //			methodNode.setProjectName(projectName);
  //			methodNode.setShingles(shingles);
  //			methodNode.setFlags(method.getFlags());
  //			methodNode.setSignature(getUnqualifiedMethodSignature(method));
  ////            graph.addVertex(methodNode);
  ////            graph.addEdge(classNode, methodNode);
  //			graph.addNamedVertex(methodNode);
  //			graph.addEdge(classNode, methodNode, new Edge(Node.Type.METHOD));
  //		});
  //	}
  //
  //
  //	private String getUnqualifiedMethodSignature(IMethod method) {
  //		StringBuilder buffer = new StringBuilder();
  //
  //		buffer.append('(');
  //
  //		String[] types = method.getParameterTypes();
  //		for (int i = 0; i < types.length; i++) {
  //			if (i > 0)
  //				buffer.append(", "); //$NON-NLS-1$
  //			String typeSig = Signature.toString(types[i]);
  //			buffer.append(typeSig);
  //		}
  //		buffer.append(')');
  //
  //		return buffer.toString();
  //	}
  //
  //	private void browseFields(@NonNull List<IField> fields, @NonNull Node classNode) {
  //		fields.forEach(field -> {
  //			int[] shingles = this.shinglesUtil.computeMethodShingles(field.getSource());
  //			String fullyQualifiedName = classNode.getFullyQualifiedName() + "." + field.getElementName();
  //			Node fieldNode = new Node(fullyQualifiedName, Node.Type.FIELD);
  //			fieldNode.setProjectName(this.projectName);
  //			fieldNode.setShingles(shingles);
  //			fieldNode.setFlags(field.getFlags());
  //			fieldNode.setSignature(field.getTypeSignature());
  //			fieldNode.setDeprecated(field.getIsDeprecated());
  ////            graph.addVertex(fieldNode);
  ////            graph.addEdge(classNode, fieldNode);
  //			graph.addNamedVertex(fieldNode);
  //			graph.addEdge(classNode, fieldNode, new Edge(Node.Type.FIELD));
  //		});
  //	}
  //

  /**
   * This prunes away the javadoc comments
   *
   * @param source
   * @return
   */
  private String statementBody(String source) {
    int lastAtChar = source.lastIndexOf("@");
    if (lastAtChar == -1) lastAtChar = 0;
    int openingBracket = source.indexOf("{", lastAtChar);
    int closingBracket = source.lastIndexOf("}");
    if (openingBracket != -1) return source.substring(openingBracket + 1, closingBracket);
    return source;
  }
}
