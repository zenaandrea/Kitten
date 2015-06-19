package absyn;

import java.io.FileWriter;
import java.io.IOException;

import semantical.TypeChecker;
import types.ClassType;
import types.FixtureSignature;
import types.VoidType;

public class FixtureDeclaration extends CodeDeclaration {

	private static int counter;
	private String name;
	
	public FixtureDeclaration(int pos, Command body, ClassMemberDeclaration next) {
		super(pos, null, body, next);
		name = "fixture".concat(String.valueOf(++counter));
	}
	
	/**
	 * Yields the signature of this fixture declaration.
	 *
	 * @return the signature of this fixture declaration.
	 *         Yields {@code null} if type-checking has not been performed yet
	 */

	/*@Override
	public FixtureSignature getSignature() {
		return (FixtureSignature) super.getSignature();
	}*/

	@Override
	protected void toDotAux(FileWriter where) throws IOException {
		linkToNode("body", getBody().toDot(where), where);
		
	}

	/**
	 * Adds the signature of this fixture declaration to the given class.
	 *
	 * @param clazz the class where the signature of this fixture
	 *              declaration must be added
	 */
	
	@Override
	protected void addTo(ClassType clazz) {
		FixtureSignature fSig = new FixtureSignature (clazz, name, this);
		
		clazz.addFixture(fSig);
		
		// we record the signature of this fixture inside this abstract syntax
		setSignature(fSig);
	}
	
	@Override
	protected void typeCheckAux(ClassType clazz) {
		TypeChecker checker = new TypeChecker(VoidType.INSTANCE, clazz.getErrorMsg());
		checker = checker.putVar("this", clazz);
		
		// we type-check the body of the constructor in the resulting type-checker
		getBody().typeCheck(checker);

		// we check that there is no dead-code in the body of the constructor
		getBody().checkForDeadcode();
		
		// fixtures return nothing, so that we do not check whether
		// a return statement is always present at the end of every
		// syntactical execution path in the body of a fixture
	}
}