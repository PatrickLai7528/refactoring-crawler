package refactoring.crawler.testPullUpMethod.newVersion;

/**
 * @program: refactoring-crawler
 * @description: Class for testing push up method
 * @author: laikinmeng
 * @package: ${IntelliJ IDEA}
 * @create: 2020-03-30 14:53
 */
public class Tank extends Unit {

  public Tank() {
    super();
  }

  public Tank(String name) {
    super(name, "Tank");
  }

  public void printUnitInfo() {
    System.out.println(this.getName() + this.getHealth());
  }
}
