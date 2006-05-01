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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.builder.ICacheBuilder;

public class ComponentCache extends AbstractComponentCache {

	private Map componentMap = new HashMap();						// IType, Set<IComponentElement
	
	public ComponentCache(ICacheBuilder builder) {
		super(builder);
	}
	
	public boolean contains(IType type) {
		return componentMap.containsKey(type);
	}

	public IComponentElement[] getComponents(IType type) {
		if (type == null) {
			return CacheConstants.EMPTY_COMPONENTS;
		}
		if (componentMap.containsKey(type)) {
			Set result = (Set) componentMap.get(type);
			return (IComponentElement[]) result
				.toArray(new IComponentElement[result.size()]);
		}
		return CacheConstants.EMPTY_COMPONENTS;
	}
	
	public IComponentElement[] getAllComponents() {
		List componentList = new LinkedList();
		for (Iterator it = componentMap.values().iterator(); it.hasNext();) {
			Set components = (Set) it.next();
			for (Iterator it2 = components.iterator(); it2.hasNext();) {
				IComponentElement component = (IComponentElement) it2.next();
				componentList.add(component);
			}
		}
		return (IComponentElement[]) componentList
			.toArray(new IComponentElement[componentList.size()]);
	}
	
	public void clearCache() {
		componentMap.clear();
	}
	
	public void addComponent(IComponentElement component) {
		addComponentMap(component);
	}
	
	public void removeComponent(IComponentElement component) {
		removeComponentMap(component);
	}
	
	private void addComponentMap(IComponentElement component) {
		IRtti componentRtti = getRtti(component);
		if (componentRtti != null) {
			addComponentMap(componentRtti.getType(), component);
		}
	}
	
	private void addComponentMap(IType type, IComponentElement component) {
		if (componentMap.containsKey(type)) {
			Set componentSet = (Set) componentMap.get(type);
			componentSet.add(component);
		} else {
			Set componentSet = new HashSet();
			componentSet.add(component);
			componentMap.put(type, componentSet);
		}
	}
	
	private void removeComponentMap(IComponentElement component) {
		IRtti componentRtti = getRtti(component);
		if (componentRtti != null) {
			removeComponentMap(componentRtti.getType(), component);
		}
	}
	
	private void removeComponentMap(IType type, IComponentElement component) {
		Set componentSet = (Set) componentMap.get(type);
		if (componentSet == null) {
			return;
		}
		componentSet.remove(component);
		if (componentSet.isEmpty()) {
			componentMap.remove(type);
		}
	}

}
