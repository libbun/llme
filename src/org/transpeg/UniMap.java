// ***************************************************************************
// Copyright (c) 2013-2014, Libbun project authors. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// *  Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
// *  Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// **************************************************************************

package org.transpeg;

import java.util.HashMap;

public final class UniMap <T> {
	final HashMap<String, T>	Map;

	public UniMap(MetaType ElementType) {
		this.Map = new HashMap<String, T>();
	}

	public UniMap(int TypeId, T[] Literal) {
		this.Map = new HashMap<String, T>();
		int i = 0;
		while(i < Literal.length) {
			this.Map.put(Literal[i].toString(), Literal[i+1]);
			i = i + 2;
		}
	}

	@Override public String toString() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("{");
		int i = 0;
		for(String Key : this.Map.keySet()) {
			if(i > 0) {
				sBuilder.append(", ");
			}
			sBuilder.append(this.Stringify(Key));
			sBuilder.append(" : ");
			sBuilder.append(this.Stringify(this.Map.get(Key)));
			i++;
		}
		sBuilder.append("}");
		return sBuilder.toString();
	}

	protected String Stringify(Object Value) {
		if(Value instanceof String) {
			return UniCharset._QuoteString((String) Value);
		}
		return Value.toString();
	}

	public final void put(String Key, T Value) {
		this.Map.put(Key, Value);
	}

	public final T GetOrNull(String Key) {
		return this.Map.get(Key);
	}

	public final T GetValue(String Key, T DefaultValue) {
		T Value = this.Map.get(Key);
		if(Value == null) {
			return DefaultValue;
		}
		return Value;
	}

	public final void remove(String Key) {
		this.Map.remove(Key);
	}

	public void AddMap(UniMap<Object> aMap) {
		throw new RuntimeException("unimplemented !!");
	}

	public final boolean HasKey(String Key) {
		return this.Map.containsKey(Key);
	}

	public final UniArray<String> keys() {
		UniArray<String> a = new UniArray<String>(new String[this.Map.size()]);
		for(String k : this.Map.keySet()) {
			a.add(k);
		}
		return a;
	}
//
//	public final static <T> T GetIndex(CommonMap<T> aMap, String Key) {
//		T Value =  aMap.Map.get(Key);
//		if(Value == null) {
//			throw new SoftwareFault("Key not found: " + Key);
//		}
//		return Value;
//	}

	public final static <T> void SetIndex(UniMap<T> aMap, String Key, T Value) {
		aMap.Map.put(Key, Value);
	}
}
