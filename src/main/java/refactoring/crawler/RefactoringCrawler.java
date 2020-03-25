package refactoring.crawler;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.val;
import org.jgrapht.nio.dot.DOTExporter;
import refactoring.crawler.detection.RefactoringDetection;
import refactoring.crawler.detection.RenameMethodDetection;
import refactoring.crawler.detection.SearchHelper;
import refactoring.crawler.project.IProject;
import refactoring.crawler.util.*;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RefactoringCrawler {

	private String projectName;

	public static void main(String[] args) throws IOException {
		val crawler = new RefactoringCrawler("project name");
		val oldSource = "package com.MyCourses.dao.impl;" +
			"public class A{" +
			"   public void foo(){" +
			"       System.out.println(1);" +
			"   }" +
			"   " +
			"   public void bar(){" +
			"       this.foo();" +
			"   }" +
			"}";
		val newSource = "package com.MyCourses.dao.impl;" +
			"public class A{" +
			"   public void fooA(){" +
			"       System.out.println(1);" +
			"   }" +
			"   public void bar(){" +
			"       this.fooA();" +
			"   }" +
			"}";

		val oldList = new ArrayList<String>();
		val newList = new ArrayList<String>();

		oldList.add(oldSource);
		newList.add(newSource);

		crawler.detect(oldList, newList);

	}

	public RefactoringCrawler(String projectName) {
		this.projectName = projectName;
	}

	private List<CompilationUnit> parse(List<String> files) {
		TypeSolver typeSolver = new ReflectionTypeSolver();
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
		StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

		List<CompilationUnit> resList = new LinkedList<>();
		for (String source : files) {
			CompilationUnit cu = StaticJavaParser.parse(source);
			resList.add(cu);
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
		navigatorForVersion.browseProject(projectName, newVersionCU);
		NamedDirectedMultigraph versionGraph = navigatorForVersion.getGraph();

		shinglesUtil.initialize(originalGraph, versionGraph);

		System.out.println("-----original graph-----");
		for (Edge e : originalGraph.edgeSet()) {
			System.out.println(originalGraph.getEdgeSource(e) + " --> " + originalGraph.getEdgeTarget(e));
		}

		System.out.println("-----new version graph-----");
		for (Edge e : versionGraph.edgeSet()) {
			System.out.println(versionGraph.getEdgeSource(e) + " --> " + versionGraph.getEdgeTarget(e));
		}

		System.out.println(originalGraph.getNamedVertexMap());

		detectRenameMethod(1, shinglesUtil, originalGraph, versionGraph);
		val res = SearchHelper.findMethodCallers(originalGraph, (MethodNode) originalGraph.findNamedNode("com.MyCourses.dao.impl.A.foo"), false);
		System.out.println(res);
	}

	private void detectRenameMethod(double tMethod, ShinglesUtil se, NamedDirectedMultigraph oldVersionGraph,
	                                NamedDirectedMultigraph newVersionGraph) {
		List<Node[]> candidateMethods = se.findSimilarMethods();
		RefactoringDetection detector = new RenameMethodDetection(oldVersionGraph, newVersionGraph);
//		detector.setThreshold(tMethod);

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
