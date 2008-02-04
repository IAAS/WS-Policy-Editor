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

import javax.xml.namespace.QName;

import org.w3c.policy.util.PolicyRegistry;
import org.w3c.policy.util.Vocabulary;

/**
 * AndAlternative represents either policy or a single policy alternative. It
 * requires that all its terms are satisfied.
 */
public class AllAlternative extends Alternative implements IAssertion {

	public AllAlternative() {
	}

	/**
	 * Adds an Assertion to its terms list
	 * 
	 * @param assertion
	 *            Assertion to be added
	 */
	@Override
	public void addTerm(IAssertion assertion) {
		if (!(isNormalized() && (assertion instanceof PrimitiveAssertion))) {
			setNormalized(false);
		}
		super.addTerm(assertion);
	}

	/**
	 * Precondition: xorTerms.size() >= 1
	 *   
	 * @param xorTerms - child XORs of an AND. The children are normalized 
	 *        (i.e., ANDs as childs of each XORs)
	 * @return child ANDs of an XOR
	 */
	public ArrayList<AllAlternative> distributeXORAlternatives(
			ArrayList<ExactlyOneAlternative> xorTerms) {
		//Debug.Assert(xorTerms.size() >= 1);
		ArrayList<AllAlternative> result = new ArrayList<AllAlternative>();

		if (xorTerms.size() <= 1) {
		    /* Get the first ExactlyOne alternative in the array list */
			ExactlyOneAlternative xorTerm = xorTerms.get(0);

			// result.addAll((List<AllAlternative>)(xorTerm.getTerms());
			// in verbose mode, because of stupid type safety:

			Iterator iterator = xorTerm.getTerms().iterator();
			/* For each All alternative in this ExactlyOne alternative */
			while (iterator.hasNext()) {
				AllAlternative andTerm = (AllAlternative) iterator.next();

				// AndAlternative anAndTerm = new AndAlternative();
				//
				// anAndTerm.addComments(andTermA.getComments());
				//
				// anAndTerm.addTerms(andTermA.getTerms());
				result.add(andTerm);
			}
		} else {
			/* Get the first ExactlyOne alternative in the array list */
			ExactlyOneAlternative xorTerm = xorTerms.get(0);

			ArrayList<ExactlyOneAlternative> subList = xorTerms;
			subList.remove(0);

			/*
			 * Get the distribution result of the rest of ExactlyOne
			 * alternatives
			 */
			ArrayList<AllAlternative> subResult = distributeXORAlternatives(subList);

			Iterator iterator = xorTerm.getTerms().iterator();

			/* For each All alternative in this ExactlyOne alternative */
			while (iterator.hasNext()) {
				AllAlternative andTermA = (AllAlternative) iterator.next();

				/* Distribute this All alternative with the distribution 
				 * result*/
				for (int i = 0; i < subResult.size(); i++) {
					AllAlternative andTermB = subResult.get(i);

					AllAlternative anAndTerm = new AllAlternative();

					anAndTerm.addComments(andTermA.getComments());
					anAndTerm.addComments(andTermB.getComments());

					anAndTerm.addTerms(andTermA.getTerms());
					anAndTerm.addTerms(andTermB.getTerms());
					result.add(anAndTerm);
				}

			}			
		}
		
		return result;
	}

	/**
	 *  Get the my vocabulary 
	 */
	public Vocabulary getVocabulary() {
		AllAlternative andTerm = (AllAlternative) (this.isNormalized() ? this
				: this.normalize());

		List primTerms = andTerm.getTerms();

		Vocabulary vocabulary = new Vocabulary();

		for (int i = 0; i < primTerms.size(); i++) {
			PrimitiveAssertion primAssertion = (PrimitiveAssertion) primTerms
					.get(i);

			String qName = primAssertion.getQName().toString();

			vocabulary.add(qName);
		}

		return vocabulary;
	}

