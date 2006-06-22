/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.s2daoplugin.cache.model;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.seasar.kijimuna.core.ConstCore;
import org.seasar.kijimuna.core.parser.IElement;

public class ElementAdaptor implements IElement, ConstCore {

	private IElement element;
	
	public ElementAdaptor(IElement element) {
		if (element == null) {
			throw new IllegalArgumentException();
		}
		this.element = element;
	}
	
	public void addChild(IElement child) {
		element.addChild(child);
	}

	public IElement getParent() {
		return element.getParent();
	}

	public void setRootElement(IElement root) {
		element.setRootElement(root);
	}

	public void setParent(IElement parent) {
		element.setParent(parent);
	}

	public void setStartLocation(int depth, int startLine, int startColumn) {
		element.setStartLocation(depth, startLine, startColumn);
	}

	public int getStartLine() {
		return element.getStartLine();
	}

	public void setEndLocation(int endLine, int endColumn) {
		element.setEndLocation(endLine, endColumn);
	}

	public int getEndLine() {
		return element.getEndLine();
	}

	public void setAttributes(Map properties) {
		element.setAttributes(properties);
	}

	public String getElementName() {
		return element.getElementName();
	}

	public String getBody() {
		return element.getBody();
	}

	public void setBody(String body) {
		element.setBody(body);
	}

	public String getAttribute(String name) {
		return element.getAttribute(name);
	}

	public List getChildren() {
		return element.getChildren();
	}

	public IStorage getStorage() {
		return element.getStorage();
	}

	public IProject getProject() {
		return element.getProject();
	}

	public Object getAdapter(Class adapter) {
		return element.getAdapter(adapter);
	}
	
	public String toString() {
		return element.toString();
	}

}
