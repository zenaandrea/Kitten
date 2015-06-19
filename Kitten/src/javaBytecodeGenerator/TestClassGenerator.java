package javaBytecodeGenerator;

import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LDC;

import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import types.ClassMemberSignature;
import types.ClassType;
import types.FixtureSignature;
import types.TestSignature;

/**
 * A Java bytecode generator. It transforms the Kitten intermediate language
 * into Java bytecode that can be dumped to Java class files and run.
 * It uses the BCEL library to represent Java classes and dump them on the file-system.
 *
 * @author <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */


@SuppressWarnings("serial")
public class TestClassGenerator extends JavaClassGenerator {

	ClassType clazz;
	
	public TestClassGenerator(ClassType clazz, Set<ClassMemberSignature> sigs) {
		super(clazz.getName() + "Test", // name of the class
				// the superclass of the Kitten Object class is set to be the Java java.lang.Object class
				clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "java.lang.Object",
				clazz.getName() + ".kit" // source file
				);

		this.clazz = clazz;	
		// we add the tests
		for (FixtureSignature fix: clazz.getFixtures())
			fix.createFixture(this);
		// we add the fixtures
		for (TestSignature test: clazz.getTests())
			test.createTest(this);
		
		this.createMain();
	}
	
	public void createMain() {
		InstructionList iList = new InstructionList();
		iList.append(getFactory().createPrintln("Test execution for class " + this.clazz.getName()));
		
		// 
		iList.append(InstructionFactory.ICONST_0);
		iList.append(InstructionFactory.ISTORE_1);
		
		for (TestSignature test: clazz.getTests()) {
			InstructionList ilTest;
			
			// create the test
			ilTest = pgTest(test, clazz.getFixtures());
			
			// store the result of the test (0: passed, -1:failed)
			ilTest.append(InstructionFactory.ILOAD_1);
			ilTest.append(InstructionFactory.IADD);
			ilTest.append(InstructionFactory.ISTORE_1);

			// print the time
			pgTime(ilTest);
			
			iList.append(ilTest);
		}
		
		// -- print "n tests passed, m failed [xyz ms]"
		
		// # tests passed
		iList.append(getFactory().createGetStatic("java/lang/System", "out", 
				Type.getType(java.io.PrintStream.class)));
		iList.append(InstructionFactory.ILOAD_1);
		iList.append(new LDC(getConstantPool().addInteger(clazz.getTests().size())));
		iList.append(InstructionFactory.IADD);
		iList.append(getFactory().createInvoke(
				"java/io/PrintStream", 
				"print", 
				Type.VOID, 
				new org.apache.bcel.generic.Type[]{org.apache.bcel.generic.Type.INT},
				org.apache.bcel.Constants.INVOKEVIRTUAL
		));
		
		iList.append(pgPrint(" test(s) passed, "));
		
		// # tests failed
		iList.append(getFactory().createGetStatic("java/lang/System", "out", 
				Type.getType(java.io.PrintStream.class)));
		iList.append(InstructionFactory.ILOAD_1);
		iList.append(InstructionFactory.INEG);
		iList.append(getFactory().createInvoke(
				"java/io/PrintStream", 
				"print", 
				Type.VOID, 
				new org.apache.bcel.generic.Type[]{org.apache.bcel.generic.Type.INT},
				org.apache.bcel.Constants.INVOKEVIRTUAL
		));
		
		iList.append(pgPrint(" failed "));
		
		pgTime(iList);

		iList.append(InstructionFactory.createReturn(Type.VOID));	
		
		
		MethodGen methodGen;
			methodGen = new MethodGen
				(Constants.ACC_PUBLIC | Constants.ACC_STATIC, // public and static
				org.apache.bcel.generic.Type.VOID, // return type
				new org.apache.bcel.generic.Type[] // parameters
					{ new org.apache.bcel.generic.ArrayType("java.lang.String", 1) },
				null, // parameters names: we do not care
				"main", // method's name
				this.getClassName(), // defining class
				iList,
				this.getConstantPool()); // constant pool
			
			
			
		// we must always call these methods before the getMethod()
		// method below. They set the number of local variables and stack
		// elements used by the code of the method
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// we add a method to the class that we are generating
		this.addMethod(methodGen.getMethod());
	}

