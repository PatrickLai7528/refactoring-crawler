package refactoring.crawler.util;

/**
 * @author Can Comertoglu
 */
public class DefaultStrategy implements ShinglesStrategy {

	/**
	 *
	 */
	public DefaultStrategy() {
		super();
	}

	/* (non-Javadoc)
	 * @see edu.uiuc.detectRefactorings.util.Strategy#computeNumShingles(int, int)
	 */
	public int upperBoundLimitForShinglesBag(int loc, int s_base) {
		return s_base;
	}

	public int upperBoundForClassShingles(int numMethods, int s_base) {
		return s_base;
	}

	public int upperBoundForPackageShingles(int numClasses, int s_base) {
		return s_base;
	}

}
