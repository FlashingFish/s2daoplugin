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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.builder.AutoRegisterUtil;
import org.seasar.s2daoplugin.cache.builder.ICacheBuilder;
import org.seasar.s2daoplugin.cache.model.IAutoRegisterElement;
import org.seasar.s2daoplugin.util.StringUtil;

public class AutoRegisterCache extends AbstractComponentCache {

	private Set autoRegisters = new HashSet();
	
	public AutoRegisterCache(ICacheBuilder builder) {
		super(builder);
	}
	
	public IComponentElement[] getComponents(IType type) {
		return EMPTY_COMPONENTS;
	}

	public IComponentElement[] getComponents(String fullyQualifiedClassName) {
		return EMPTY_COMPONENTS;
	}

	public IComponentElement[] getAllComponents() {
		return (IComponentElement[]) autoRegisters
				.toArray(new IComponentElement[autoRegisters.size()]);
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
		boolean first = true;
		for (Iterator it = autoRegisters.iterator(); it.hasNext();) {
			IAutoRegisterElement auto = (IAutoRegisterElement) it.next();
			List types = AutoRegisterUtil.getAppliedTypes(auto);
			if (first) {
				first = false;
				result.addAll(types);
				continue;
			}
			for (int i = 0; i < types.size(); i++) {
				if (result.contains(types.get(i))) {
					temp.add(types.get(i));
				}
			}
			result.clear();
			result.addAll(temp);
			temp.clear();
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}

	public boolean contains(IType type) {
		return type != null ? contains(type.getFullyQualifiedName()) : false;
	}

	public boolean contains(String fullyQualifiedClassName) {
		if (StringUtil.isEmpty(fullyQualifiedClassName)) {
			return false;
		}
		if (autoRegisters.isEmpty()) {
			return false;
		}
		for (Iterator it = autoRegisters.iterator(); it.hasNext();) {
			IAutoRegisterElement auto = (IAutoRegisterElement) it.next();
			if (!auto.isApplied(fullyQualifiedClassName)) {
				return false;
			}
		}
		return true;
	}

	public void addComponent(IComponentElement component) {
		if (!AutoRegisterUtil.isAutoRegister(component)) {
			return;
		}
		autoRegisters.add(component);
	}

	public void removeComponent(IComponentElement component) {
		if (!AutoRegisterUtil.isAutoRegister(component)) {
			return;
		}
		autoRegisters.remove(component);
	}

	public void clearCache() {
		autoRegisters.clear();
	}

	public IComponentCache getComponentCache(IPath containerPath) {
		return null;
	}

}