	private InvokeInstruction currentMillis() {
		return getFactory().createInvoke(
				"java/lang/System", 
				"currentTimeMillis", 
				org.apache.bcel.generic.Type.LONG, 
				new org.apache.bcel.generic.Type[]{},
				org.apache.bcel.Constants.INVOKESTATIC
		);
	}
	
	private void pgTime(InstructionList il) {
		InstructionList prima = new InstructionList();
		prima.append(getFactory().createGetStatic("java/lang/System", "out", Type.getType(java.io.PrintStream.class)));
		prima.append(currentMillis());
		
		InstructionList dopo = new InstructionList();
		dopo.append(currentMillis());
		dopo.append(InstructionConstants.LSUB);
		dopo.append(InstructionConstants.LNEG);
		dopo.append(InstructionConstants.L2I);
		dopo.append(pgPrint(" ["));
		dopo.append(getFactory().createInvoke(
				"java/io/PrintStream", 
				"print", 
				Type.VOID, 
				new org.apache.bcel.generic.Type[]{org.apache.bcel.generic.Type.INT},
				org.apache.bcel.Constants.INVOKEVIRTUAL
		));
		dopo.append(pgPrint("ms]\n"));
		
		il.insert(prima);
		il.append(dopo);

	}

	private InstructionList pgPrint(String msg) {
		InstructionList il = new InstructionList();
		il.append(getFactory().createGetStatic("java/lang/System", "out", 
				Type.getType(java.io.PrintStream.class)));
		il.append(new LDC(getConstantPool().addString(msg)));
		il.append(getFactory().createInvoke(
				"java/io/PrintStream", 
				"print", 
				Type.VOID, 
				new org.apache.bcel.generic.Type[]{org.apache.bcel.generic.Type.STRING},
				org.apache.bcel.Constants.INVOKEVIRTUAL
		));
		
		return il;
	}
	
	private InstructionList pgTest(TestSignature test, Set<FixtureSignature> fixtures) {
		InstructionList il = new InstructionList();
		il.append(pgPrint("\t- Test: " + test.getName() + " "));
		
		// Creo un nuovo oggetto
		il.append(getFactory().createNew(this.clazz.getName()));
		// stack:	obj
		
		// dup
		il.append(InstructionFactory.DUP);
		// stack:	obj
		//			obj

		// chiamo il costruttore
		il.append(getFactory().createInvoke(
				this.clazz.getName(), 
				"<init>", 
				org.apache.bcel.generic.Type.VOID, 
				new org.apache.bcel.generic.Type[]{},
				org.apache.bcel.Constants.INVOKESPECIAL
		));
		// stack:	obj
		
		for (FixtureSignature fixture: clazz.getFixtures()) {		
			// dup
			il.append(InstructionFactory.DUP);
			// stack:	obj
			//			obj
			il.append(getFactory().createInvoke(
					this.clazz.getName() + "Test", 
					fixture.getName(), 
					org.apache.bcel.generic.Type.VOID, 
					new org.apache.bcel.generic.Type[]{clazz.toBCEL()},
					org.apache.bcel.Constants.INVOKESTATIC
			));
		}
		
		// chiamo il test
		il.append(getFactory().createInvoke(
				this.clazz.getName() + "Test", 
				test.getName(), 
				org.apache.bcel.generic.Type.INT, 
				new org.apache.bcel.generic.Type[]{clazz.toBCEL()},
				org.apache.bcel.Constants.INVOKESTATIC
		));
		
		return il;
	}
}