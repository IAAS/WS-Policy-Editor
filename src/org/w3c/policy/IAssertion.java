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

import org.w3c.policy.util.PolicyRegistry;

/**
 * Assertion is an interface which all constructs of policy must implements. It
 * defines few policy operations that all policy constructs must support.
 */

public interface IAssertion {

	/**
	 * Returns the parent of self or null if a parent non-exists
	 * 
	 * @return the parent of self
	 */
	public IAssertion getParent();

	/**
	 * Returns an assertion which is the equivalent of intersect of self and
	 * argument. The rules to construct the equivalent assertion are specified
	 * in WS Policy 1.0 specification.
	 * 
	 * @param assertion
	 *            the assertion to intersect with
	 * @return the equivalent of intersect of self and the argument
	 */
	public IAssertion intersect(IAssertion assertion)
			throws UnsupportedOperationException;

	/**
	 * Returns an assertion which is equivalent of intersect of self and
	 * argument. Here the external policy are resolved via a policy registry
	 * that is supplied as an argument.
	 * 
	 * @param assertion
	 *            the assertion to intersect with
	 * @param reg
	 *            the policy registry which is used to resolve external policy
	 *            references
	 * @return the equivalent of intersection of self and argument
	 * @throws UnsupportedOperationException
	 *             if the operation is not meaningful
	 */
	public IAssertion intersect(IAssertion assertion, PolicyRegistry reg)
			throws UnsupportedOperationException;


	public boolean isNormalized();

	/**
	 * Returns the equivalent of merge of self and argument. The rules to
	 * construct the equivalent of merge are defined in WS Policy specification
	 * 1.0
	 * 
	 * @param assertion
	 *            the argument to merge with
	 * @return the equivalent of the merge of self and argument
	 */
	public IAssertion merge(IAssertion assertion)
			throws UnsupportedOperationException;

	/**
	 * Returns the equivalent of merge of self and argument. The rules to
	 * construct argument are specified in WS Policy specification 1.0 Here the
	 * external policy references are resolved via a policy registry that is
	 * supplied as an argument
	 * 
	 * @param assertion
	 *            the assertion to merge with
	 * @param reg
	 *            the policy registry that should be used to resolve external
	 *            policy references
	 * @return the equivalent of merge of self and argument
	 * @throws UnsupportedOperationException
	 *             if the merge is not meaningful
	 */
	public IAssertion merge(IAssertion assertion, PolicyRegistry reg)
			throws UnsupportedOperationException;


	public IAssertion normalize() throws UnsupportedOperationException;


	public IAssertion normalize(PolicyRegistry reg)
			throws UnsupportedOperationException;

	/**
	 * 
	 * @param flag
	 */
	public void setNormalized(boolean flag);

	/**
	 * Set the parent to argument
	 * 
	 * @param parent
	 *            the parent that should be parent of self
	 */
	public void setParent(IAssertion parent);
}
