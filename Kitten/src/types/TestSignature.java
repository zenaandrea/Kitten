package types;

import javaBytecodeGenerator.JavaClassGenerator;
import javaBytecodeGenerator.TestClassGenerator;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.MethodGen;
import translation.Block;
import absyn.TestDeclaration;

public class TestSignature extends CodeSignature {

	public TestSignature(ClassType clazz, String name, TestDeclaration abstractSyntax) {
		super(clazz, VoidType.INSTANCE, TypeList.EMPTY, name, abstractSyntax);
	}

	@Override
	protected Block addPrefixToCode(Block code) {
		// TODO Auto-generated method stub
		return code;
	}
	
	@Override
    public boolean equals(Object other) {
    		if(getClass() == other.getClass()){
    			TestSignature otherT = (TestSignature) other;
    			return getName() == otherT.getName();
    		}
    		else
    			return false;
    }

	@Override
    public String toString() {
    	return getDefiningClass() + "."
   			+ getName() + "=test";
    }
	
	public void createTest(TestClassGenerator classGen) {
		MethodGen methodGen;
		methodGen = new MethodGen
		(Constants.ACC_PRIVATE | Constants.ACC_STATIC, // private and static
		org.apache.bcel.generic.Type.INT, // return type
		new org.apache.bcel.generic.Type[]{getDefiningClass().toBCEL()},
		null, // parameters names: we do not care
		getName(), // method's name
		classGen.getClassName(), // defining class
		classGen.generateJavaBytecode(getCode()), // bytecode of the method
		classGen.getConstantPool()
		); // constant pool
    
		// we must always call these methods before the getMethod()
		// method below. They set the number of local variables and stack
		// elements used by the code of the method
		methodGen.setMaxStack();
		methodGen.setMaxLocals();
		// we add a method to the class that we are generating
		classGen.addMethod(methodGen.getMethod());
    }
    
	public INVOKESTATIC createINVOKESTATIC(JavaClassGenerator classGen) {
		return (INVOKESTATIC) createInvokeInstruction(classGen, Constants.INVOKESTATIC);
	}
}
