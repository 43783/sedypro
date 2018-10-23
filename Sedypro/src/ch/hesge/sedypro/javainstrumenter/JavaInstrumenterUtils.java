package ch.hesge.sedypro.javainstrumenter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ch.hesge.sedypro.utils.StringUtils;

/**
 * Utility class related to parsing source file
 */
public class JavaInstrumenterUtils {

	/**
	 * Return the name of the compilation unit is defining.
	 * 
	 * @param compilationUnit
	 * @return the name of the current package
	 */
	private static String getPackageName(CompilationUnit compilationUnit) {
		if (compilationUnit.getPackage() == null) {
			return "default";
		}
		return compilationUnit.getPackage().getName().getFullyQualifiedName();
	}

	/**
	 * Return true if the method passed in argument is owned by an interface.
	 * Otherwise, return false.
	 * 
	 * @param declaration
	 *            the method to use
	 * @return true for interface, false otherwise
	 */
	public static boolean isInterfaceMethod(MethodDeclaration declaration) {

		ASTNode parent = declaration.getParent();

		// Lookup trough the ownership hierarchy the declaring class
		while (parent != null) {

			if (TypeDeclaration.class.isAssignableFrom(parent.getClass())) {
				return ((TypeDeclaration) parent).isInterface();
			}

			parent = parent.getParent();
		}

		return false;
	}

	/**
	 * Return the class owning the declaration passed in argument.
	 * 
	 * @param declaration
	 * @return a classname
	 */
	public static String getClassName(MethodDeclaration declaration) {

		String className = "UndefinedClass";
		String anonymousType = null;

		ASTNode parent = declaration.getParent();

		// Lookup trough the ownership hierarchy of the declaring class
		while (parent != null) {

			if (TypeDeclaration.class.isAssignableFrom(parent.getClass())) {
				className = ((TypeDeclaration) parent).getName().getFullyQualifiedName();
				break;
			}

			parent = parent.getParent();
		}

		parent = declaration.getParent();

		// Lookup trough the ownership hierarchy if the method is owned by an
		// anonymous class
		while (parent != null) {

			if (ClassInstanceCreation.class.isAssignableFrom(parent.getClass())) {
				anonymousType = ((ClassInstanceCreation) parent).getType().toString();
				break;
			}

			parent = parent.getParent();
		}

		if (anonymousType != null) {
			return className + "$" + anonymousType;
		}

		return className;
	}

	public static String getSuperClassName(MethodDeclaration declaration) {
		
		String superClassName = "";
		
		ASTNode parent = declaration.getParent();

		// Lookup trough the ownership hierarchy of the declaring class
		while (parent != null) {

			if (TypeDeclaration.class.isAssignableFrom(parent.getClass())) {
				TypeDeclaration typeDeclaration = (TypeDeclaration) parent;
				if (typeDeclaration.getSuperclassType() != null) {
					Type superclassType = typeDeclaration.getSuperclassType();
					
					if (superclassType.isSimpleType()) {
						SimpleType type = (SimpleType) superclassType;
						superClassName += type.getName().getFullyQualifiedName() + "#";
					}
					else if (superclassType.isQualifiedType()) {
						QualifiedType type = (QualifiedType) superclassType;
						superClassName += type.getName().getFullyQualifiedName() + "#";
					}
					break;
				}
			}

			parent = parent.getParent();
		}

		superClassName += "Object";
		
		return superClassName;
	}
	
	/**
	 * Return the method name defined by the declaration.
	 * 
	 * @param declaration
	 * @return a method name
	 */
	private static String getMethodName(MethodDeclaration declaration) {
		return declaration.getName().getFullyQualifiedName();
	}

	/**
	 * Return a string corresponding to all parameters defined in a method
	 * declaration.
	 * 
	 * @param declaration
	 * @return the parameter list separated by comma.
	 */
	@SuppressWarnings("unchecked")
	private static String getParameterTypes(MethodDeclaration declaration) {

		String parametersString = "";

		for (SingleVariableDeclaration parameter : (List<SingleVariableDeclaration>) declaration.parameters()) {
			parametersString += parameter.getType().toString();
			parametersString += " ";
			parametersString += parameter.getName().getFullyQualifiedName();
			parametersString += ",";
		}

		parametersString = StringUtils.removeLastChar(parametersString);

		if (parametersString.trim().length() == 0) {
			parametersString = "";
		}
		
		return parametersString;
	}

	/**
	 * Return an expression allowing retrieve all argument's values.
	 * 
	 * @param declaration
	 * @return an expression.
	 */
	private static Expression getArgumentValues(AST ast, MethodDeclaration declaration) {

		@SuppressWarnings("unchecked")
		List<SingleVariableDeclaration> parameters = new ArrayList<>((List<SingleVariableDeclaration>) declaration.parameters());

		Expression argValuesExpr = null;
		
		if (parameters.size() == 0) {
			
			StringLiteral expr = ast.newStringLiteral(); 
			expr.setLiteralValue("");
			argValuesExpr = expr;
		}
		else {
			
			// Retrieve imbricated expression concatenating all arguments		
			Expression valuesExpr = getArgumentValuesAsExpression2(ast, parameters);
			
			// Create ("" + expr) expression, so to cast everything to String
			InfixExpression expr = ast.newInfixExpression();
			StringLiteral stringLiteral = ast.newStringLiteral(); 
			stringLiteral.setLiteralValue("");
			expr.setLeftOperand(stringLiteral);
			expr.setOperator(Operator.PLUS);
			expr.setRightOperand(valuesExpr);
			argValuesExpr = expr;
		}
		
		return argValuesExpr;
	}

