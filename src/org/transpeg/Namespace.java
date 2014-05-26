package org.transpeg;

public class Namespace extends SymbolTable {

	public PegParser parser;
	public UniArray<String> exportSymbolList;

	public Namespace(PegParser parser) {
		super(null);
		this.namespace = this;
		this.parser = parser;
	}

	public void importFrom(Namespace ns) {
		for(int i = 0; i < ns.exportSymbolList.size(); i++) {
			String symbol = ns.exportSymbolList.ArrayValues[i];
			this.setSymbol(symbol, ns.getSymbol(symbol));
		}
	}


}
