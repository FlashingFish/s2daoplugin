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
package org.seasar.s2daoplugin.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.builder.ICacheBuilder;
import org.seasar.s2daoplugin.util.StringUtil;

public class ComponentCache extends AbstractComponentCache {

	private Map componentsByFqcn = new HashMap();
	
	public ComponentCache(ICacheBuilder builder) {
		super(builder);
	}
	
	public IComponentElement[] getComponents(IType type) {
		return type != null ?
				getComponents(type.getFullyQualifiedName()) : EMPTY_COMPONENTS;
	}

	public IComponentElement[] getComponents(String fullyQualifiedClassName) {
		if (StringUtil.isEmpty(fullyQualifiedClassName)) {
			return EMPTY_COMPONENTS;
		}
		Set components = (Set) componentsByFqcn.get(fullyQualifiedClassName);
		if (components == null) {
			return EMPTY_COMPONENTS;
		}
		return (IComponentElement[]) components.toArray(
				new IComponentElement[components.size()]);
	}

	public IComponentElement[] getAllComponents() {
		Set result = new HashSet();
		for (Iterator it = componentsByFqcn.values().iterator(); it.hasNext();) {
			result.addAll((Set) it.next());
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}

	public void setContainerPath(IPath containerPath) {
		// do nothing
	}

	public IPath getContainerPath() {
		return null;
	}

	public IType[] getAllAppliedTypes() {
		Set result = new HashSet();
		for (Iterator it = componentsByFqcn.values().iterator(); it.hasNext();) {
			for (Iterator jt = ((Set) it.next()).iterator(); jt.hasNext();) {
				IComponentElement component = (IComponentElement) jt.next();
				if (existsType(component)) {
					result.add(getRtti(component).getType());
				}
			}
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}

	public boolean contains(IType type) {
		return type != null ? contains(type.getFullyQualifiedName()) : false;
	}

	public boolean contains(String fullyQualifiedClassName) {
		Set components = (Set) componentsByFqcn.get(fullyQualifiedClassName);
		if (components == null) {
			return false;
		}
		for (Iterator it = components.iterator(); it.hasNext();) {
			IComponentElement component = (IComponentElement) it.next();
			if (existsType(component)) {
				return true;
			}
		}
		return false;
	}

	public void addComponent(IComponentElement component) {
		if (component == null) {
			return;
		}
		addComponentMap(component);
	}

	public void removeComponent(IComponentElement component) {
		if (component == null) {
			return;
		}
		removeComponentMap(component);
	}

	public void clearCache() {
		componentsByFqcn.clear();
	}

	public IComponentCache getComponentCache(IPath containerPath) {
		return null;
	}
	
	protected void addComponentMap(IComponentElement component) {
		String fqcn = component.getComponentClassName();
		if (StringUtil.isEmpty(fqcn)) {
			return;
		}
		if (componentsByFqcn.containsKey(fqcn)) {
			Set components = (Set) componentsByFqcn.get(fqcn);
			components.add(component);
		} else {
			Set components = new HashSet();
			components.add(component);
			componentsByFqcn.put(fqcn, components);
		}
	}
	
	protected void removeComponentMap(IComponentElement component) {
		String fqcn = component.getComponentClassName();
		Set components = (Set) componentsByFqcn.get(fqcn);
		if (components == null) {
			return;
		}
		components.remove(component);
		if (components.isEmpty()) {
			componentsByFqcn.remove(fqcn);
		}
	}
	
	private boolean existsType(IComponentElement component) {
		IRtti rtti = getRtti(component);
		return rtti != null && rtti.getType() != null;
	}

}
