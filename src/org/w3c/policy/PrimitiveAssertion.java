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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Comment;
import org.w3c.policy.util.PolicyConstants;
import org.w3c.policy.util.PolicyRegistry;

/**
 * PrimitiveAssertion wraps an assertion which is indivisible. Such assertion
 * require domain specific knowledge for further processing. Hence this class
 * seperates that domain specific knowledge from generic framework.
 * 
 */
public class PrimitiveAssertion implements IAssertion {

	private Hashtable<QName, String> attributes = new 
	                                         Hashtable<QName, String>();

	private ArrayList<Comment> comments = new ArrayList<Comment>();

	private boolean isNormalized = false;

	private boolean isOptional = false;
	
	private boolean isIgnorable = false;
	
	private IAssertion parent = null;

	private QName qName;

	/* Stores the values of my direct text node children */
	private ArrayList<String> strValues = new ArrayList<String>();

	/* Stroes the my children */
	private List<IAssertion> terms = new ArrayList<IAssertion>();

	public PrimitiveAssertion(QName qName) {
		this.qName = qName;
	}

	public void addComment(Comment comment) {
		comments.add(comment);
	}

	public void addStrValue(String value) {
		strValues.add(value);
	}

	/**
	 *  Add a child assertion to me 
	 */  
	public void addTerm(IAssertion term) {
		term.setParent(this);
		terms.add(term);
	}

	/**
	 *  Retrieve an attribute by its QName from my attributes table
	 */
	public String getAttribute(QName qname) {
		return attributes.get(qname);
	}

	/** 
	 * Return all my attributes
	 */ 
	public Hashtable<QName, String> getAttributes() {
		return attributes;
	}

	/**
	 * @return Returns the strComment.
	 */
	public ArrayList<Comment> getComments() {
		return this.comments;
	}

	/**
	 * 
	 * @see org.w3c.policy.IAssertion#getParent()
	 */
	public IAssertion getParent() {
		return parent;
	}

	/** Retrun my QName */
	public QName getQName() {
		return qName;
	}

	/**
	 * Return only myself with my attributes and text values but without my
	 * children assertions
	 */
	private PrimitiveAssertion getSelfWithoutTerms() {
		PrimitiveAssertion self = new PrimitiveAssertion(getQName());
		self.setAttributes(getAttributes());
		self.setStrValues(getStrValues());
		self.setComments(getComments());
		return self;
	}

	public ArrayList<String> getStrValues() {
		return strValues;
	}

	/* Return all my children */
	public List<IAssertion> getTerms() {
		return terms;
	}

	/**
	 * @see org.w3c.policy.IAssertion#intersect(org.w3c.policy.IAssertion)
	 */
	public IAssertion intersect(IAssertion assertion)
			throws UnsupportedOperationException {
		return intersect(assertion, null);
	}

	/**
	 * @see org.w3c.policy.IAssertion#intersect(org.w3c.policy.IAssertion,
	 *      org.w3c.policy.util.PolicyRegistry)
	 */
	public IAssertion intersect(IAssertion assertion, PolicyRegistry reg)
			throws UnsupportedOperationException {

		IAssertion normalizedMe = normalize(reg);
		IAssertion parameter = assertion.normalize(reg);

		/* Am not a primitive assertion anymore .. */
		if (!(assertion instanceof PrimitiveAssertion)) {
			return normalizedMe.intersect(assertion, reg);
		}

		/* Parameter is not primitive assertion */
		if (!(parameter instanceof PrimitiveAssertion)) {
			return parameter.intersect(normalizedMe, reg);
		}

		PrimitiveAssertion target = (PrimitiveAssertion) parameter;
		PrimitiveAssertion self = (PrimitiveAssertion) normalizedMe;

		if (!self.getQName().equals(target.getQName())) {
			/* No bahaviour is admissible */
			return new ExactlyOneAlternative();
		}

		/* Our QNames match with each other */
		AllAlternative andTerm = new AllAlternative();
		andTerm.addTerm(self);
		andTerm.addTerm(target);
		return andTerm;
	}

	/**
	 * @see org.w3c.policy.IAssertion#isNormalized()
	 */
	public boolean isNormalized() {
		return isNormalized;
	}

