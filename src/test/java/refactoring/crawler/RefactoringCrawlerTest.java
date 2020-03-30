/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package refactoring.crawler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import refactoring.crawler.graph.Node;
import refactoring.crawler.util.RefactoringCategory;

class RefactoringCrawlerTest {

  private static Dictionary<RefactoringCrawler.Settings, Double> settings;

  @BeforeAll
  static void setUp() {
    settings = new Hashtable<>();

    settings.put(RefactoringCrawler.Settings.T_CHANGE_METHOD_SIGNATURE, 0.5);
    settings.put(RefactoringCrawler.Settings.T_RENAME_METHOD, 0.5);
    settings.put(RefactoringCrawler.Settings.T_MOVE_METHOD, 0.5);
    settings.put(RefactoringCrawler.Settings.T_PUSH_DOWN_METHOD, 0.6);
    settings.put(RefactoringCrawler.Settings.T_PULL_UP_METHOD, 0.6);
    settings.put(RefactoringCrawler.Settings.T_RENAME_CLASS, 0.7);
  }

  @Test
  void testRenameMethod() {
    String source =
        "/*\n"
            + " * This Java source file was generated by the Gradle 'init' task.\n"
            + " */\n"
            + "package refactoring.crawler;\n"
            + "\n"
            + "public class Library {\n"
            + "\tprivate int a;\n"
            + "\tprivate int b;\n"
            + "\n"
            + "\tpublic void foo() {\n"
            + "\t\tSystem.out.println(11111);\n"
            + "\t}\n"
            + "\n"
            + "\tpublic void bar() {\n"
            + "\t\tthis.foo();\n"
            + "\t}\n"
            + "}\n";

    String newVersion =
        "/*\n"
            + " * This Java source file was generated by the Gradle 'init' task.\n"
            + " */\n"
            + "package refactoring.crawler;\n"
            + "\n"
            + "public class Library {\n"
            + "\tprivate int a;\n"
            + "\tprivate int b;\n"
            + "\n"
            + "\tpublic void foo12() {\n"
            + "\t\tSystem.out.println(11111);\n"
            + "\t}\n"
            + "\n"
            + "\tpublic void bar() {\n"
            + "\t\tthis.foo12();\n"
            + "\t}\n"
            + "}\n";

    RefactoringCrawler refactoringCrawler = new RefactoringCrawler("TEST", settings);
    refactoringCrawler.detect(source, newVersion);
    List<RefactoringCategory> categories = refactoringCrawler.getRefactoringCategories();
    assertEquals(1, categories.size());
    assertEquals("RenamedMethods", categories.get(0).getName());
    Node[] nodes = categories.get(0).getRefactoringPairs().get(0);
    assertEquals("refactoring.crawler.Library.foo", nodes[0].getFullyQualifiedName());
    assertEquals("refactoring.crawler.Library.foo12", nodes[1].getFullyQualifiedName());
  }

  @Test
  void testChangeMethodSignature() {
    String source =
        "/*\n"
            + " * This Java source file was generated by the Gradle 'init' task.\n"
            + " */\n"
            + "package refactoring.crawler;\n"
            + "\n"
            + "public class Library {\n"
            + "\tprivate int a;\n"
            + "\tprivate int b;\n"
            + "\n"
            + "\tpublic void foo() {\n"
            + "\t\tSystem.out.println(11111);\n"
            + "\t}\n"
            + "\n"
            + "\tpublic void bar() {\n"
            + "\t\tthis.foo();\n"
            + "\t}\n"
            + "}\n";

    String newVersion =
        "/*\n"
            + " * This Java source file was generated by the Gradle 'init' task.\n"
            + " */\n"
            + "package refactoring.crawler;\n"
            + "\n"
            + "public class Library {\n"
            + "\tprivate int a;\n"
            + "\tprivate int b;\n"
            + "\n"
            + "\tpublic void foo(int i) {\n"
            + "\t\tSystem.out.println(i);\n"
            + "\t}\n"
            + "\n"
            + "\tpublic void bar() {\n"
            + "\t\tthis.foo(1);\n"
            + "\t}\n"
            + "}\n";

    RefactoringCrawler refactoringCrawler = new RefactoringCrawler("TEST", settings);
    refactoringCrawler.detect(source, newVersion);
    List<RefactoringCategory> categories = refactoringCrawler.getRefactoringCategories();
    RefactoringCategory refactoringCategory = categories.get(0);
    Node[] nodes = refactoringCategory.getRefactoringPairs().get(0);
    assertEquals(1, categories.size());
    assertEquals("ChangedMethodSignatures", refactoringCategory.getName());
    assertEquals("METHOD= refactoring.crawler.Library.foo()", nodes[0].toString());
    assertEquals("METHOD= refactoring.crawler.Library.foo(int)", nodes[1].toString());
  }

