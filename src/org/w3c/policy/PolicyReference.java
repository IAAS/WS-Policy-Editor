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

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.Comment;
import org.w3c.policy.util.PolicyReader;
import org.w3c.policy.util.PolicyRegistry;

/**
 * PolicyReference class has implicit reference to a external policy. It acts 
 * as wrapper to external policies in the standard policy framework.
 * 
 */
public class PolicyReference implements IAssertion {

	private ArrayList<Comment> comments = new ArrayList<Comment>();
	
	private IAssertion parent = null;

	private String PolicyURIString = null;


	public PolicyReference(String policyURIString) {
		this.PolicyURIString = policyURIString;
	}

	public void addComment(Comment comment) {
		comments.add(comment);
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

	public String getPolicyURIString() {
		return PolicyURIString;
	}

	public IAssertion intersect(IAssertion assertion) {
		throw new UnsupportedOperationException();
	}

	public IAssertion intersect(IAssertion assertion, PolicyRegistry reg)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public boolean isNormalized() {
		throw new UnsupportedOperationException();
	}

	public IAssertion merge(IAssertion assertion) {
		throw new UnsupportedOperationException();
	}

	public IAssertion merge(IAssertion assertion, PolicyRegistry reg)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public IAssertion normalize() {
		return normalize(null);
	}

	public IAssertion normalize(PolicyRegistry reg) {
		// TODO resolve policy through URL

		/* Try to resolve the policy reference in file system */
		File file = new File(PolicyURIString);

		if (file.exists()) {
			return PolicyReader.parsePolicy(file);
		}

		/* 
		 * If not found in file system then try to resolve it in policy 
		 * registry 
		 */
		if (reg == null) {
			throw new RuntimeException("Cannot resolve : "
					+ getPolicyURIString() + " .. PolicyRegistry is null");
		}

		Policy targetPolicy = reg.lookup(getPolicyURIString());

		/* Cannot resolve policy reference */
		if (targetPolicy == null) {
			throw new RuntimeException("error : " + getPolicyURIString()
					+ " doesn't resolve to any known policy");
		}

		return targetPolicy;
	}

	/**
	 * @param commentList
	 *            The comments to set.
	 */
	public void setComments(ArrayList<Comment> commentList) {
		this.comments = commentList;
	}

	public void setNormalized(boolean flag) {
		throw new UnsupportedOperationException();
	}

	public void setParent(IAssertion parent) {
		this.parent = parent;
	}
}