	public boolean isOptional() {
		return isOptional;
	}

	public boolean isIgnorable() {
		return isIgnorable;
	}
	
	/**
	 * @see org.w3c.policy.IAssertion#merge(org.w3c.policy.IAssertion)
	 */
	public IAssertion merge(IAssertion assertion)
			throws UnsupportedOperationException {
		return merge(assertion, null);
	}

	/**
	 * @see org.w3c.policy.IAssertion#merge(org.w3c.policy.IAssertion,
	 *      org.w3c.policy.util.PolicyRegistry)
	 */
	public IAssertion merge(IAssertion assertion, PolicyRegistry reg)
			throws UnsupportedOperationException {

		IAssertion normalizedMe = normalize(reg);
		IAssertion target = assertion.normalize(reg);

		if (!(normalizedMe instanceof PrimitiveAssertion)) {
			return normalizedMe.merge(assertion, reg);
		}

		if (!(target instanceof PrimitiveAssertion)) {
			return target.intersect(normalizedMe, reg);
		}

		AllAlternative andTerm = new AllAlternative();
		andTerm.addTerm(target);
		andTerm.addTerm(normalizedMe);
		return andTerm;
	}

	/**
	 * @see org.w3c.policy.IAssertion#normalize()
	 */
	public IAssertion normalize() throws UnsupportedOperationException {
		return normalize(null);
	}

	/**
	 * @see org.w3c.policy.IAssertion#normalize(org.w3c.policy.util.PolicyRegistry)
	 */
	public IAssertion normalize(PolicyRegistry reg)
			throws UnsupportedOperationException {

		if (isNormalized()) {
			return this;
		}

		/* If I am optional */
		if (isOptional()) {
			ExactlyOneAlternative XOR = new ExactlyOneAlternative();
			AllAlternative AND = new AllAlternative();

			/*
			 * Get only myself with my attributes and text values but without my
			 * children assertions
			 */
			PrimitiveAssertion PRIM = getSelfWithoutTerms();

			/* Remove attribute "Optional" */
			PRIM.removeAttribute(new QName(
					PolicyConstants.WS_POLICY_NAMESPACE_URI, "Optional"));

			PRIM.setOptional(false);

			/* Add my children */
			PRIM.setTerms(getTerms());

			/* Add me to an All Alternative. */
			AND.addTerm(PRIM);
			/* Add this All Alternative to a ExactlyOne Alternative */
			XOR.addTerm(AND);
			/*
			 * Add a empty All Alternative to the resulting ExactlyOne
			 * Alternative
			 */
			XOR.addTerm(new AllAlternative());

			return XOR.normalize(reg);
		}

		/* If I am not optional */
		this.removeAttribute(new QName(PolicyConstants.WS_POLICY_NAMESPACE_URI,
				"Optional"));

		return this;
	}

	public void removeAttribute(QName qname) {
		/*
		 * Remove the attribut "wsp:Optional" from the attributes. If the
		 * attribut does not exist in the table, this method will do nothing
		 */
		attributes.remove(qname);
	}

	public void setAttributes(Hashtable<QName, String> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @param comment
	 *            The strComment to set.
	 */
	public void setComments(ArrayList<Comment> comment) {
		this.comments = comment;
	}

	/**
	 * @see org.w3c.policy.IAssertion#setNormalized(boolean)
	 */
	public void setNormalized(boolean flag) {
		Iterator iterator = getTerms().iterator();
		while (iterator.hasNext()) {
			IAssertion assertion = (IAssertion) iterator.next();
			assertion.setNormalized(flag);
		}
		this.isNormalized = flag;
	}

	public void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}
	
	public void setIgnorable(boolean isIgnorable) {
		this.isIgnorable = isIgnorable;
	}

	/**
	 * @see org.w3c.policy.IAssertion#setParent(org.w3c.policy.IAssertion)
	 */
	public void setParent(IAssertion parent) {
		this.parent = parent;

	}

	public void setStrValues(ArrayList<String> valueList) {
		this.strValues = valueList;
	}

	public void setTerms(List<IAssertion> terms) {
		this.terms = terms;
	}

}
