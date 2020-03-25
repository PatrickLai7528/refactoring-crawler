package refactoring.crawler.project;

public interface IMethod {

    public String getSource();

    public String getElementName();

    public boolean getIsPublic();

    public boolean getIsDeprecated();

    public boolean getIsProtected();

    public int getFlags();

    public String[] getParameterTypes();
}
