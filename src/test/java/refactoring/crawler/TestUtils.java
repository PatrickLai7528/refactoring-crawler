package refactoring.crawler;

import java.io.*;

/**
 * @program: refactoring-crawler
 * @description: Utils Class For Testing
 * @author: laikinmeng
 * @package: ${IntelliJ IDEA}
 * @create: 2020-03-29 13:55
 */
public class TestUtils {
  public static String readFile(
      String folder, String filename, String replacePackage, String packageName)
      throws IOException {
    String filePath = String.format("src/test/java/refactoring/crawler/%s/%s", folder, filename);
    BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filePath)));
    String st;
    StringBuilder sb = new StringBuilder();
    while ((st = bufferedReader.readLine()) != null) {
      sb.append(st);
      sb.append(System.getProperty("line.separator"));
    }
    String fileContent = sb.toString();
    if (packageName != null) {
      fileContent = fileContent.replace(replacePackage, packageName);
    }
    return fileContent;
  }
}
