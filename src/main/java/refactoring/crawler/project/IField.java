package refactoring.crawler.project;

public interface IField {

    public String getSource();

    public String getElementName();

    public int getFlags();

    public String getTypeSignature();

    public boolean getIsDeprecated();
}
