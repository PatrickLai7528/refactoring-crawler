package refactoring.crawler.visitor;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodVisitor extends VoidVisitorAdapter<Object> {

    public void visit(MethodDeclaration n, Object arg) {
        System.out.println(n.getBody());
        System.out.println(n.getName());
    }

    @Override
    public void visit(AnnotationDeclaration n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(AnnotationMemberDeclaration n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayAccessExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayCreationExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayInitializerExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(AssertStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(AssignExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(BinaryExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(BlockComment n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(BlockStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(BooleanLiteralExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(BreakStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(CastExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(CatchClause n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(CharLiteralExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceType n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(CompilationUnit n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ConditionalExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ConstructorDeclaration n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ContinueStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(DoStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(DoubleLiteralExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(EmptyStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(EnclosedExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(EnumConstantDeclaration n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(EnumDeclaration n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ExpressionStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldAccessExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldDeclaration n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ForEachStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ForStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(IfStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(InitializerDeclaration n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(InstanceOfExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(IntegerLiteralExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(JavadocComment n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(LabeledStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(LineComment n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(LongLiteralExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(MarkerAnnotationExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(MemberValuePair n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(NameExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(NormalAnnotationExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(NullLiteralExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ObjectCreationExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(PackageDeclaration n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(Parameter n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(PrimitiveType n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(Name n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(SimpleName n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayType n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayCreationLevel n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(IntersectionType n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(UnionType n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ReturnStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(SingleMemberAnnotationExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(StringLiteralExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(SuperExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(SwitchEntry n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(SwitchStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(SynchronizedStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ThisExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ThrowStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(TryStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(LocalClassDeclarationStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(TypeParameter n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(UnaryExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(UnknownType n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(VariableDeclarationExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(VariableDeclarator n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(VoidType n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(WhileStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(WildcardType n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(LambdaExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodReferenceExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(TypeExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(NodeList n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ImportDeclaration n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ModuleDeclaration n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ModuleRequiresDirective n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ModuleExportsDirective n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ModuleProvidesDirective n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ModuleUsesDirective n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ModuleOpensDirective n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(UnparsableStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ReceiverParameter n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(VarType n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(Modifier n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(SwitchExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(TextBlockLiteralExpr n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(YieldStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(JavadocBlockTag n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(JavadocContent n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(JavadocDescription n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(JavadocInlineTag n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(JavadocSnippet n, Object arg) {
        super.visit(n, arg);
    }
}