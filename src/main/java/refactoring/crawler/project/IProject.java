package refactoring.crawler.project;

import java.util.List;

public interface IProject {

    public List<IPackage> getPackages();

    public String getProjectName();
}