	/**
	 * Returns the intersection of self and argument against a specified Policy
	 * Registry.
	 * 
	 * @param assertion
	 *            the assertion to intersect with self
	 * @param reg
	 *            a sepcified policy registry
	 * @return assertion the assertion which is equivalent to intersection
	 *         between self and the argument
	 */
	public IAssertion intersect(IAssertion assertion, PolicyRegistry reg) {

		/* If myself is not normalized, then normalize me. */
		Alternative normalizedMe = (Alternative) ((isNormalized()) ? this
				: normalize(reg));

		/* Am not an All alternative anymore */
		if (!(normalizedMe instanceof AllAlternative)) {
			return normalizedMe.intersect(assertion, reg);
		}

		/* Intesect with a primitive assertion */
		if (assertion instanceof PrimitiveAssertion) {
			QName qname = ((PrimitiveAssertion) assertion).getQName();

			/* Get an iterator over the children of this all alternative */
			Iterator iterator = getTerms().iterator();

			/* We suppose that there is a match */
			boolean isMatch = true;

			/* Compare vocabularies */
			while (iterator.hasNext()) {
				PrimitiveAssertion primTerm = (PrimitiveAssertion) iterator
						.next();

				/* If a mismatch was found */
				if (!primTerm.getQName().equals(qname)) {
					isMatch = false;
					break;
				}
			}
			return (isMatch) ? normalizedMe : new ExactlyOneAlternative();
		}

		/* Otherwise is the parameter assertion an alternative */
		Alternative target = (Alternative) assertion;

		/* If the target is not normalized, then normalize it. */
		target = (Alternative) ((target.isNormalized()) ? target : target
				.normalize(reg));

		/* If target is a policy */
		if (target instanceof Policy) {

			/* Get the ExactlyOne alternative of the policy */
			ExactlyOneAlternative alters = (ExactlyOneAlternative) target
					.getTerms().get(0);
			return normalizedMe.intersect(alters);

		} else if (target instanceof ExactlyOneAlternative) {
			/* If target is a ExactlyOne alternative */
			ExactlyOneAlternative result = new ExactlyOneAlternative();

			/* Interect myself with all children of the target */
			Iterator iterator = target.getTerms().iterator();

			while (iterator.hasNext()) {
				AllAlternative andTerm = (AllAlternative) iterator.next();

				/* See below "Intersect myself with an All alternative" */
				IAssertion value = normalizedMe.intersect(andTerm);

				if (value instanceof AllAlternative) {
					result.addTerm(value);
				}
			}
			return result;
		}

		/* Get the vocabularies of the two All alternatives. */
		Vocabulary myVocabulary = ((AllAlternative) normalizedMe)
				.getVocabulary();

		Vocabulary targetVocabulary = ((AllAlternative) target)
		    .getVocabulary();

		/* Compare the vocabularies */
		boolean vocabularyMatch = myVocabulary.compare(targetVocabulary);

		boolean semanticMatch = false;
		if (vocabularyMatch) {
			semanticMatch = matchSemantic(normalizedMe, target);
		}

		/*
		 * If a matching was found, then add all primitive assertions of the 
		 * two alternatives to the result
		 */
		if (semanticMatch) {
			AllAlternative result = new AllAlternative();

			result.addTerms(normalizedMe.getTerms());
			result.addTerms(target.getTerms());
			return result;
		}

		/* Otherwise no behaviour is admissible */
		return new ExactlyOneAlternative();
	}

	/**
	 * @param normalizedMe
	 * @param target
	 * @return
	 */
	private boolean matchSemantic(Alternative normalizedMe, 
			Alternative target) {

		return true;
	}

	/**
	 * Returns an assertion which is equivalent to merge of self and the
	 * argument.
	 * 
	 * @param assertion
	 *            the assertion to be merged with
	 * @param reg
	 *            the policy registry which the is used resolve external policy
	 *            references
	 * @return assertion the resultant assertion which is equivalent to merge 
	 *         of self and argument
	 */
	public IAssertion merge(IAssertion assertion, PolicyRegistry reg) {
		/* If I am not normalized, then normalize me. */
		Alternative normalizedMe = (Alternative) ((isNormalized()) ? this
				: normalize(reg));

		/* If the result of normalization is no longer an all alternative */
		if (!(normalizedMe instanceof AllAlternative)) {
			return normalizedMe.merge(assertion, reg);
		}

		/* Merge with an assertion */
		if (assertion instanceof PrimitiveAssertion) {
			AllAlternative andTerm = new AllAlternative();

			andTerm.addTerm(assertion);
			andTerm.addTerms(normalizedMe.getTerms());

			andTerm.setNormalized(true);
			return andTerm;
		}

		/* Otherwise if parameter is kein primitive assertions */
		Alternative target = (Alternative) assertion;

		/* If the target is not normalized, then normalize it. */
		target = (Alternative) ((target.isNormalized()) ? target : target
				.normalize(reg));

		/* If target is a policy */
		if (target instanceof Policy) {
			/* Get the ExactlyOne alternative of the policy */
			ExactlyOneAlternative xorTerm = (ExactlyOneAlternative) target
					.getTerms().get(0);
			return normalizedMe.merge(xorTerm);

		} else if (target instanceof ExactlyOneAlternative) {
			/* If target is a ExactlyOne alternative */
			ExactlyOneAlternative xorTerm = new ExactlyOneAlternative();

			Iterator hisAndTerms = target.getTerms().iterator();
			/* Merge me with his All alternatives and add the results to him */
			while (hisAndTerms.hasNext()) {
				AllAlternative hisAndTerm = (AllAlternative) hisAndTerms.next();
				xorTerm.addTerm(normalizedMe.merge(hisAndTerm));
			}

			xorTerm.setNormalized(true);
			return xorTerm;

		} else if (target instanceof AllAlternative) {
			/* If target is an All alternative */
			AllAlternative andTerm = new AllAlternative();

			andTerm.addTerms(normalizedMe.getTerms());
			andTerm.addTerms(target.getTerms());
			andTerm.setNormalized(true);
			return andTerm;
		}

		throw new IllegalArgumentException("error : merge is not defined for"
				+ assertion.getClass().getName());
	}

