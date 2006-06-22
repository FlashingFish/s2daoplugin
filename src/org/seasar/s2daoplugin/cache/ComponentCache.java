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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.builder.ComponentCacheBuilder;
import org.seasar.s2daoplugin.cache.builder.ICacheBuilder;
import org.seasar.s2daoplugin.cache.builder.filter.IComponentFilter;
import org.seasar.s2daoplugin.util.StringUtil;

public class ComponentCache extends AbstractComponentCache {

	private IPath containerPath;
	private Map componentsByFqcn = new HashMap();
	private Map fqcnsByComponent = new HashMap();
	
	public ComponentCache(IComponentFilter filter) {
		this(new ComponentCacheBuilder(filter));
	}
	
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
		Set result = new HashSet();
		IRtti[] rtties = getAllTypes(fullyQualifiedClassName);
		for (int i = 0; i < rtties.length; i++) {
			Set components = (Set) componentsByFqcn.get(rtties[i].getQualifiedName());
			if (components != null) {
				result.addAll(components);
			}
		}
		return (IComponentElement[]) result.toArray(
				new IComponentElement[result.size()]);
	}

	public IComponentElement[] getAllComponents() {
		Set result = new HashSet();
		for (Iterator it = componentsByFqcn.values().iterator(); it.hasNext();) {
			result.addAll((Set) it.next());
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}

	public void setContainerPath(IPath containerPath) {
		this.containerPath = containerPath;
	}

	public IPath getContainerPath() {
		return containerPath;
	}

	public IType[] getAllAppliedTypes() {
		Set result = new HashSet();
		for (Iterator it = fqcnsByComponent.values().iterator(); it.hasNext();) {
			for (Iterator jt = ((Set) it.next()).iterator(); jt.hasNext();) {
				IRtti rtti = getRtti((String) jt.next());
				if (rtti != null) {
					result.add(rtti.getType());
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
		IRtti[] keys = getAllTypes(component);
		addComponentMap(keys, component);
	}

	public void removeComponent(IComponentElement component) {
		Set fqcns = (Set) fqcnsByComponent.get(component);
		if (fqcns == null) {
			return;
		}
		for (Iterator it = fqcns.iterator(); it.hasNext();) {
			removeComponentByFqcn((String) it.next(), component);
		}
		fqcnsByComponent.remove(component);
	}
	
	public void clearCache() {
		componentsByFqcn.clear();
		fqcnsByComponent.clear();
	}

	public IComponentCache getComponentCache(IPath containerPath) {
		return null;
	}
	
	private IRtti[] getAllTypes(IComponentElement component) {
		return getAllTypes(component.getComponentClassName());
	}
	
	private IRtti[] getAllTypes(String fullyQualifiedClassName) {
		return RttiUtil.getAllTypes(getRtti(fullyQualifiedClassName));
	}
	
	private void addComponentMap(IRtti[] keys, IComponentElement component) {
		String[] fqcns = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			fqcns[i] = keys[i].getQualifiedName();
			addComponentByFqcn(fqcns[i], component);
		}
		addFqcnByComponent(component, fqcns);
	}
	
	private void addComponentByFqcn(String fqcn, IComponentElement component) {
		if (componentsByFqcn.containsKey(fqcn)) {
			Set components = (Set) componentsByFqcn.get(fqcn);
			components.add(component);
		} else {
			Set components = new HashSet();
			components.add(component);
			componentsByFqcn.put(fqcn, components);
		}
	}
	
	private void addFqcnByComponent(IComponentElement component, String[] fqcns) {
		fqcnsByComponent.put(component, new HashSet(Arrays.asList(fqcns)));
	}
	
	private void removeComponentByFqcn(String fqcn, IComponentElement component) {
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
