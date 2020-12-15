package at.searles.commons.util;

import java.util.TreeMap;

/**
 * This class represents BiMaps.
 * @param <A>
 * @param <B>
 */
public class Table<A extends Comparable<A>, B extends Comparable<B>> {
	// this is a map in both directions
	private TreeMap<A, B> lr = new TreeMap<>();
	private TreeMap<B, A> rl = new TreeMap<>();

	public void add(A a, B b) {
		if(lr.put(a, b) != null || rl.put(b, a) != null) throw new IllegalArgumentException();
	}

	public boolean containsL(A a) {
		return lr.containsKey(a);
	}

	public boolean containsR(B b) {
		return rl.containsKey(b);
	}

	public B r(A a) {
		return lr.get(a);
	}

	public A l(B b) {
		return rl.get(b);
	}

	public String toString() {
        return lr.toString();
    }
}
