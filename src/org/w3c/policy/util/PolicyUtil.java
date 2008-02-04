/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.w3c.policy.util;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.policy.AllAlternative;
import org.w3c.policy.Policy;
import org.w3c.policy.PrimitiveAssertion;
import org.w3c.policy.ExactlyOneAlternative;

/**
 * WSPolicyUtil contains several utility methods for policy manipulations.
 * 
 */
public class PolicyUtil {

	public static boolean isPolicyReference(Node node) {
		if (node instanceof Element) {
			return node.getNodeName().equals(
					PolicyConstants.QNAME_POLICY_REFERENCE);
		}
		return false;
	}

	public static boolean isPolicyOperator(Node node) {
		if (node instanceof Element) {
			return node.getNodeName().equals(PolicyConstants.QNAME_POLICY);
		}
		return false;
	}

	public static boolean isXOROperator(Node node) {
		if (node instanceof Element) {
			return node.getNodeName().equals(PolicyConstants.QNAME_XOR);
		}
		return false;
	}

	public static boolean isAllOperator(Node node) {
		if (node instanceof Element) {
			return node.getNodeName().equals(PolicyConstants.QNAME_AND);
		}
		return false;
	}

	public static boolean isAssertion(Node node) {
		if (node instanceof Element) {
			return !(isPolicyOperator(node) || isXOROperator(node)
					|| isAllOperator(node) || isPolicyReference(node));
		}
		return false;
	}

	public static boolean matchByQName(PrimitiveAssertion primTermA,
			PrimitiveAssertion primTermB) {
		return primTermA.getQName().equals(primTermB.getQName());
	}

	public static boolean matchByQName(List primTermsA, List primTermsB) {
		List larger = (primTermsA.size() > primTermsB.size()) ? primTermsA
				: primTermsB;
		List smaller = (primTermsA.size() < primTermsB.size()) ? primTermsA
				: primTermsB;

		Iterator iterator = larger.iterator();
		PrimitiveAssertion primTerm;
		QName qname;
		Iterator iterator2;
		while (iterator.hasNext()) {
			primTerm = (PrimitiveAssertion) iterator.next();
			qname = primTerm.getQName();
			iterator2 = smaller.iterator();

			boolean match = false;
			PrimitiveAssertion primTerm2;
			while (iterator2.hasNext()) {
				primTerm2 = (PrimitiveAssertion) iterator2.next();
				if (primTerm2.getQName().equals(qname)) {
					match = true;
					break;
				}
			}
			if (!match) {
				return false;
			}
		}
		return true;
	}

	public static List getPrimTermList(Policy policy) {
		if (!policy.isNormalized()) {
			policy = (Policy) policy.normalize();
		}

		ExactlyOneAlternative xorTerm = (ExactlyOneAlternative) 
		      policy.getTerms().get(0);
		AllAlternative andTerm = (AllAlternative) xorTerm.getTerms().get(0);

		return andTerm.getTerms();
	}

	public static Policy mergePolicies(List policyList, PolicyRegistry reg) {
		Policy policyTerm = null;
		Iterator iterator = policyList.iterator();

		Policy policyTerm2;
		while (iterator.hasNext()) {
			policyTerm2 = (Policy) iterator.next();
			policyTerm = (policyTerm == null) ? policyTerm2
					: (Policy) policyTerm.merge(policyTerm2, reg);
		}

		if (!policyTerm.isNormalized()) {
			policyTerm = (Policy) policyTerm.normalize();
		}
		return policyTerm;
	}

	public static Policy getPolicy(List terms) {
		Policy policyTerm = new Policy();
		ExactlyOneAlternative xorTerm = new ExactlyOneAlternative();
		AllAlternative andTerm = new AllAlternative();

		andTerm.addTerms(terms);
		xorTerm.addTerm(andTerm);
		policyTerm.addTerm(xorTerm);

		return policyTerm;
	}

	public static Policy intersect(File file1, File file2) {
		Policy policy1 = PolicyReader.parsePolicy(file1);

		return intersect(policy1, file2);
	}

	public static Policy intersect(Policy policy1, File file) {
		Policy policy2 = PolicyReader.parsePolicy(file);

		return (Policy) policy1.intersect(policy2);
	}

	public static Policy merge(File file1, File file2) {
		Policy policy1 = PolicyReader.parsePolicy(file1);

		return merge(policy1, file2);
	}

	public static Policy merge(Policy policy1, File file) {
		Policy policy2 = PolicyReader.parsePolicy(file);

		return (Policy) policy1.merge(policy2);
	}
}
