package refactoring.crawler.project;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RCProject implements IProject {

	@Getter
	private String projectName;

	private List<CompilationUnit> units;

	private List<IPackage> packages;

	public RCProject(String projectName, List<String> fileSources) {
		this.projectName = projectName;
		this.units = new LinkedList<>();
		for (String source : fileSources) {
			CompilationUnit compilationUnit = StaticJavaParser.parse(source);
			this.units.add(compilationUnit);
		}
	}

	@Override
	public List<IPackage> getPackages() {
		return null;
	}
}