	/**
	 * Recursively build the imbricated expression describing all arguments values.
	 * 
	 * @param ast
	 * @param parameters
	 * @return
	 */
	private static Expression getArgumentValuesAsExpression2(AST ast, List<SingleVariableDeclaration> parameters) {
		
		Expression expr = null;
		
		if (parameters.isEmpty()) {
			StringLiteral emptyStringExpr = ast.newStringLiteral(); 
			emptyStringExpr.setLiteralValue("");
			expr = emptyStringExpr;
		}
		else if (parameters.size() == 1) {
			SingleVariableDeclaration parameter = parameters.get(0);
			expr = ast.newSimpleName(parameter.getName().toString());
		}
		else {
					
			SingleVariableDeclaration parameter = parameters.remove(0);
			
			Expression expr1 = ast.newSimpleName(parameter.getName().toString());
			Expression expr2 = getArgumentValuesAsExpression2(ast, parameters);
			
			// Create (expr1 + ",") expression
			InfixExpression infix1 = ast.newInfixExpression();
			infix1.setLeftOperand(expr1);
			infix1.setOperator(Operator.PLUS);
			StringLiteral commaExpr1 = ast.newStringLiteral(); 
			commaExpr1.setLiteralValue(",");
			infix1.setRightOperand(commaExpr1);
			
			// Create (expr1 + "," + expr2) expression
			InfixExpression infix2 = ast.newInfixExpression();
			infix2.setLeftOperand(infix1);
			infix2.setOperator(Operator.PLUS);
			infix2.setRightOperand(expr2);
			
			expr = infix2;
		}
		
		return expr;
	}
	
	/**
	 * Retrieve the return type of the method passed in argument.
	 * 
	 * @param declaration
	 * @return the return type
	 */
	private static String getReturnType(MethodDeclaration declaration) {
		return declaration.getReturnType2() == null ? "void" : declaration.getReturnType2().toString();
	}

	/**
	 * Create a MethodInvocation based on the method specified in argument.
	 * 
	 * @param compilationUnit
	 *            the top level unit containing the method
	 * @param traceType
	 *            type of trace = "entering" or "exiting"
	 * @param declaration
	 *            the method declaration
	 * @return the method invocation for the class, method passed in argument.
	 */
	@SuppressWarnings("unchecked")
	public static MethodInvocation createTraceInvocation(CompilationUnit compilationUnit, String traceType, MethodDeclaration declaration) {

		AST ast = compilationUnit.getAST();

		boolean isStaticMethod = Modifier.isStatic(declaration.getModifiers()); 
		
		// Retrieve the package name
		StringLiteral packageNameLLiteral = ast.newStringLiteral();
		packageNameLLiteral.setLiteralValue(JavaInstrumenterUtils.getPackageName(compilationUnit));

		// Retrieve the classname
		Expression classNameExpression;
		String classname = JavaInstrumenterUtils.getClassName(declaration);
		String superClassName = JavaInstrumenterUtils.getSuperClassName(declaration);
		
		if (isStaticMethod) {			

			// Retrieve the classname for static methods
			StringLiteral stringLiteral = ast.newStringLiteral(); 
			stringLiteral.setLiteralValue(classname + "#Object");
			classNameExpression = stringLiteral;
		}		
		else {
			
			// Concatenate classname and superClassName
			StringLiteral stringLiteral = ast.newStringLiteral();
			stringLiteral.setLiteralValue(classname + "#" + superClassName);

			classNameExpression = stringLiteral;
		}
		
		// Retrieve the method name
		StringLiteral methodNameLiteral = ast.newStringLiteral();
		String methodName = JavaInstrumenterUtils.getMethodName(declaration);
		if (methodName.equals(classname)) {
			methodName = "new";
		}
		methodNameLiteral.setLiteralValue(methodName);

		// Retrieve the list of parameter types
		StringLiteral parameterTypesLiteral = ast.newStringLiteral();
		String paramTypesExpression = JavaInstrumenterUtils.getParameterTypes(declaration);
		parameterTypesLiteral.setLiteralValue(paramTypesExpression);

		// Retrieve the list of argument's values
		Expression argumentValuesExpression = getArgumentValues(ast, declaration);
		
		// Retrieve the return type
		StringLiteral returnTypeLiteral = ast.newStringLiteral();
		returnTypeLiteral.setLiteralValue(JavaInstrumenterUtils.getReturnType(declaration));

		// Create a new method invocation with proper parameters
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(ast.newName(TraceLogger.TRACECLASS_FULLNAME));
		methodInvocation.setName(ast.newSimpleName(traceType));
		methodInvocation.arguments().add(packageNameLLiteral);
		methodInvocation.arguments().add(classNameExpression);
		methodInvocation.arguments().add(methodNameLiteral);
		methodInvocation.arguments().add(parameterTypesLiteral);
		methodInvocation.arguments().add(returnTypeLiteral);
		methodInvocation.arguments().add(argumentValuesExpression);

		return methodInvocation;
	}
}
