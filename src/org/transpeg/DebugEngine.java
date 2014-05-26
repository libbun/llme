package org.transpeg;

public class DebugEngine extends MetaEngine {

	@Override
	public void initTable(Namespace gamma) {
		// define type information
//		gamma.setType("void",   new VoidType("void"));
//		gamma.setType("int",    new IntType("int32_t"));
//		gamma.setType("String", new StringType("string"));
//		gamma.addFunctor(new IntegerFunctor("c.int", gamma, "int"));
//		gamma.addFunctor(new DefineFunctor());
//		gamma.addFunctor(new ErrorFunctor());
//		gamma.load("lib/konoha.bun", this);
	}

	@Override
	public void pushName(String name, int nameIndex) {
		System.out.print(name+"_"+nameIndex);
	}

	@Override
	public void pushInteger(String number) {
		System.out.print(number);
	}

	@Override
	public void pushNewLine() {
		System.out.println();
	}

	@Override
	public void pushCode(String text) {
		System.out.print(text);
	}

	@Override
	public void pushNode(PegObject node) {
		if(node.matched != null) {
			node.matched.build(node, this);
		}
		else {
			SymbolTable gamma = node.getSymbolTable();
			Functor f = gamma.getFunctor(node);
			System.out.println("*unknown "+ node.name + "/" + f + "*");
		}
	}

	@Override
	public void pushTypeOf(PegObject node) {
		if(node.typed != null) {
			System.out.println(node.typed.getName());
		}
		else {
			System.out.println("*untyped "+ node.name + "*");
		}
	}

	@Override
	public void pushCommand(String name, PegObject node) {
		System.out.print("${" + name + " ");
		this.pushNode(node);
		System.out.print("}");
	}

	@Override
	public void pushErrorMessage(SourceToken source, String msg) {
		System.out.println(source.source.formatErrorLineMarker("error", source.startIndex, msg));
	}


}
