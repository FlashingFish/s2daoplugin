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

public class ComponentCache extends AbstractComponentCache {

	private Map componentByType = new HashMap();
	private Map componentByContainer = new HashMap();
	
	public ComponentCache(ICacheBuilder builder) {
		super(builder);
	}
	
	public IPath[] getAllContainerPaths() {
		return (IPath[]) componentByContainer.keySet()
				.toArray(new IPath[componentByContainer.size()]);
	}
	
	public IType[] getAllAppliedTypes() {
		return (IType[]) componentByType.keySet()
				.toArray(new IType[componentByType.size()]);
	}
	
	public IType[] getAppliedTypes(IPath containerPath) {
		Set components = (Set) componentByContainer.get(containerPath);
		if (components == null) {
			return new IType[0];
		}
		Set result = new HashSet();
		for (Iterator it = components.iterator(); it.hasNext();) {
			IComponentElement component = (IComponentElement) it.next();
			IRtti rtti = getRtti(component);
			if (rtti != null) {
				result.add(rtti.getType());
			}
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}
	
	public boolean contains(IType type) {
		return componentByType.containsKey(type);
	}
	
	public boolean contains(IType type, IPath containerPath) {
		Set components = (Set) componentByContainer.get(containerPath);
		if (components == null) {
			return false;
		}
		for (Iterator it = components.iterator(); it.hasNext();) {
			IComponentElement component = (IComponentElement) it.next();
			IRtti rtti = getRtti(component);
			if (rtti != null && rtti.getType().equals(type)) {
				return true;
			}
		}
		return false;
	}

	public IComponentElement[] getComponents(IType type) {
		if (type == null) {
			return CacheConstants.EMPTY_COMPONENTS;
		}
		if (componentByType.containsKey(type)) {
			Set result = (Set) componentByType.get(type);
			return (IComponentElement[]) result
				.toArray(new IComponentElement[result.size()]);
		}
		return CacheConstants.EMPTY_COMPONENTS;
	}
	
	public IComponentElement[] getComponents(IType type, IPath containerPath) {
		Set components = (Set) componentByContainer.get(containerPath);
		if (components == null) {
			return CacheConstants.EMPTY_COMPONENTS;
		}
		Set result = new HashSet();
		for (Iterator it = components.iterator(); it.hasNext();) {
			IComponentElement component = (IComponentElement) it.next();
			IRtti rtti = getRtti(component);
			if (rtti != null && rtti.getType().equals(type)) {
				result.add(component);
			}
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}
	
	public IComponentElement[] getAllComponents() {
		Set result = new HashSet();
		for (Iterator it = componentByType.values().iterator(); it.hasNext();) {
			Set components = (Set) it.next();
			for (Iterator it2 = components.iterator(); it2.hasNext();) {
				IComponentElement component = (IComponentElement) it2.next();
				result.add(component);
			}
		}
		return (IComponentElement[]) result
			.toArray(new IComponentElement[result.size()]);
	}
	
	public void clearCache() {
		componentByType.clear();
		componentByContainer.clear();
	}
	
	public void addComponent(IComponentElement component) {
		IRtti componentRtti = getRtti(component);
		if (componentRtti != null) {
			addComponentByType(componentRtti.getType(), component);
			addComponentByContainer(component.getStorage().getFullPath(), component);
		}
	}
	
	public void removeComponent(IComponentElement component) {
		IRtti componentRtti = getRtti(component);
		if (componentRtti != null) {
			removeComponentByType(componentRtti.getType(), component);
			removeComponentByContainer(component.getStorage().getFullPath(), component);
		}
	}
	
	private void addComponentByType(IType type, IComponentElement component) {
		addComponentMap(componentByType, type, component);
	}
	
	private void addComponentByContainer(IPath path, IComponentElement component) {
		addComponentMap(componentByContainer, path, component);
	}
	
	private void addComponentMap(Map map, Object key, IComponentElement component) {
		if (map.containsKey(key)) {
			Set components = (Set) map.get(key);
			components.add(component);
		} else {
			Set components = new HashSet();
			components.add(component);
			map.put(key, components);
		}
	}
	
	private void removeComponentByType(IType type, IComponentElement component) {
		removeComponentMap(componentByType, type, component);
	}
	
	private void removeComponentByContainer(IPath path, IComponentElement component) {
		removeComponentMap(componentByContainer, path, component);
	}
	
	private void removeComponentMap(Map map, Object key, IComponentElement component) {
		Set components = (Set) map.get(key);
		if (components == null) {
			return;
		}
		components.remove(component);
		if (components.isEmpty()) {
			map.remove(key);
		}
	}

}
