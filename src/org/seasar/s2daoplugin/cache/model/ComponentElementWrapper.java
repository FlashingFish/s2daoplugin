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

import java.util.ArrayList;
import java.util.List;

import org.seasar.kijimuna.core.dicon.info.IComponentKey;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;

public class ComponentElementWrapper extends DiconElementWrapper
		implements IComponentElement {

	private IComponentElement component;
	
	public ComponentElementWrapper(IComponentElement component) {
		super(component);
		this.component = component;
	}
	
	public String getComponentName() {
		return component.getComponentName();
	}

	public String getComponentClassName() {
		return component.getComponentClassName();
	}

	public String getAutoBindingMode() {
		return component.getAutoBindingMode();
	}

	public String getInstanceMode() {
		return component.getInstanceMode();
	}

	public List getArgList() {
		List ret = new ArrayList(component.getArgList());
		ret.addAll(getChildren(DICON_TAG_ARG));
		return ret;
	}

	public List getAspectList() {
		List ret = new ArrayList(component.getAspectList());
		ret.addAll(getChildren(DICON_TAG_ASPECT));
		return ret;
	}

	public List getDestroyMethodList() {
		List ret = new ArrayList(component.getDestroyMethodList());
		ret.addAll(getChildren(DICON_TAG_DESTROYMETHOD));
		return ret;
	}

	public List getInitMethodList() {
		List ret = new ArrayList(component.getInitMethodList());
		ret.addAll(getChildren(DICON_TAG_INITMETHOD));
		return ret;
	}

	public List getPropertyList() {
		List ret = new ArrayList(component.getPropertyList());
		ret.addAll(getChildren(DICON_TAG_PROPERTY));
		return ret;
	}

	public IComponentKey[] getTooManyComponentKeyArray(int tooMany) {
		return component.getTooManyComponentKeyArray(tooMany);
	}

	public void setLocking(boolean lock) {
		component.setLocking(lock);
	}

	public String getExpression() {
		return component.getExpression();
	}

	public boolean isOGNL() {
		return component.isOGNL();
	}
	
	public IComponentElement getCorrespondingComponent() {
		return component;
	}

}
