package refactoring.crawler.shingles;

public class FactorOf2Strategy implements ShinglesStrategy {

    /** */
    public FactorOf2Strategy() {
        super();
    }

    /* (non-Javadoc)
    * @Override
    * @see edu.uiuc.detectRefactorings.util.Strategy#computeNumShingles(int, int)
    */
    public int upperBoundLimitForShinglesBag(int loc, int s_base) {
        return s_base + (2 * loc);
    }

    public int upperBoundForClassShingles(int numMethods, int s_base) {
        return s_base + (2 * numMethods);
    }

    public int upperBoundForPackageShingles(int numClasses, int s_base) {
        return s_base + (2 * numClasses);
    }
}
