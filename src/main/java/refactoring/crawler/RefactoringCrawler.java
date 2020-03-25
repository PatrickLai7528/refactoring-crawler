package refactoring.crawler;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import lombok.val;
import org.jgrapht.nio.dot.DOTExporter;
import refactoring.crawler.detection.RefactoringDetection;
import refactoring.crawler.detection.RenameMethodDetection;
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
		val oldSource = "package com.MyCourses.dao.impl;/*\n" +
			" * @PackageName com.MyCourses.dao.impl\n" +
			" * @ClassName ForumDAO\n" +
			" * @Author Lai Kin Meng\n" +
			" * @Date 2019-02-25\n" +
			" * @ProjectName MyCoursesServer\n" +
			" */\n" +
			"\n" +
			"import com.MyCourses.dao.IForumDAO;\n" +
			"import com.MyCourses.entity.ForumEntity;\n" +
			"import org.springframework.stereotype.Repository;\n" +
			"import org.springframework.transaction.annotation.Transactional;\n" +
			"\n" +
			"import javax.persistence.EntityManager;\n" +
			"import javax.persistence.PersistenceContext;\n" +
			"\n" +
			"@Repository\n" +
			"@Transactional\n" +
			"public class ForumDAO implements IForumDAO {\n" +
			"\n" +
			"    @PersistenceContext\n" +
			"    private EntityManager entityManager;\n" +
			"\n" +
			"\n" +
			"    @Override\n" +
			"    public ForumEntity retrieveByFid(Long fid) {\n" +
			"        return entityManager.find(ForumEntity.class, fid);\n" +
			"    }\n" +
			"\n" +
			"    @Override\n" +
			"    public void update(ForumEntity forumEntity) {\n" +
			"        ForumEntity f = retrieveByFid(forumEntity.getFid());\n" +
			"        f.setTopic(forumEntity.getTopic());\n" +
			"        f.setQuestionerStudent(forumEntity.getQuestionerStudent());\n" +
			"        f.setQuestionerTeacher(forumEntity.getQuestionerTeacher());\n" +
			"        f.setCommentEntityList(forumEntity.getCommentEntityList());\n" +
			"        entityManager.flush();\n" +
			"    }\n" +
			"}";
		val newSource = "package com.MyCourses.dao.impl;/*\n" +
			" * @PackageName com.MyCourses.dao.impl\n" +
			" * @ClassName ForumDAO\n" +
			" * @Author Lai Kin Meng\n" +
			" * @Date 2019-02-25\n" +
			" * @ProjectName MyCoursesServer\n" +
			" */\n" +
			"\n" +
			"import com.MyCourses.dao.IForumDAO;\n" +
			"import com.MyCourses.entity.ForumEntity;\n" +
			"import org.springframework.stereotype.Repository;\n" +
			"import org.springframework.transaction.annotation.Transactional;\n" +
			"\n" +
			"import javax.persistence.EntityManager;\n" +
			"import javax.persistence.PersistenceContext;\n" +
			"\n" +
			"@Repository\n" +
			"@Transactional\n" +
			"public class ForumDAO implements IForumDAO {\n" +
			"\n" +
			"    @PersistenceContext\n" +
			"    private EntityManager entityManager;\n" +
			"\n" +
			"\n" +
			"    @Override\n" +
			"    public ForumEntity retrieveByFid(Long fid) {\n" +
			"        return entityManager.find(ForumEntity.class, fid);\n" +
			"    }\n" +
			"\n" +
			"    @Override\n" +
			"    public void updateAgain(ForumEntity forumEntity) {\n" +
			"        ForumEntity f = retrieveByFid(forumEntity.getFid());\n" +
			"        f.setTopic(forumEntity.getTopic());\n" +
			"        f.setQuestionerStudent(forumEntity.getQuestionerStudent());\n" +
			"        f.setQuestionerTeacher(forumEntity.getQuestionerTeacher());\n" +
			"        f.setCommentEntityList(forumEntity.getCommentEntityList());\n" +
			"        entityManager.flush();\n" +
			"    }\n" +
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
