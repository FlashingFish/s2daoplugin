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
package org.seasar.s2daoplugin.cache.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IPropertyElement;
import org.seasar.kijimuna.core.parser.IElement;
import org.seasar.s2daoplugin.cache.model.ClassPattern;
import org.seasar.s2daoplugin.util.JavaUtil;

public class AspectAutoRegisterDeployer extends AbstractAutoRegisterDeployer {

	public AspectAutoRegisterDeployer(IComponentContainer container,
			IComponentElement component) {
		super(container, component);
		if (!ASPECT_AUTO_REGISTER.equals(component.getComponentClassName())) {
			throw new IllegalArgumentException();
		}
	}
	
	public void deploy() {
		IComponentElement[] components = getPreparedComponents();
		for (int i = 0; i < components.length; i++) {
			if (isApplied(components[i])) {
				process(components[i]);
			}
		}
		deploy(getAutoRegister());
	}
	
	private boolean isApplied(IComponentElement component) {
		String fqcn = component.getComponentClassName();
		String packageName = JavaUtil.getPackageName(fqcn);
		String className = JavaUtil.getShortClassName(fqcn);
		if (isIgnore(packageName, className)) {
			return false;
		}
		for (int i = 0; i < getClassPatternSize(); i++) {
			ClassPattern cp = getClassPattern(i);
			if (cp.isAppliedPackageName(packageName) &&
					cp.isAppliedShortClassName(className)) {
				return true;
			}
		}
		return false;
	}
	
	private void process(IComponentElement component) {
		IAspectElement aspect = (IAspectElement) createElement(DICON_TAG_ASPECT);
		createAttributes(aspect);
		createInterceptor(aspect);
		aspect.setParent(component);
//		component.addChild(aspect);
	}
	
	private void createAttributes(IAspectElement aspect) {
		String pointcut = getBody("pointcut");
		if (pointcut == null) {
			return;
		}
		Map attributes = new HashMap();
		attributes.put("pointcut", trimQuote(pointcut));
		aspect.setAttributes(attributes);
	}
	
	private void createInterceptor(IAspectElement aspect) {
		String interceptor = getBody("interceptor");
		if (interceptor != null) {
			aspect.setBody(interceptor);
		}
		List children = getPropertyChildren("interceptor");
		for (int i = 0; i < children.size(); i++) {
			aspect.addChild((IElement) children.get(i));
		}
	}
	
	private String getBody(String propertyName) {
		IPropertyElement prop = getPropertyElement(propertyName);
		return prop != null ? prop.getBody() : null;
	}
	
	private List getPropertyChildren(String propertyName) {
		IPropertyElement prop = getPropertyElement(propertyName);
		return prop != null ? prop.getChildren() : Collections.EMPTY_LIST;
	}
	
	private IPropertyElement getPropertyElement(String propertyName) {
		List props = getAutoRegister().getPropertyList();
		for (int i = 0; i < props.size(); i++) {
			IPropertyElement prop = (IPropertyElement) props.get(i);
			if (propertyName.equals(prop.getPropertyName())) {
				return prop;
			}
		}
		return null;
	}
	
	private String trimQuote(String value) {
		String newValue = value.trim();
		if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
			int s = newValue.indexOf('"');
			int e = newValue.lastIndexOf('"');
			return newValue.substring(s + 1, e);
		}
		return newValue;
	}

}
