package refactoring.crawler.util;

public interface ShinglesStrategy {
    int upperBoundLimitForShinglesBag(int loc, int s_base);

    int upperBoundForClassShingles(int numMethods, int s_base);

    int upperBoundForPackageShingles(int numClasses, int s_base);
}
