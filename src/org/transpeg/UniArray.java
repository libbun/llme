package org.transpeg;

import java.lang.reflect.Array;

public class UniArray<T> {
	private int    currentSize;
	public T[] ArrayValues;

	public UniArray(T[] Values) {
		this.ArrayValues = Values;
		this.currentSize = 0;
	}

	@Override public String toString() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("[");
		for(int i = 0; i < this.size(); i++) {
			if(i > 0) {
				sBuilder.append(", ");
			}
			sBuilder.append(this.Stringify(this.ArrayValues[i]));
		}
		sBuilder.append("]");
		return sBuilder.toString();
	}

	protected String Stringify(Object Value) {
		if(Value instanceof String) {
			return UniCharset._QuoteString((String) Value);
		}
		return Value.toString();
	}

	public final int size() {
		return this.currentSize;
	}

	private T[] newArray(int CopySize, int NewSize) {
		@SuppressWarnings("unchecked")
		T[] newValues = (T[])Array.newInstance(this.ArrayValues.getClass().getComponentType(), NewSize);
		System.arraycopy(this.ArrayValues, 0, newValues, 0, CopySize);
		return newValues;
	}

	private void reserve(int newsize) {
		int currentCapacity = this.ArrayValues.length;
		if(newsize < currentCapacity) {
			return;
		}
		int newCapacity = currentCapacity * 2;
		if(newCapacity < newsize) {
			newCapacity = newsize;
		}
		this.ArrayValues = this.newArray(this.currentSize, newCapacity);
	}

	public final void add(T Value) {
		this.reserve(this.currentSize + 1);
		this.ArrayValues[this.currentSize] = Value;
		this.currentSize = this.currentSize + 1;
	}

	public final void add(int index, T Value) {
		this.reserve(this.currentSize + 1);
		System.arraycopy(this.ArrayValues, index, this.ArrayValues, index+1, this.currentSize - index);
		this.ArrayValues[index] = Value;
		this.currentSize = this.currentSize + 1;
	}

	public final void remove(int index) {
		if(this.currentSize > 1) {
			System.arraycopy(this.ArrayValues, index+1, this.ArrayValues, index, this.currentSize - 1);
		}
		this.currentSize = this.currentSize - 1;
	}

	public final void clear(int index) {
		this.currentSize = index;
	}

	public final T[] compactArray() {
		if(this.currentSize == this.ArrayValues.length) {
			return this.ArrayValues;
		}
		else {
			@SuppressWarnings("unchecked")
			T[] newValues = (T[])Array.newInstance(this.ArrayValues.getClass().getComponentType(), this.currentSize);
			System.arraycopy(this.ArrayValues, 0, newValues, 0, this.currentSize);
			return newValues;
		}
	}
	//
	//	public static void ThrowOutOfArrayIndex(int Size, long Index) {
	//		throw new SoftwareFault("out of array index " + Index + " < " + Size);
	//	}

}
