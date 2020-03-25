package refactoring.crawler.project;

import com.github.javaparser.ast.CompilationUnit;

import java.util.List;

public interface IPackage {

    public List<IClass> getClasses();

    public List<IPackage> getChildrenPackages();

    public String getElementName();

//    public List<CompilationUnit> getCompilationUnit();
}
