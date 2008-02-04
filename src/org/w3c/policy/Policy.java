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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.policy.util.PolicyConstants;
import org.w3c.policy.util.PolicyRegistry;

/**
 * Policy is the access point for policy framework. It the object model that
 * represents a policy at runtime.
 * 
 */
public class Policy extends AllAlternative implements IAssertion {
	private Hashtable<QName, String> attributes = new Hashtable<QName, 
	         String>();

	private Element element = null;

	/* id represents the relative URI of the policy */
	private String id = null;

	/* xmlBase specifies the base URI of the policy */
	private String xmlBase = null;

	public Policy() {
		setNormalized(false);
	}

	public Policy(Element element) {
		this.element = element;
		setNormalized(false);
		setPolicyURI();
	}

	/**
	 * @return Returns the attributes.
	 */
	public Hashtable<QName, String> getAttributes() {
		return attributes;
	}

	public String getBase() {
		return xmlBase;
	}

	/**
	 * @return Returns the element.
	 */
	public Element getElement() {
		return element;
	}

	public String getId() {
		return id;
	}

	/**
	 *  Retrieve the absolute URI from the base URI and relative
	 *  URI 
	 */
	public String getPolicyURI() {
		  if (id != null) {
                if (xmlBase != null) {
                        return xmlBase + "#" + id;
                }
                return "#" + id;
        }
        return null;
	}

	@Override
	/**
	 *  PolicyRegistry can be used later to retrieve a policy from a 
	 *  repository 
	 */
	public IAssertion intersect(IAssertion assertion, PolicyRegistry aRegistry) {
		Policy result = new Policy(element);
		
		result.setId(null);
		
		result.setBase(null);

		result.addComments(getComments());

		if (assertion instanceof Policy) {
			result.addComments(((Policy) assertion).getComments());
		}
		
		/* If myself is not normalized, then normalize me. */
		Policy normalizedMe = (Policy) ((isNormalized()) ? this
				: normalize(aRegistry));

		/* Get the only ExactlyOne alternative of the normalized policy */
		ExactlyOneAlternative xorTermA = (ExactlyOneAlternative) 
		        normalizedMe.getTerms().get(0);

		/* If parameter assertion is a primitive assertion */
		if (assertion instanceof PrimitiveAssertion) {
			/*
			 * Add the intersection of ExactlyOne alternative with this
			 * assertion to the result
			 */
			result.addTerm(xorTermA.intersect(assertion, aRegistry));
			return result;
		}

		/* Otherwise is the parameter assertion an alternative */
		Alternative target = (Alternative) assertion;

		/* If the target is not normalized, then normalize it. */
		target = (Alternative) ((target.isNormalized()) ? target : target
				.normalize(aRegistry));

		/* If target is a policy */
		if (target instanceof Policy) {
			/* Get the only ExactlyOne alternative of this normalized policy */
			ExactlyOneAlternative xorTermB = (ExactlyOneAlternative) 
			        target.getTerms().get(0);
			/*
			 * Add the intersection of these two ExactlyOne alternatives to the
			 * result
			 */
			result.addTerm(xorTermA.intersect(xorTermB));
			return result;
		}

		/* If target is a XORAlternative or an AndAlternative */
		result.addTerm(xorTermA.intersect(target));
		return result;
	}

	@Override
	public IAssertion merge(IAssertion assertion, PolicyRegistry aRegistry) {
		Policy result = new Policy(element);
		
		result.setId(null);
		
		result.setBase(null);

		result.addComments(getComments());

		if (assertion instanceof Policy) {
			result.addComments(((Policy) assertion).getComments());
		}

		/* If myself is not normalized, then normalize me. */
		Policy normalizedMe = (Policy) ((isNormalized()) ? this
				: normalize(aRegistry));

		/* Get the only ExactlyOne alternative of the normalized policy */
		ExactlyOneAlternative xorTerm = (ExactlyOneAlternative) 
		    normalizedMe.getTerms().get(0);

		IAssertion term = xorTerm.merge(assertion, aRegistry);

		result.addTerm(term);
		result.setNormalized(true);
		return result;
	}

	@Override
	public IAssertion normalize() {
		return normalize(null);
	}

