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
package org.seasar.s2daoplugin.cache.deployment.deployer;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;

public abstract class AbstractComponentDeployer implements IComponentDeployer {

	private IDeploymentContainer container;
	private IComponentElement component;
	
	public AbstractComponentDeployer(IDeploymentContainer container,
			IComponentElement component) {
		this(container, component, null);
	}
	
	public AbstractComponentDeployer(IDeploymentContainer container,
			IComponentElement component, String componentClassName) {
		if (container == null || component == null) {
			throw new IllegalArgumentException();
		}
		this.container = container;
		this.component = component;
		if (componentClassName != null) {
			assertComponentClassName(componentClassName);
		}
	}
	
	public void deploy() {
		getContainer().addComponent(getComponent());
		doDeploy();
	}
	
	protected abstract void doDeploy();
	
	protected IDeploymentContainer getContainer() {
		return container;
	}
	
	protected IComponentElement getComponent() {
		return component;
	}
	
	protected void assertComponentClassName(String componentClassName) {
		if (!component.getComponentClassName().equals(componentClassName)) {
			throw new IllegalArgumentException();
		}
	}
	
	protected IRtti findRttiReferencedClassField(String staticOgnl) {
		int begin = staticOgnl.indexOf('@');
		int end = staticOgnl.lastIndexOf("@class");
		if (begin == -1 || end == -1 || begin == end) {
			return null;
		}
		String className = staticOgnl.substring(begin + 1, end);
		return getComponent().getRttiLoader().loadRtti(className);
	}
	
	protected String trimQuote(String value) {
		String newValue = value.trim();
		if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
			int s = newValue.indexOf('"');
			int e = newValue.lastIndexOf('"');
			return newValue.substring(s + 1, e);
		}
		return newValue;
	}

}
