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
import org.seasar.s2daoplugin.cache.builder.AutoRegisterUtil;
import org.seasar.s2daoplugin.cache.builder.ICacheBuilder;
import org.seasar.s2daoplugin.cache.model.IAutoRegisterElement;

public class AutoRegisterCache extends AbstractComponentCache {

	private Set autoRegisters = new HashSet();
	private Map autoRegisterByContainer = new HashMap();
	
	public AutoRegisterCache(ICacheBuilder builder) {
		super(builder);
	}

	public IComponentElement[] getComponents(IType type) {
		if (type == null) {
			return CacheConstants.EMPTY_COMPONENTS;
		}
		Set components = new HashSet();
		for (Iterator it = autoRegisters.iterator(); it.hasNext();) {
			IAutoRegisterElement auto = (IAutoRegisterElement) it.next();
			if (auto.isApplied(type)) {
				components.add(auto);
			}
		}
		return (IComponentElement[]) components
				.toArray(new IComponentElement[components.size()]);
	}
	
	public IComponentElement[] getComponents(IType type, IPath containerPath) {
		if (type == null || containerPath == null) {
			return CacheConstants.EMPTY_COMPONENTS;
		}
		Set components = (Set) autoRegisterByContainer.get(containerPath);
		if (components == null) {
			return CacheConstants.EMPTY_COMPONENTS;
		}
		Set result = new HashSet();
		for (Iterator it = components.iterator(); it.hasNext();) {
			IAutoRegisterElement auto = (IAutoRegisterElement) it.next();
			if (auto.isApplied(type)) {
				result.add(auto);
			}
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}

	public IComponentElement[] getAllComponents() {
		return (IComponentElement[]) autoRegisters
				.toArray(new IComponentElement[autoRegisters.size()]);
	}

	public IPath[] getAllContainerPaths() {
		return (IPath[]) autoRegisterByContainer.keySet().toArray(
				new IPath[autoRegisterByContainer.size()]);
	}
	
	public IType[] getAllAppliedTypes() {
		Set result = new HashSet();
		for (Iterator it = autoRegisters.iterator(); it.hasNext();) {
			IAutoRegisterElement auto = (IAutoRegisterElement) it.next();
			result.addAll(AutoRegisterUtil.getAppliedTypes(auto));
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}
	
	public boolean contains(IType type) {
		if (type == null) {
			return false;
		}
		for (Iterator it = autoRegisterByContainer.keySet().iterator(); it.hasNext();) {
			if (contains(type, (IPath) it.next())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean contains(IType type, IPath containerPath) {
		Set components = (Set) autoRegisterByContainer.get(containerPath);
		if (components == null) {
			return false;
		}
		for (Iterator it = components.iterator(); it.hasNext();) {
			IAutoRegisterElement auto = (IAutoRegisterElement) it.next();
			if (auto.isApplied(type)) {
				return true;
			}
		}
		return false;
	}
	
	public void addComponent(IComponentElement component) {
		if (!(component instanceof IAutoRegisterElement)) {
			return;
		}
		autoRegisters.add(component);
		addComponentByContainer((IAutoRegisterElement) component);
	}

	public void removeComponent(IComponentElement component) {
		if (!(component instanceof IAutoRegisterElement)) {
			return;
		}
		autoRegisters.remove(component);
		removeComponentByContainer((IAutoRegisterElement) component);
	}

	public void clearCache() {
		autoRegisters.clear();
		autoRegisterByContainer.clear();
	}
	
	private void addComponentByContainer(IAutoRegisterElement autoRegister) {
		IPath key = autoRegister.getStorage().getFullPath();
		if (autoRegisterByContainer.containsKey(key)) {
			Set components = (Set) autoRegisterByContainer.get(key);
			components.add(autoRegister);
		} else {
			Set components = new HashSet();
			components.add(autoRegister);
			autoRegisterByContainer.put(key, components);
		}
	}
	
	private void removeComponentByContainer(IAutoRegisterElement autoRegister) {
		IPath key = autoRegister.getStorage().getFullPath();
		Set components = (Set) autoRegisterByContainer.get(key);
		if (components == null) {
			return;
		}
		components.remove(autoRegister);
		if (components.size() == 0) {
			autoRegisterByContainer.remove(key);
		}
	}

}
