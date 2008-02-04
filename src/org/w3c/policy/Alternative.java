/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.w3c.policy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Comment;

/**
 * Alternative abstract class implements few method which are common to
 * composite assertions. A composite assertion as some terms (if any) and
 * implicit logic that whether all (or any) of its terms should be statisfied.
 */
public abstract class Alternative implements IAssertion {

	private ArrayList<Comment> comments = new ArrayList<Comment>();

	protected boolean isNormalized = false;

	private IAssertion parent = null;

	private List<IAssertion> terms = new ArrayList<IAssertion>();

	public void addComment(Comment comment) {
		comments.add(comment);
	}
	
	/**
	 * Adds an assertion as one of its terms
	 * 
	 * @param assertion
	 *            the assertion that should be added as its term
	 */
	public void addTerm(IAssertion assertion) {
		assertion.setParent(this);
		terms.add(assertion);
	}

	public void addComments(ArrayList<Comment> commentList) {
		Iterator iterator = commentList.iterator();
		
		while (iterator.hasNext()) {
			Comment comment = (Comment) iterator.next();
			comments.add(comment);
		}
	}
	
	/**
	 * @return Returns the comments.
	 */
	public ArrayList<Comment> getComments() {
		return this.comments;
	}

	public IAssertion getParent() {
		return parent;
	}

	public List<IAssertion> getTerms() {
		return terms;
	}

	public IAssertion intersect(IAssertion assertion)
			throws UnsupportedOperationException {
		return intersect(assertion, null);
	}

	/**
	 * Returns true if no terms exist or false otherwise
	 * 
	 * @return true if no terms exist or false otherwise
	 */
	public boolean isEmpty() {
		return terms.size() == 0;
	}

	public boolean isNormalized() {
		return isNormalized;
	}

	public IAssertion merge(IAssertion assertion)
			throws UnsupportedOperationException {
		return merge(assertion, null);
	}

	public IAssertion normalize() {
		return normalize(null);
	}

	public boolean remove(IAssertion assertion) {
		return terms.remove(assertion);
	}

	/**
	 * @param commentList
	 *            The comments to set.
	 */
	public void setComments(ArrayList<Comment> commentList) {
		this.comments = commentList;
	}

	public void setNormalized(boolean value) {
		Iterator children = getTerms().iterator();

		while (children.hasNext()) {
			Object child = children.next();
			if (child instanceof Alternative) {
				((Alternative) child).setNormalized(value);
			}
		}
		isNormalized = value;
	}

	public void setParent(IAssertion parent) {
		this.parent = parent;
	}

	/**
	 * Adds set of assertions as its terms
	 * 
	 * @param assertions
	 *            the set of assertions that should be added as its terms
	 */
	public void addTerms(List assertions) {
		if (assertions.isEmpty()) {
			return;
		}

		Iterator items = assertions.iterator();
		while (items.hasNext()) {
			Object value = items.next();

			if (!(value instanceof IAssertion)) {
				throw new IllegalArgumentException("argument contains a "
						+ "non-assertion");
			}
			addTerm((IAssertion) value);
		}
	}

	public int size() {
		return terms.size();
	}

}
