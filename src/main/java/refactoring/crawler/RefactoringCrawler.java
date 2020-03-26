package refactoring.crawler;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import java.util.*;
import lombok.Getter;
import refactoring.crawler.detection.RefactoringDetection;
import refactoring.crawler.detection.classDetection.RenameClassDetection;
import refactoring.crawler.detection.methodDetection.*;
import refactoring.crawler.graph.NamedDirectedMultigraph;
import refactoring.crawler.graph.Node;
import refactoring.crawler.shingles.ShinglesUtil;
import refactoring.crawler.util.*;

public class RefactoringCrawler {

    public static enum Settings {
        T_RENAME_METHOD,
        T_RENAME_CLASS,
        T_MOVE_METHOD,
        T_PULL_UP_METHOD,
        T_PUSH_DOWN_METHOD,
        T_CHANGE_METHOD_SIGNATURE
    }

    private String projectName;
    private Dictionary<Settings, Double> settings;

    @Getter private List<RefactoringCategory> refactoringCategories = new LinkedList<>();

    public RefactoringCrawler(String projectName, Dictionary<Settings, Double> settings) {
        this.projectName = projectName;
        this.settings = settings;
    }

    private static List<CompilationUnit> parse(List<String> files) {
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

    public void detect(String oldVersion, String newVersion) {
        List<String> oldVersionList = new LinkedList<>();
        List<String> newVersionList = new LinkedList<>();

        oldVersionList.add(oldVersion);
        newVersionList.add(newVersion);

        this.detect(oldVersionList, newVersionList);
    }

    public void detect(List<String> oldVersion, List<String> newVersion) {
        ShinglesUtil shinglesUtil = new ShinglesUtil();

        List<CompilationUnit> oldVersionCU = parse(oldVersion);
        List<CompilationUnit> newVersionCU = parse(newVersion);
        SourceNavigator navigator = new SourceNavigator();
        navigator.setShinglesUtil(shinglesUtil);
        navigator.browseProject(projectName, oldVersionCU);
        NamedDirectedMultigraph originalGraph = navigator.getGraph();

        SourceNavigator navigatorForVersion = new SourceNavigator();
        navigatorForVersion.setShinglesUtil(shinglesUtil);
        navigatorForVersion.browseProject(projectName, newVersionCU);
        NamedDirectedMultigraph versionGraph = navigatorForVersion.getGraph();

        shinglesUtil.initialize(originalGraph, versionGraph);

        // must in this order

        double tRenameMethod = this.settings.get(Settings.T_RENAME_METHOD);
        this.detectRenameMethod(tRenameMethod, shinglesUtil, originalGraph, versionGraph);

        double tRenameClass = this.settings.get(Settings.T_RENAME_CLASS);
        detectRenameClass(tRenameClass, shinglesUtil, originalGraph, versionGraph);

        double tMoveMethod = this.settings.get(Settings.T_MOVE_METHOD);
        detectMoveMethod(tMoveMethod, shinglesUtil, originalGraph, versionGraph);

        double tPullUpMethod = this.settings.get(Settings.T_PULL_UP_METHOD);
        detectPullUpMethod(tPullUpMethod, shinglesUtil, originalGraph, versionGraph);

        double tPushDownMethod = this.settings.get(Settings.T_PUSH_DOWN_METHOD);
        detectPushDownMethod(tPushDownMethod, shinglesUtil, originalGraph, versionGraph);

        double tChangeMethodSignature = this.settings.get(Settings.T_CHANGE_METHOD_SIGNATURE);
        detectChangeMethodSignature(tChangeMethodSignature, shinglesUtil, originalGraph, versionGraph);
    }

    private void detectChangeMethodSignature(
            double tChangeMethodSignature,
            ShinglesUtil shinglesUtil,
            NamedDirectedMultigraph originalGraph,
            NamedDirectedMultigraph versionGraph) {
        List<Node[]> candidateChangedMethodSignatures = shinglesUtil.findSimilarMethods();
        RefactoringDetection detector = new ChangeMethodSignatureDetection(originalGraph, versionGraph);
        detector.setThreshold(tChangeMethodSignature);
        List<Node[]> changedMethodSignatures =
                detector.detectRefactorings(candidateChangedMethodSignatures);
        if (changedMethodSignatures.size() > 0) {
            RefactoringCategory changeSignatureCategory = new RefactoringCategory();
            changeSignatureCategory.setName("ChangedMethodSignatures");
            changeSignatureCategory.setRefactoringPairs(changedMethodSignatures);
            this.refactoringCategories.add(changeSignatureCategory);
        }
    }

    private void detectRenameMethod(
            double tMethod,
            ShinglesUtil se,
            NamedDirectedMultigraph oldVersionGraph,
            NamedDirectedMultigraph newVersionGraph) {
        List<Node[]> candidateMethods = se.findSimilarMethods();
        RefactoringDetection detector = new RenameMethodDetection(oldVersionGraph, newVersionGraph);
        detector.setThreshold(tMethod);

        List<Node[]> renamedMethods = detector.detectRefactorings(candidateMethods);
        if (renamedMethods.size() > 0) {
            RefactoringCategory renameMethodCategory = new RefactoringCategory();
            renameMethodCategory.setName("RenamedMethods");
            renameMethodCategory.setRefactoringPairs(renamedMethods);
            this.refactoringCategories.add(renameMethodCategory);
        }
    }

    public void detectRenameClass(
            double tClass,
            ShinglesUtil se,
            NamedDirectedMultigraph originalGraph,
            NamedDirectedMultigraph versionGraph) {
        List<Node[]> candidateClasses = se.findSimilarClasses();
        RefactoringDetection detector = new RenameClassDetection(originalGraph, versionGraph);
        detector.setThreshold(tClass);
        List<Node[]> renamedClasses = detector.detectRefactorings(candidateClasses);
        if (renamedClasses.size() > 0) {
            RefactoringCategory renameClassCategory = new RefactoringCategory();
            renameClassCategory.setName("RenamedClasses");
            renameClassCategory.setRefactoringPairs(renamedClasses);
            this.refactoringCategories.add(renameClassCategory);
        }
    }

    public void detectMoveMethod(
            double tMoveMethod,
            ShinglesUtil se,
            NamedDirectedMultigraph originalGraph,
            NamedDirectedMultigraph versionGraph) {
        List<Node[]> methodCandidates = se.findSimilarMethods();
        se.findSimilarClasses();
        RefactoringDetection detector = new MoveMethodDetection(this, originalGraph, versionGraph);
        detector.setThreshold(tMoveMethod);
        List<Node[]> movedMethods = detector.detectRefactorings(methodCandidates);
        if (movedMethods.size() > 0) {
            RefactoringCategory moveMethodCategory = new RefactoringCategory();
            moveMethodCategory.setName("MovedMethods");
            moveMethodCategory.setRefactoringPairs(movedMethods);
            this.refactoringCategories.add(moveMethodCategory);
        }
    }

    public void detectPullUpMethod(
            double tPullUpMethod,
            ShinglesUtil se,
            NamedDirectedMultigraph originalGraph,
            NamedDirectedMultigraph versionGraph) {
        List<Node[]> candidatePullUpMethods = se.findPullUpMethodCandidates();
        RefactoringDetection detector = new PullUpMethodDetection(originalGraph, versionGraph);
        detector.setThreshold(tPullUpMethod);
        List<Node[]> pullUpMethodResults = detector.detectRefactorings(candidatePullUpMethods);
        if (pullUpMethodResults.size() > 0) {
            RefactoringCategory pullUpCategory = new RefactoringCategory();
            pullUpCategory.setName("PulledUpMethods");
            pullUpCategory.setRefactoringPairs(pullUpMethodResults);
            this.refactoringCategories.add(pullUpCategory);
        }
    }

    public void detectPushDownMethod(
            double tPushDownMethod,
            ShinglesUtil se,
            NamedDirectedMultigraph originalGraph,
            NamedDirectedMultigraph versionGraph) {
        List<Node[]> candidatePushDownMethods = se.findPushDownMethodCandidates();
        RefactoringDetection detector = new PushDownMethodDetection(originalGraph, versionGraph);
        detector.setThreshold(tPushDownMethod);
        List<Node[]> pushDownMethodResults = detector.detectRefactorings(candidatePushDownMethods);
        if (pushDownMethodResults.size() > 0) {
            RefactoringCategory pushDownCategory = new RefactoringCategory();
            pushDownCategory.setName("PushedDownMethods");
            pushDownCategory.setRefactoringPairs(pushDownMethodResults);
            this.refactoringCategories.add(pushDownCategory);
        }
    }
}
