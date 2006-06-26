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
package org.seasar.s2daoplugin.cache.deployment.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.seasar.kijimuna.core.dicon.info.IComponentKey;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.kijimuna.core.dicon.model.IDiconElement;
import org.seasar.kijimuna.core.rtti.IRtti;

public class ContainerElementWrapper extends DiconElementWrapper implements
		IContainerElement {

	private IContainerElement container;
	
	public ContainerElementWrapper(IContainerElement container) {
		super(container);
		this.container = container;
	}
	
	public List getComponentList() {
		return new ArrayList(getChildren(DICON_TAG_COMPONENT));
	}

	public List getIncludeList() {
		// IncludeElement‚àƒ‰ƒbƒv‚·‚é‚È‚çcontainer.getIncludeList‚Í‚¢‚ç‚È‚¢
		List ret = new ArrayList(container.getIncludeList());
		ret.addAll(getChildren(DICON_TAG_INCLUDE));
		return ret;
	}

	public String getNamespace() {
		return container.getNamespace();
	}

	public String getPath() {
		return container.getPath();
	}

	public IComponentKey createComponentKey(Object key) {
		throw new UnsupportedOperationException();
	}

	public IDiconElement findDefinition(IComponentKey componentKey, Stack stack) {
		return container.findDefinition(componentKey, stack);
	}

	public IRtti getComponent(IComponentKey componentKey) {
		return container.getComponent(componentKey);
	}

}
