package absyn;

import java.io.FileWriter;
import bytecode.NEWSTRING;
import bytecode.VIRTUALCALL;
import bytecode.CONST;
import bytecode.RETURN;
import semantical.TypeChecker;
import translation.Block;
import types.ClassType;
import types.IntType;
import types.TypeList;




public class Assert extends Command {

	private final Expression asserted;
	
	public Assert(int pos, Expression asserted) {
		super(pos);
		
		this.asserted = asserted;
	}
	
	public Expression getAsserted() {
		return asserted;
	}
	
	@Override
	protected void toDotAux(FileWriter where) throws java.io.IOException {
			linkToNode("asserted", asserted.toDot(where), where);
	}

	@Override
	protected TypeChecker typeCheckAux(TypeChecker checker) {
		asserted.mustBeBoolean(checker);
		boolean expectedInTest = checker.isInTest();
		
		if (expectedInTest != true)
			error("assert not defined in method test");
		
		return checker;
	}

	@Override
	public boolean checkForDeadcode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Block translate(Block continuation) {
		String out = makeFailureMessage();
		
		Block failed = new Block(new RETURN(IntType.INSTANCE));
		failed = new CONST(-1).followedBy(failed);
		failed = new VIRTUALCALL(ClassType.mkFromFileName("String.kit"),
			ClassType.mkFromFileName("String.kit").methodLookup("output", TypeList.EMPTY)).followedBy(failed);
		failed = new NEWSTRING(out).followedBy(failed);

		return asserted.translateAsTest(continuation, failed);
	}
	
	private String makeFailureMessage() {
		String pos = getTypeChecker().calcPos(getPos());

		return "Assert failed at: " + pos;
	}
}
