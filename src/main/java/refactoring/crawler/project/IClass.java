package refactoring.crawler.project;

import java.util.List;

public interface IClass {

    public List<IMethod> getMethods();

    public List<IField> getFields();

    public String getFullyQualifiedName();

    public int getFlags();

    public boolean getIsDeprecated();

    public boolean getIsInterface();

    public boolean isPublic();

    public boolean isProtected();
}
