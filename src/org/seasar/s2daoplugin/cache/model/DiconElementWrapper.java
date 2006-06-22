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

import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.kijimuna.core.dicon.model.IDiconElement;
import org.seasar.kijimuna.core.rtti.RttiLoader;

public class DiconElementWrapper extends ElementWrapper implements
		IDiconElement {

	private IDiconElement element;
	
	public DiconElementWrapper(IDiconElement element) {
		super(element);
		this.element = element;
	}
	
	public RttiLoader getRttiLoader() {
		return element.getRttiLoader();
	}

	public IContainerElement getContainerElement() {
		if (getRoot() instanceof IContainerElement) {
			return (IContainerElement) getRoot();
		}
		return null;
	}

	public int getMarkerSeverity() {
		return element.getMarkerSeverity();
	}

	public void setMarkerServerity(int severity) {
		element.setMarkerServerity(severity);
	}

	public String getDisplayName() {
		return element.getDisplayName();
	}

}
