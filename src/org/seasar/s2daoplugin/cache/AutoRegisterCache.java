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
import org.seasar.s2daoplugin.cache.builder.AutoRegisterUtil;
import org.seasar.s2daoplugin.cache.builder.ICacheBuilder;
import org.seasar.s2daoplugin.cache.model.IAutoRegisterElement;
import org.seasar.s2daoplugin.util.StringUtil;

public class AutoRegisterCache extends AbstractComponentCache {

	private Set componentAutos = new HashSet();
	private Map componentTargetAutos = new HashMap();
	
	public AutoRegisterCache(ICacheBuilder builder) {
		super(builder);
	}
	
	public IComponentElement[] getComponents(IType type) {
		return type != null ? getComponents(type.getFullyQualifiedName()) :
				EMPTY_COMPONENTS;
	}

	public IComponentElement[] getComponents(String fullyQualifiedClassName) {
		if (StringUtil.isEmpty(fullyQualifiedClassName)) {
			return EMPTY_COMPONENTS;
		}
		if (componentAutos.isEmpty() || componentTargetAutos.isEmpty()) {
			return EMPTY_COMPONENTS;
		}
		Set result = new HashSet();
		for (Iterator it = componentAutos.iterator(); it.hasNext();) {
			IAutoRegisterElement auto = (IAutoRegisterElement) it.next();
			if (!auto.isApplied(fullyQualifiedClassName)) {
				continue;
			}
			IComponentElement[] components =
				getTargets(fullyQualifiedClassName, auto.getStartLine());
			if (components.length != 0) {
				result.add(auto);
				result.addAll(Arrays.asList(components));
			}
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}
	
	private IComponentElement[] getTargets(String fullyQualifiedClassName, int lineNumber) {
		Set result = new HashSet();
		for (Iterator it = componentTargetAutos.values().iterator(); it.hasNext();) {
			Set autoRegisters = (Set) it.next();
			IComponentElement[] components =
				getTargetComponents(autoRegisters, fullyQualifiedClassName, lineNumber);
			if (components.length == 0) {
				return EMPTY_COMPONENTS;
			}
			result.addAll(Arrays.asList(components));
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}
	
	private IComponentElement[] getTargetComponents(Set autoRegisters,
			String fullyQualifiedClassName, int lineNumber) {
		Set result = new HashSet();
		for (Iterator it = autoRegisters.iterator(); it.hasNext();) {
			IAutoRegisterElement auto = (IAutoRegisterElement) it.next();
			if (auto.isApplied(fullyQualifiedClassName) &&
					auto.getStartLine() > lineNumber) {
				result.add(auto);
			}
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}

	public IComponentElement[] getAllComponents() {
		Set result = new HashSet(componentAutos);
		for (Iterator it = componentTargetAutos.values().iterator(); it.hasNext();) {
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
		Set temp = new HashSet();
		for (Iterator it = componentAutos.iterator(); it.hasNext();) {
			IAutoRegisterElement auto = (IAutoRegisterElement) it.next();
			temp.addAll(AutoRegisterUtil.getAppliedTypes(auto));
			sift(temp, auto.getStartLine());
			result.addAll(temp);
			temp.clear();
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}
	
	private void sift(Set types, int lineNumber) {
		Set temp = new HashSet(types);
		for (Iterator it = componentTargetAutos.values().iterator(); it.hasNext();) {
			Set autoRegisters = (Set) it.next();
			for (Iterator jt = autoRegisters.iterator(); jt.hasNext();) {
				IAutoRegisterElement auto = (IAutoRegisterElement) jt.next();
				if (lineNumber > auto.getStartLine()) {
					continue;
				}
				for (Iterator kt = types.iterator(); kt.hasNext();) {
					IType type = (IType) kt.next();
					if (auto.isApplied(type)) {
						temp.remove(type);
					}
				}
			}
			types.removeAll(temp);
			temp.clear();
			temp.addAll(types);
		}
	}

	public boolean contains(IType type) {
		return type != null ? contains(type.getFullyQualifiedName()) : false;
	}

	public boolean contains(String fullyQualifiedClassName) {
		if (StringUtil.isEmpty(fullyQualifiedClassName)) {
			return false;
		}
		if (componentAutos.isEmpty() || componentTargetAutos.isEmpty()) {
			return false;
		}
		for (Iterator it = componentAutos.iterator(); it.hasNext();) {
			IAutoRegisterElement auto = (IAutoRegisterElement) it.next();
			if (!auto.isApplied(fullyQualifiedClassName)) {
				continue;
			}
			if (containsTarget(fullyQualifiedClassName, auto.getStartLine())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsTarget(String fullyQualifiedClassName,  int lineNumber) {
		for (Iterator it = componentTargetAutos.values().iterator(); it.hasNext();) {
			Set autoRegisters = (Set) it.next();
			if (!containsComponentTarget(autoRegisters, fullyQualifiedClassName, lineNumber)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean containsComponentTarget(Set autoRegisters, String fullyQualifiedClassName, int lineNumber) {
		for (Iterator it = autoRegisters.iterator(); it.hasNext();) {
			IAutoRegisterElement auto = (IAutoRegisterElement) it.next();
			if (auto.isApplied(fullyQualifiedClassName) &&
					lineNumber < auto.getStartLine()) {
				return true;
			}
		}
		return false;
	}

	public void addComponent(IComponentElement component) {
		if (!AutoRegisterUtil.isAutoRegister(component)) {
			return;
		}
		int type = ((IAutoRegisterElement) component).getAutoRegisterType();
		if (type == IAutoRegisterElement.TYPE_COMPONENT) {
			componentAutos.add(component);
		} else if (type == IAutoRegisterElement.TYPE_COMPONENT_TARGET) {
			addComponentTarget(component);
		}
	}

	private void addComponentTarget(IComponentElement component) {
		String key = component.getComponentClassName();
		if (componentTargetAutos.containsKey(key)) {
			Set components = (Set) componentTargetAutos.get(key);
			components.add(component);
		} else {
			Set components = new HashSet();
			components.add(component);
			componentTargetAutos.put(key, components);
		}
	}
	
	public void removeComponent(IComponentElement component) {
		if (!AutoRegisterUtil.isAutoRegister(component)) {
			return;
		}
		int type = ((IAutoRegisterElement) component).getAutoRegisterType();
		if (type == IAutoRegisterElement.TYPE_COMPONENT) {
			componentAutos.remove(component);
		} else if (type == IAutoRegisterElement.TYPE_COMPONENT_TARGET) {
			removeComponentTarget(component);
		}
	}
	
	private void removeComponentTarget(IComponentElement component) {
		String key = component.getComponentClassName();
		Set components = (Set) componentTargetAutos.get(key);
		if (components == null) {
			return;
		}
		components.remove(component);
		if (components.isEmpty()) {
			componentTargetAutos.remove(key);
		}
	}

	public void clearCache() {
		componentAutos.clear();
		componentTargetAutos.clear();
	}

	public IComponentCache getComponentCache(IPath containerPath) {
		return null;
	}

}
