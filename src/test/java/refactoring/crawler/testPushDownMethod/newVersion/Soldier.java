package refactoring.crawler.testPushDownMethod.newVersion;

/**
 * @program: refactoring-crawler
 * @description: Class for testing push up method
 * @author: laikinmeng
 * @package: ${IntelliJ IDEA}
 * @create: 2020-03-30 14:50
 */
public class Soldier extends Unit {

  public Soldier() {
    super();
  }

  public Soldier(String name) {
    super(name, "Solider");
  }

  public int getHealth() {
    return this.health;
  }

  public void printUnitInfo() {
    System.out.println(this.getName() + this.getHealth());
  }
}
