package absyn;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import semantical.TypeChecker;
import translation.Block;
import types.ClassMemberSignature;
import types.ClassType;

import types.IntType;
import types.TestSignature;
import types.TypeList;
import types.VoidType;
import bytecode.VIRTUALCALL;
import bytecode.RETURN;
import bytecode.NEWSTRING;
import bytecode.CONST;









public class TestDeclaration extends CodeDeclaration {

	private final String name;

	public TestDeclaration(int pos, String name, Command body, ClassMemberDeclaration next) {
		super(pos, null, body, next);

		this.name = name;
	}

	/**
	 * Yields the name of this test.
	 *
	 * @return the name of this test
	 */

	public String getName() {
		return name;
	}

	/**
	 * Yields the signature of this test declaration.
	 *
	 * @return the signature of this test declaration. Yields {@code null}
	 *         if type-checking has not been performed yet
	 */

	@Override
	public TestSignature getSignature() {
		return (TestSignature) super.getSignature();
	}

	@Override
	protected void toDotAux(FileWriter where) throws IOException {
		linkToNode("body", getBody().toDot(where), where);

	}

	/**
	 * Adds the signature of this test declaration to the given class.
	 *
	 * @param clazz the class where the signature of this test declaration must be added
	 */

	@Override
	protected void addTo(ClassType clazz) {
		TestSignature tSig = new TestSignature(clazz, name, this);

		clazz.addTest(tSig);

		// we record the signature of this test inside this abstract syntax
		setSignature(tSig);	
	}

	@Override
	protected void typeCheckAux(ClassType clazz) {
		TypeChecker checker = new TypeChecker(VoidType.INSTANCE, clazz.getErrorMsg(), true);

		// we type-check the body of the test in the resulting type-checking
		getBody().typeCheck(checker);

		// we check that there is no dead code in the body of the test
		getBody().checkForDeadcode();

		// tests return nothing, so that we do not check whether
		// a return statement is always present at the end of every
		// syntactical execution path in the body of a test
	}

	public void translate(Set<ClassMemberSignature> done) {
		if (done.add(getSignature())) {
			translate2(getSignature().getDefiningClass(),done);
			Block post = new Block(new RETURN(IntType.INSTANCE));
			post = new CONST(0).followedBy(post);
			post = new VIRTUALCALL(ClassType.mkFromFileName("String.kit"),
					ClassType.mkFromFileName("String.kit").methodLookup("output", TypeList.EMPTY))
			.followedBy(post);
			post = new NEWSTRING("Assert passed").followedBy(post);	
			getSignature().setCode(getBody().translate(post));
			translateReferenced(getSignature().getCode(), done, new HashSet<Block>());
		}
	}
	
	/*private void translateReferenced(Block block, Set<ClassMemberSignature> done, Set<Block> blocksDone) {
    	// if we already processed the block, we return immediately
    	if (!blocksDone.add(block))
    		return;
    	for (BytecodeList cursor = block.getBytecode(); cursor != null; cursor = cursor.getTail()) {
    		Bytecode h = cursor.getHead();
    		if (h instanceof GETFIELD) 
    			done.add(((GETFIELD) h).getField());
    		else if (h instanceof PUTFIELD) 
    			done.add(((PUTFIELD) h).getField());
    		else if (h instanceof CALL)
    			for (CodeSignature callee: ((CALL) h).getDynamicTargets())
    				callee.getAbstractSyntax().translate(done);
    	}
    	// we continue with the following blocks
    	for (Block follow: block.getFollows())
    		translateReferenced(follow, done, blocksDone);
    }*/
}