  @Test
  void testRenameClass() {
    String source =
        "/*\n"
            + " * This Java source file was generated by the Gradle 'init' task.\n"
            + " */\n"
            + "package refactoring.crawler;\n"
            + "\n"
            + "public class Library {\n"
            + "\tprivate int a;\n"
            + "\tprivate int b;\n"
            + "\n"
            + "\tpublic void foo() {\n"
            + "\t\tSystem.out.println(11111);\n"
            + "\t}\n"
            + "\n"
            + "\tpublic void bar() {\n"
            + "\t\tthis.foo();\n"
            + "\t}\n"
            + "}\n";
    String source2 =
        "/*\n"
            + " * This Java source file was generated by the Gradle 'init' task.\n"
            + " */\n"
            + "package refactoring.a;\n"
            + "import refactoring.crawler.Library;\n"
            + "public class A {\n"
            + "private Library library = new Library();\n"
            + "}\n";
    String newVersion =
        "/*\n"
            + " * This Java source file was generated by the Gradle 'init' task.\n"
            + " */\n"
            + "package refactoring.crawler;\n"
            + "\n"
            + "public class Library123 {\n"
            + "\tprivate int a;\n"
            + "\tprivate int b;\n"
            + "\n"
            + "\tpublic void foo() {\n"
            + "\t\tSystem.out.println(1);\n"
            + "\t}\n"
            + "\n"
            + "\tpublic void bar() {\n"
            + "\t\tthis.foo();\n"
            + "\t}\n"
            + "}\n";
    String newVersion2 =
        "/*\n"
            + " * This Java source file was generated by the Gradle 'init' task.\n"
            + " */\n"
            + "package refactoring.a;\n"
            + "import refactoring.crawler.Library123;\n"
            + "public class A {\n"
            + "private Library library = new Library123();\n"
            + "}\n";

    List<String> originals = new LinkedList<>();
    List<String> newVersions = new LinkedList<>();

    originals.add(source);
    originals.add(source2);

    newVersions.add(newVersion);
    newVersions.add(newVersion2);

    RefactoringCrawler refactoringCrawler = new RefactoringCrawler("TEST", settings);
    refactoringCrawler.detect(originals, newVersions);
    List<RefactoringCategory> categories = refactoringCrawler.getRefactoringCategories();
    RefactoringCategory refactoringCategory = categories.get(0);
    Node[] nodes = refactoringCategory.getRefactoringPairs().get(0);
    assertEquals(1, categories.size());
    assertEquals("RenamedClasses", refactoringCategory.getName());
    assertEquals("CLASS= refactoring.crawler.Library", nodes[0].toString());
    assertEquals("CLASS= refactoring.crawler.Library123", nodes[1].toString());
  }

  @Test
  void testMoveMethod() throws IOException {
    String originalArray = TestUtils.readFile("original", "Array.java", null, null);
    String originalArrayStack = TestUtils.readFile("original", "ArrayStack.java", null, null);
    String originalStack = TestUtils.readFile("original", "Stack.java", null, null);

    String newVersionArray =
        TestUtils.readFile(
            "testMoveMethod",
            "Array.java",
            "package refactoring.crawler.testMoveMethod",
            "package refactoring.crawler.original;");
    String newVersionArrayStack =
        TestUtils.readFile(
            "testMoveMethod",
            "ArrayStack.java",
            "package refactoring.crawler.testMoveMethod",
            "package refactoring.crawler.original;");
    String newVersionStack =
        TestUtils.readFile(
            "testMoveMethod",
            "Stack.java",
            "package refactoring.crawler.testMoveMethod",
            "package refactoring.crawler.original;");

    List<String> originalList = new ArrayList<>();
    List<String> newVersionList = new ArrayList<>();

    originalList.add(originalArray);
    originalList.add(originalStack);
    originalList.add(originalArrayStack);

    newVersionList.add(newVersionArray);
    newVersionList.add(newVersionStack);
    newVersionList.add(newVersionArrayStack);

    RefactoringCrawler refactoringCrawler = new RefactoringCrawler("testMoveMethod", settings);
    refactoringCrawler.detect(originalList, newVersionList);
    List<RefactoringCategory> categories = refactoringCrawler.getRefactoringCategories();
    assertEquals(1, categories.size());
    RefactoringCategory refactoringCategory = categories.get(0);
    Node[] nodes = refactoringCategory.getRefactoringPairs().get(0);
    assertEquals("MovedMethods", refactoringCategory.getName());
    assertEquals("METHOD= refactoring.crawler.original.Array.printLast()", nodes[0].toString());
    assertEquals(
        "METHOD= refactoring.crawler.original.ArrayStack.printLast()", nodes[1].toString());
  }

