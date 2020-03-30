package refactoring.crawler.testPushDownMethod.original;

/**
 * @program: refactoring-crawler
 * @description: Class for testing push up method
 * @author: laikinmeng
 * @package: ${IntelliJ IDEA}
 * @create: 2020-03-30 14:48
 */
public class Unit {

  private String name;
  private String unitType;

  protected int health;

  public Unit() {}

  public Unit(String name, String unitType) {
    this.name = name;
    this.unitType = unitType;
  }

  public String getName() {
    return this.name;
  }

  public String getUnitType() {
    return this.unitType;
  }

  public int getHealth() {
    return this.health;
  }
}
