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

package org.w3c.policy.util;

/**
 * WSPConstants interfaces defines some CONST VALUES that are used in the entier
 * framework.
 * 
 */
public interface PolicyConstants {

	public static final String AND_ALTERNATIVE = "All";

	public static final String ATTR_NAME_OPTIONAL = "Optional";
	
	public static final String ATTR_NAME_IGNORABLE = "Ignorable";
	
	public static final String XOR_ALTERNATIVE = "ExactlyOne";

	public static final String WS_POLICY = "Policy";

	public static final String WS_POLICY_PREFIX = "wsp";

	public static final String QNAME_OPTIONAL = WS_POLICY_PREFIX + ":"
			+ ATTR_NAME_OPTIONAL;

	public static final String QNAME_IGNORABLE = WS_POLICY_PREFIX + ":"
        	+ ATTR_NAME_IGNORABLE;
	
	public static final String WS_POLICY_REFERENCE = "PolicyReference";

	public static final String WS_POLICY_NAMESPACE_URI = "http://www.w3.org/ns/ws-policy";

	public static final String SP_NAMESPACE_URI = "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702";

	public static final String SP_PREFIX = "sp";

	public static final String WSAM_NAMEPSECE_URI = "http://www.w3.org/2007/05/addressing/metadata";

	public static final String WSAM_PREFIX = "wsam";

	public static final String WSPE_NAMESPACE_URI = "http://www.iaas.uni-stuttgart.de/wsp-editor";

	public static final String WSPE_PREFIX = "wspe";
	
	public static final String WSU_NAMESPACE_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

	public static final String WSU_PREFIX = "wsu";
	
	public static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
	
	public static final String XML_PREFIX = "xml";

	public static final String XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";
	
	public static final String XMLNS_PREFIX = "xmlns";
	
	public static final String XS_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";

	public static final String XS_PREFIX = "xs";

	public static final String QNAME_POLICY = WS_POLICY_PREFIX + ":"
			+ WS_POLICY;

	public static final String QNAME_POLICY_REFERENCE = WS_POLICY_PREFIX + ":"
			+ WS_POLICY_REFERENCE;

	public static final String QNAME_XOR = WS_POLICY_PREFIX + ":"
			+ XOR_ALTERNATIVE;

	public static final String QNAME_AND = WS_POLICY_PREFIX + ":"
			+ AND_ALTERNATIVE;

	public static final String WS_POLICY_NS_NAME = XMLNS_PREFIX + ":"
			+ WS_POLICY_PREFIX;
}