	/**
	 * Returns an Assertion which is normalized using a specified policy
	 * registry.
	 * 
	 * @param reg
	 *            the policy registry used to resolve policy references
	 * @return an Assertion which is the normalized form of self
	 * This can be either AND or XOR
	 */
	public IAssertion normalize(PolicyRegistry reg) {
		if (isNormalized()) {
			return this;
		}
		
		// one possible result -> XOR follows later
		AllAlternative AND = new AllAlternative();

		// normalized empty AND is an empty AND
		if (isEmpty()) {
			AND.setComments(getComments());
			AND.setNormalized(true);
			return AND;
		}

		// collects nested XOR-alternatives
		ArrayList<ExactlyOneAlternative> xorTerms = new ArrayList<ExactlyOneAlternative>();

		Iterator terms = getTerms().iterator();
		while (terms.hasNext()) {
			IAssertion term = (IAssertion) terms.next();
			
			term = (term instanceof Policy) ? term : term.normalize(reg);
			// term is normlized if term is not a policy

			if (term instanceof Policy) {
				/* wsp:Policy is equivalent to wsp:All */
				IAssertion wrapper = new AllAlternative();

				/* Wrap all children of the policy into an All alternative */
				((AllAlternative) wrapper).addTerms(((Policy) term).getTerms());
				
				/* Normalize the resulted alternative */
				term = wrapper.normalize(reg);
			}			
			// term is normalized and not a policy

			if (term instanceof ExactlyOneAlternative) {
				/* Empty ExactlyOne alternative means no behavior is admissible */
				if (((ExactlyOneAlternative) term).isEmpty()) {
					/*
					 * Return an empty ExatctlyOne alternative and terminate the
					 * processing
					 */
					ExactlyOneAlternative emptyXor = new ExactlyOneAlternative();
					emptyXor.setNormalized(true);
					return emptyXor;
				} else {
					xorTerms.add((ExactlyOneAlternative) term);
				}
			} else if (term instanceof AllAlternative) {
				/* Idempotent */
				AND.addTerms(((AllAlternative) term).getTerms());
			} else {
				// term is an assertion
				/* Associative Law */
				AND.addTerm(term);
			}
		}		
		// AND is filled with normalized XORs or assertions

		ExactlyOneAlternative XOR = new ExactlyOneAlternative();	
		
		/* Distributive Law */
		if (xorTerms.size() > 1) {
			// xorTerms are children of the current AND
			ArrayList<AllAlternative> andTerms = distributeXORAlternatives(xorTerms);
			// result: ANDs to be added to an XOR
			for (int i = 0; i < andTerms.size(); i++) {
				XOR.addTerm(andTerms.get(i));
			}
		} else if (xorTerms.size() == 1) {
			ExactlyOneAlternative xorTerm = xorTerms.get(0);
			XOR.addTerms(xorTerm.getTerms());
		}
		//xorTerms are now merged into XOR 

		// no XOR alternatives -> just return the AND
		if (XOR.isEmpty()) {
			AND.setNormalized(true);
			return AND;
		}

		// XOR is not empty, but AND is empty -> no further handling necessary
		if (AND.isEmpty()) {
			XOR.setNormalized(true);
			return XOR;
		}

		// no it gets hard. We have XORs and ANDs. They have to be merged

		// cf. Policy.java-code: put all ANDs into the XORs
		List primTerms = AND.getTerms();
		Iterator interator = XOR.getTerms().iterator();
		while (interator.hasNext()) {
			AllAlternative andTerm = (AllAlternative) interator.next();
			andTerm.addTerms(primTerms);
		}

		XOR.setNormalized(true);
		return XOR;
	}

}