  @Test
  void testPullUpMethod() throws IOException {
    String originalFolder = "testPullUpMethod/original";
    String originalUnit = TestUtils.readFile(originalFolder, "Unit.java", null, null);
    String originalTank = TestUtils.readFile(originalFolder, "Tank.java", null, null);
    String originalSoldier = TestUtils.readFile(originalFolder, "Soldier.java", null, null);

    String newVersionFolder = "testPullUpMethod/newVersion";
    String originalPackage = "package refactoring.crawler.testPullUpMethod.original";
    String newVersionPackage = "package refactoring.crawler.testPullUpMethod.newVersion";

    String newVersionUnit =
        TestUtils.readFile(newVersionFolder, "Unit.java", newVersionPackage, originalPackage);
    String newVersionTank =
        TestUtils.readFile(newVersionFolder, "Tank.java", newVersionPackage, originalPackage);
    String newVersionSoldier =
        TestUtils.readFile(newVersionFolder, "Soldier.java", newVersionPackage, originalPackage);

    List<String> originalList = new ArrayList<>();
    List<String> newVersionList = new ArrayList<>();

    originalList.add(originalUnit);
    originalList.add(originalTank);
    originalList.add(originalSoldier);

    newVersionList.add(newVersionUnit);
    newVersionList.add(newVersionTank);
    newVersionList.add(newVersionSoldier);

    RefactoringCrawler refactoringCrawler = new RefactoringCrawler("test push up method", settings);
    refactoringCrawler.detect(originalList, newVersionList);
    List<RefactoringCategory> categories = refactoringCrawler.getRefactoringCategories();
    assertEquals(1, categories.size());
    RefactoringCategory refactoringCategory = categories.get(0);
    Node[] nodes = refactoringCategory.getRefactoringPairs().get(0);
    assertEquals("PulledUpMethods", refactoringCategory.getName());
    assertEquals(
        "METHOD= refactoring.crawler.testPullUpMethod.original.Tank.getHealth()",
        nodes[0].toString());
    assertEquals(
        "METHOD= refactoring.crawler.testPullUpMethod.original.Unit.getHealth()",
        nodes[1].toString());
  }

  @Test
  void testPushDownMethod() throws IOException {
    String originalFolder = "testPushDownMethod/original";
    String originalUnit = TestUtils.readFile(originalFolder, "Unit.java", null, null);
    String originalTank = TestUtils.readFile(originalFolder, "Tank.java", null, null);
    String originalSoldier = TestUtils.readFile(originalFolder, "Soldier.java", null, null);

    String newVersionFolder = "testPushDownMethod/newVersion";
    String originalPackage = "package refactoring.crawler.testPushDownMethod.original";
    String newVersionPackage = "package refactoring.crawler.testPushDownMethod.newVersion";

    String newVersionUnit =
        TestUtils.readFile(newVersionFolder, "Unit.java", newVersionPackage, originalPackage);
    String newVersionTank =
        TestUtils.readFile(newVersionFolder, "Tank.java", newVersionPackage, originalPackage);
    String newVersionSoldier =
        TestUtils.readFile(newVersionFolder, "Soldier.java", newVersionPackage, originalPackage);

    List<String> originalList = new ArrayList<>();
    List<String> newVersionList = new ArrayList<>();

    originalList.add(originalUnit);
    originalList.add(originalTank);
    originalList.add(originalSoldier);

    newVersionList.add(newVersionUnit);
    newVersionList.add(newVersionTank);
    newVersionList.add(newVersionSoldier);

    RefactoringCrawler refactoringCrawler =
        new RefactoringCrawler("test pull down method", settings);
    refactoringCrawler.detect(originalList, newVersionList);
    List<RefactoringCategory> categories = refactoringCrawler.getRefactoringCategories();
    assertEquals(1, categories.size());
    RefactoringCategory refactoringCategory = categories.get(0);
    Node[] nodes = refactoringCategory.getRefactoringPairs().get(0);
    assertEquals("PushedDownMethods", refactoringCategory.getName());
    assertEquals(
        "METHOD= refactoring.crawler.testPushDownMethod.original.Unit.getHealth()",
        nodes[0].toString());
    assertEquals(
        "METHOD= refactoring.crawler.testPushDownMethod.original.Tank.getHealth()",
        nodes[1].toString());
  }
}
