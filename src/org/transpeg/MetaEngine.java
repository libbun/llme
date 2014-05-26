package org.transpeg;

public abstract class MetaEngine {

	public abstract void initTable(Namespace gamma);

	public abstract void pushCode(String text);
	public abstract void pushNode(PegObject node);
	public abstract void pushTypeOf(PegObject node);
	public abstract void pushCommand(String name, PegObject node);

	public abstract void pushNewLine();
	public abstract void pushName(String name, int nameIndex);
	public abstract void pushInteger(String number);

	public abstract void pushErrorMessage(SourceToken source, String msg);

}
