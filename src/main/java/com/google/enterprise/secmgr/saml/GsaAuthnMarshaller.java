/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.enterprise.secmgr.saml;

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

/** A marshaller (object to XML converter) for {@link GsaAuthn}. */
final class GsaAuthnMarshaller extends AbstractSAMLObjectMarshaller {
  @Override
  protected void marshallAttributes(XMLObject xmlObject, Element domElement) {
    GsaAuthn object = (GsaAuthn) xmlObject;
    XMLHelper.marshallAttribute(GsaAuthn.VERSION_ATTRIB_NAME, Integer.toString(object.getVersion()),
        domElement, false);
    XMLHelper.marshallAttribute(GsaAuthn.SESSION_ID_ATTRIB_NAME, object.getSessionId(),
        domElement, false);
  }
}
