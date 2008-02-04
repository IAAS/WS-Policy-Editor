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

import java.util.Iterator;

import org.w3c.policy.util.PolicyRegistry;

/**
 * XORCompositeAssertion represents a bunch of policy alternatives. It requires
 * that exactly one of its terms (policy alternative) is statisfied.
 * 
 */
public class ExactlyOneAlternative extends Alternative implements IAssertion {

	public ExactlyOneAlternative() {
	}

	@Override
	public void addTerm(IAssertion assertion) {
		/*
		 * By adding a child that is not an normalized All alternative and I am
		 * not normalized
		 */
		if (!(isNormalized() && (assertion instanceof AllAlternative) && ((AllAlternative) assertion)
				.isNormalized())) {
			/* Set me to not normalized */
			setNormalized(false);
		}
		super.addTerm(assertion);
	}

	public IAssertion intersect(IAssertion assertion, PolicyRegistry reg) {

		/* If myself is not normalized, then normalize me. */
		Alternative normalizedMe = (Alternative) ((isNormalized()) ? this
				: normalize(reg));

		/* Am not an ExactlyOne alternative anymore */
		if (!(normalizedMe instanceof ExactlyOneAlternative)) {
			return normalizedMe.intersect(assertion, reg);
		}

		ExactlyOneAlternative result = new ExactlyOneAlternative();

		if (assertion instanceof PrimitiveAssertion) {
			Iterator iterator = normalizedMe.getTerms().iterator();

			while (iterator.hasNext()) {
				AllAlternative andTerm = (AllAlternative) iterator.next();
				IAssertion value = andTerm.intersect(assertion);
				if (value instanceof AllAlternative) {
					result.addTerm(value);
				}
			}
			return result;
		}

		/* Otherwise is the parameter assertion an alternative */
		Alternative target = (Alternative) assertion;

		/* If the target is not normalized, then normalize it. */
		target = (Alternative) ((target.isNormalized()) ? target : target
				.normalize(reg));

		Iterator iterator = normalizedMe.getTerms().iterator();

		/* For each children All alernative of mine */
		while (iterator.hasNext()) {
			AllAlternative andTerm = (AllAlternative) iterator.next();

			if (target instanceof AllAlternative) {
				/* Intersect my children All alternative with target */
				IAssertion value = andTerm.intersect(target);

				if (value instanceof AllAlternative) {
					result.addTerm(value);
				}

			} else if (target instanceof ExactlyOneAlternative) {

				Iterator andTerms = target.getTerms().iterator();

				/* For each children All alternative of target */
				while (andTerms.hasNext()) {
					AllAlternative tAndTerm = (AllAlternative) andTerms.next();

					/*
					 * Intersect my children All alternative with the one of
					 * target
					 */
					IAssertion value = andTerm.intersect(tAndTerm);

					if (value instanceof AllAlternative) {
						result.addTerm(value);
					}
				}
			}
		}

		return result;
	}

	public IAssertion merge(IAssertion assertion, PolicyRegistry reg) {
		/* If I am not normalized, then normalize me. */
		Alternative normalizedMe = (Alternative) ((isNormalized()) ? this
				: normalize(reg));

		/* If the result of normalization is no longer an all alternative */
		if (!(normalizedMe instanceof ExactlyOneAlternative)) {
			return normalizedMe.merge(assertion, reg);
		}

		if (assertion instanceof PrimitiveAssertion) {
			ExactlyOneAlternative xorTerm = new ExactlyOneAlternative();

			Iterator iterator = normalizedMe.getTerms().iterator();

			if (iterator.hasNext()) {
				do {
					AllAlternative andTerm = new AllAlternative();

					andTerm.addTerm(assertion);

					AllAlternative anAndTerm = (AllAlternative) iterator.next();

					andTerm.addTerms(anAndTerm.getTerms());

					xorTerm.addTerm(andTerm);
				} while (iterator.hasNext());
			} else {
				AllAlternative andTerm = new AllAlternative();

				andTerm.addTerm(assertion);

				xorTerm.addTerm(andTerm);
			}

			xorTerm.setNormalized(true);

			return xorTerm;
		}

		Alternative target = (Alternative) assertion;

		target = (Alternative) ((target.isNormalized()) ? target : target
				.normalize(reg));

		if (target instanceof Policy) {
			ExactlyOneAlternative xorTerm = (ExactlyOneAlternative) target.getTerms().get(0);
			return normalizedMe.merge(xorTerm);

		} else if (target instanceof ExactlyOneAlternative) {
			int mySize = normalizedMe.getTerms().size();
			int hisSize = target.getTerms().size();
			
			if (mySize==0) {
				return target;
			}else if (hisSize == 0) {
				return normalizedMe;
			}
			
			ExactlyOneAlternative xorTerm = new ExactlyOneAlternative();

			Iterator myAndTerms = normalizedMe.getTerms().iterator();

			while (myAndTerms.hasNext()) {
				AllAlternative myAndTerm = (AllAlternative) myAndTerms.next();

				Iterator hisAndTerms = target.getTerms().iterator();

				while (hisAndTerms.hasNext()) {
					AllAlternative hisAndTerm = (AllAlternative) hisAndTerms
							.next();

					xorTerm.addTerm(myAndTerm.merge(hisAndTerm));
				}
			}

			xorTerm.setNormalized(true);

			return xorTerm;

		} else if (target instanceof AllAlternative) {
			ExactlyOneAlternative xorTerm = new ExactlyOneAlternative();

			Iterator myAndTerms = normalizedMe.getTerms().iterator();

			while (myAndTerms.hasNext()) {
				AllAlternative andTerm = new AllAlternative();

				andTerm.addTerms(target.getTerms());

				AllAlternative myAndTerm = (AllAlternative) myAndTerms.next();

				andTerm.addTerms(myAndTerm.getTerms());

				xorTerm.addTerm(andTerm);
			}

			xorTerm.setNormalized(true);

			return xorTerm;
		}

		throw new IllegalArgumentException("error : merge is not defined for"
				+ target.getClass().getName());
	}

	/**
	 * @return XOR with only ALL operators as children
	 */
	public IAssertion normalize(PolicyRegistry reg) {
		if (isNormalized()) {
			return this;
		}

		// XOR is the result
		ExactlyOneAlternative XOR = new ExactlyOneAlternative();

		if (isEmpty()) {
			XOR.setNormalized(true);
			return XOR;
		}

		Iterator terms = getTerms().iterator();

		while (terms.hasNext()) {
			IAssertion term = (IAssertion) terms.next();

			term = (term instanceof Policy) ? term : term.normalize(reg);
			// term is normalized if term is not a policy
			
			if (term instanceof Policy) {
				// Convert policy to All
				IAssertion wrapper = new AllAlternative();
				((AllAlternative) wrapper).addTerms(((Policy) term).getTerms());
				wrapper = wrapper.normalize(reg);

				// a normalized all alternative is either an ALL-alternative or an XOR-alternative				
				if (wrapper instanceof AllAlternative) {
					XOR.addTerm(wrapper);
				} else {
					XOR.addTerms(((ExactlyOneAlternative) wrapper).getTerms());
				}
			} else if (term instanceof ExactlyOneAlternative) {
				XOR.addTerms(((ExactlyOneAlternative) term).getTerms());
			} else if (term instanceof AllAlternative) {
				XOR.addTerm(term);
			} else if (term instanceof PrimitiveAssertion) {
				AllAlternative wrapper = new AllAlternative();
				wrapper.addTerm(term);
				XOR.addTerm(wrapper);
			}
		}

		XOR.setNormalized(true);
		
		return XOR;
	}
}
