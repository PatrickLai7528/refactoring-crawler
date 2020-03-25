package refactoring.crawler;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import refactoring.crawler.detection.RefactoringDetection;
import refactoring.crawler.detection.RenameMethodDetection;
import refactoring.crawler.project.IProject;
import refactoring.crawler.util.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RefactoringCrawler {

	private String projectName;

	public static void main(String[] args) throws IOException {
		// creates an input stream for the file to be parsed
		String source = "import detections.Detection;\n" +
			"import detections.DetectionFactory;\n" +
			"import org.eclipse.jdt.core.dom.AST;\n" +
			"import utils.*;\n" +
			"\n" +
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.core.dom.ASTParser;\n" +
			"\n" +
			"public class RefactoringCrawler {\n" +
			"    public static void main(String[] args) {\n" +
			"        ASTParser parser = ASTParser.newParser(AST.JLS13);\n" +
			"//        parser.setSource();\n" +
			"    }\n" +
			"\n" +
			"    public String detect(Component component1, Component component2) {\n" +
			"\n" +
			"        AbstractGraph originalGraph = ASTUtils.parseToAST(component1);\n" +
			"        AbstractGraph newVersionGraph = ASTUtils.parseToAST(component2);\n" +
			"\n" +
			"        Shingles shingles1 = ShinglesUtils.annotateGraphNodesWithShingles(originalGraph);\n" +
			"        Shingles shingles2 = ShinglesUtils.annotateGraphNodesWithShingles(newVersionGraph);\n" +
			"\n" +
			"        List<Pair> pairs = EntityUtils.findSimilarEntities(shingles1, shingles2);\n" +
			"\n" +
			"        RefactoringLog rlog = new RefactoringLog();\n" +
			"\n" +
			"        for (Detection detection : DetectionFactory.getDetectionList()) {\n" +
			"            for (Pair pair : pairs) {\n" +
			"                if (detection.isRelevant(pair)) {\n" +
			"                    if (detection.isLikelyRefactoring(pair.getFirst(), pair.getSecond(), rlog)) {\n" +
			"                        rlog.add(pair.getFirst(), pair.getSecond(), detection);\n" +
			"                    }\n" +
			"                }\n" +
			"            }\n" +
			"        }\n" +
			"        return rlog.getResult();\n" +
			"    }\n" +
			"}\n";
		CompilationUnit cu;
		// parse the file
		cu = StaticJavaParser.parse(source);


	}

	public RefactoringCrawler(String projectName) {
		this.projectName = projectName;
	}

	private List<CompilationUnit> parse(List<String> files) {
		List<CompilationUnit> resList = new LinkedList<>();
		for (String source : files) {
			resList.add(StaticJavaParser.parse(source));
		}
		return resList;
	}

	public void detect(List<String> oldVersion, List<String> newVersion) {
		ShinglesUtil shinglesUtil = new ShinglesUtil();

		List<CompilationUnit> oldVersionCU = this.parse(oldVersion);
		List<CompilationUnit> newVersionCU = this.parse(newVersion);
		SourceNavigator navigator = new SourceNavigator();
		navigator.setShinglesUtil(shinglesUtil);
		navigator.browseProject(projectName, oldVersionCU);
		NamedDirectedMultigraph originalGraph = navigator.getGraph();

		SourceNavigator navigatorForVersion = new SourceNavigator();
		navigatorForVersion.setShinglesUtil(shinglesUtil);
		navigatorForVersion.browseProject(newVersion);
		NamedDirectedMultigraph versionGraph = navigatorForVersion.getGraph();


		shinglesUtil.initialize(originalGraph, versionGraph);

		detectRenameMethod(1, shinglesUtil, originalGraph, versionGraph);
	}

	private void detectRenameMethod(double tMethod, ShinglesUtil se, NamedDirectedMultigraph oldVersionGraph,
	                                NamedDirectedMultigraph newVersionGraph) {
		List<Node[]> candidateMethods = se.findSimilarMethods();
		RefactoringDetection detector = new RenameMethodDetection(oldVersionGraph, newVersionGraph);
		detector.setThreshold(tMethod);

		List<Node[]> renamedMethods = detector.detectRefactorings(candidateMethods);
		if (renamedMethods.size() > 0) {
			System.out.println(renamedMethods);
//            RefactoringCategory renameMethodCategory = new RefactoringCategory();
//            renameMethodCategory.setName("RenamedMethods");
//            renameMethodCategory.setRefactoringPairs(renamedMethods);
//            refactoringList.add(renameMethodCategory);
		}
	}

}
