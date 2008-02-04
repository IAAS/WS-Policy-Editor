package org.w3c.policy.util;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * Vocabulary represents the vocabulary of a Policy Alternative in WS-Policy
 * normal form. It models the mathematical set abstraction and contains no
 * duplicate elements.
 * 
 * @author      Zhilei Ma
 * @version     1.0
 */
public class Vocabulary extends TreeSet<String> {

	private static final long serialVersionUID = 8827810226076339730L;

	public Vocabulary() {
		super();
	}

	/**
	 *  Compare the vocabularies of tow All alternatives. 
	 */
	public boolean compare(Vocabulary vocabulary) {

		if (size() == 0 && vocabulary.size() ==0) {
			return true;
		}
		
		if (size() != vocabulary.size()) {
			return false;
		}
		
		Iterator iteratorA = iterator();
		Iterator iteratorB = vocabulary.iterator();

		while (iteratorA.hasNext()) {
			String qNameA = (String) iteratorA.next();
			String qNameB = (String)iteratorB.next();
			if (!qNameA.equals(qNameB)) {
				return false;
			}
		}

		/* Otherwise report a matching */
		return true;
	}
}