	@Override
	public IAssertion normalize(PolicyRegistry aRegistry) {
		if (isNormalized()) {
			return this;
		}

		// Copy root data to newly generated policy (="target policy")
		Policy policy = new Policy(element);
		policy.addComments(getComments());		
		policy.setId(null);		
		policy.setBase(null);

		// collects all AND alternatives
		AllAlternative AND = new AllAlternative();
		
		// collects all XOR alternatives
		ArrayList<ExactlyOneAlternative> xorTerms = new ArrayList <ExactlyOneAlternative>();

		// the terms of the policy to normalize
		Iterator terms = getTerms().iterator();

		/* --- Processing direct children elements of policy --- */
		while (terms.hasNext()) {
			IAssertion term = (IAssertion) terms.next();

			term = (term instanceof Policy) ? term : term.normalize(aRegistry);
			// TODO: check whether term.normalize may return a policy - Otherwise this if should be merged with the subsequent if
			
			// term is normlized, except the term is a policy 

			// wsp:Policy is equivalent to wsp:All
			if (term instanceof Policy) {
				// convert policy to all operator
				AllAlternative wrapper = new AllAlternative();
				Policy policyTerm = (Policy) term;
				wrapper.addComments(policyTerm.getComments());
				wrapper.addTerms(policyTerm.getTerms());

				// since policy was not normalized, it has to be normalized here
				term = wrapper.normalize(aRegistry);
			}			
			// term is normalized here (in all cases)

			if (term instanceof ExactlyOneAlternative) {
				/* Empty ExactlyOne alternative means no behavior is 
				 * admissible 
				 */
				if (((ExactlyOneAlternative) term).isEmpty()) {
					// Create null policy 
					// TODO: Does this work if it's not the first term that matches here?
					ExactlyOneAlternative xorTerm = new ExactlyOneAlternative();
					xorTerm.setNormalized(true);
					policy.addTerm(xorTerm);
					policy.setNormalized(true);
					return policy;
				} else {
					xorTerms.add((ExactlyOneAlternative) term);
				}
			} else if (term instanceof AllAlternative) {
				if (((AllAlternative) term).isEmpty()) {
					/*
					 * If no children elements of AndAlternative exits, an empty
					 * AndAlternative will be added to the XOR later
					 */
				} else {
					/*
					 * Otherwise add the children of AndAlternative to ADD.
					 */
					AND.addTerms(((AllAlternative) term).getTerms());
				}
			} else {
				/*
				 * Otherwise add primitive assertions that are direct children of
				 * Policy to AND.
				 */
				AND.addTerm(term);
			}
		}
		
		// xorTerms contains all XOR-Alternatives
		// AND contains all AND-Alternatives
		
		// now, they have to be brought into normal form

		if (xorTerms.isEmpty()) {
			// We're in luck. We do not have to merge the ANDs into one XOR 
			// alternative (because there do not exist any XOR alternatives)
			// Create new XOR alternative and put ANDs below.
			ExactlyOneAlternative xor = new ExactlyOneAlternative();
			xor.addTerm(AND);
			policy.addTerm(xor);
			return policy;
		}
		
		// xorTerms is NOT empty. -> lengthy handling...

		// XOR is the final single "root" operator in the target policy 
		ExactlyOneAlternative XOR = new ExactlyOneAlternative();
		
		// first of all, only one XOR has to exist
		// Distributive Law
		ArrayList<AllAlternative> childAndTerms = distributeXORAlternatives(xorTerms);
		for (int i = 0; i < childAndTerms.size(); i++) {
			XOR.addTerm(childAndTerms.get(i));
		}
		
		// distribute AND over XOR
		// WS-Policy-Framework 1.5 - 2007-09-04 - Page 22 "4.3 Compact Policy Expression"
		List primTerms = AND.getTerms();
		Iterator andTerms = XOR.getTerms().iterator();
		/*
		 * Add all children of AND to every AndAlternative under XorAlternative
		 */
		while (andTerms.hasNext()) {
			Alternative anAndTerm = (Alternative) andTerms.next();
			// add all children of AND to the current "and" alternative
			anAndTerm.addTerms(primTerms);
		}

		policy.addTerm(XOR);
		policy.setNormalized(true);
		return policy;
	}

	/**
	 * @param attributes
	 *            The attributes to set.
	 */
	public void setAttributes(Hashtable<QName, String> attributes) {
		this.attributes = attributes;
	}

	public void setBase(String xmlBase) {
		this.xmlBase = xmlBase;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setPolicyURI() {
		Attr attri = element.getAttributeNodeNS(
				PolicyConstants.WSU_NAMESPACE_URI, "Id");
		if (attri != null) {
			setId(attri.getValue());
		}

		attri = element.getAttributeNodeNS(PolicyConstants.XMLNS_NAMESPACE_URI,
				"base");
		if (attri != null) {
			setBase(attri.getValue());
		}
	}
}
