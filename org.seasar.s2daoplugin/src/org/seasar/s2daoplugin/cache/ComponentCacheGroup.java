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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.cache.model.IAutoRegisterElement;

public class ComponentCacheGroup implements IComponentCache {

	private DiconModelManager manager;
	private List componentCaches = new ArrayList();
	private List autoRegisterCaches = new ArrayList();
	
	public void setManager(DiconModelManager manager) {
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			caches[i].setManager(manager);
		}
		this.manager = manager;
	}
	
	public DiconModelManager getManager() {
		return manager;
	}

	public IComponentElement[] getComponents(IType type) {
		List result = new LinkedList();
		for (int i = 0; i < componentCaches.size(); i++) {
			IComponentCache cache = (IComponentCache) componentCaches.get(i);
			result.addAll(Arrays.asList(cache.getComponents(type)));
		}
		return (IComponentElement[]) result
				.toArray(new IComponentElement[result.size()]);
	}

	public IComponentElement[] getAllComponents() {
		List result = new LinkedList();
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			result.addAll(Arrays.asList(caches[i].getAllComponents()));
		}
		return (IComponentElement[]) result
				.toArray(new IComponentElement[result.size()]);
	}

	public boolean contains(IType type) {
		return containsComponent(type) ? true : containsAutoRegister(type);
	}
	
	public void initialize() {
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			caches[i].initialize();
		}
	}
	
	public void addComponent(IComponentElement component) {
		throw new UnsupportedOperationException();
	}

	public void removeComponent(IComponentElement component) {
		throw new UnsupportedOperationException();
	}

	public void clearCache() {
		componentCaches.clear();
		autoRegisterCaches.clear();
	}

	public void diconAdded(IContainerElement container) {
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			caches[i].diconAdded(container);
		}
	}

	public void diconUpdated(IContainerElement old, IContainerElement young) {
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			caches[i].diconUpdated(old, young);
		}
	}

	public void diconRemoved(IContainerElement container) {
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			caches[i].diconRemoved(container);
		}
	}

	public void finishChanged() {
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			caches[i].finishChanged();
		}
	}
	
	public ComponentCacheGroup addComponentCache(IComponentCache cache) {
		if (cache == null) {
			return this;
		}
		if (cache instanceof ComponentCache) {
			componentCaches.add(cache);
		} else if (cache instanceof AutoRegisterCache) {
			autoRegisterCaches.add(cache);
		}
		return this;
	}
	
	private boolean containsComponent(IType type) {
		for (int i = 0; i < componentCaches.size(); i++) {
			IComponentCache cache = (IComponentCache) componentCaches.get(i);
			if (cache.contains(type)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsAutoRegister(IType type) {
		List autoRegisters = new ArrayList();
		for (int i = 0; i < autoRegisterCaches.size(); i++) {
			IComponentCache cache = (IComponentCache) autoRegisterCaches.get(i);
			if (!cache.contains(type)) {
				return false;
			}
			if (i == 0) {
				autoRegisters.addAll(Arrays.asList(cache.getComponents(type)));
			}
			autoRegisters = getComponentsInSameContainer(autoRegisters, cache.getComponents(type));
			if (autoRegisters.isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	// TODO: ComponentAutoとComponentTargetAutoの順序依存コードを取り除く
	private List getComponentsInSameContainer(List components1, IComponentElement[] components2) {
		List result = new ArrayList();
		for (int i = 0; i < components1.size(); i++) {
			IAutoRegisterElement auto1 = (IAutoRegisterElement) components1.get(i);
			for (int j = 0; j < components2.length; j++) {
				IAutoRegisterElement auto2 = (IAutoRegisterElement) components2[j];
				// S2Containerの仕様 - http://s2container.seasar.org/ja/DIContainer.html#AspectAutoRegister
				// 同一Dicon内で
				if (auto1.getStorage().getFullPath().equals(auto2.getStorage().getFullPath())) {
					// 他のAutoRegisterよりComponentAutoRegisterが先にある場合有効
					if (!(!isComponentAutoRegister(auto1) && isComponentAutoRegister(auto2)) &&
							!(auto1.getStartLine() > auto2.getStartLine())) {
						result.add(auto2);
					}
				}
			}
		}
		return result;
	}
	
	private boolean isComponentAutoRegister(IAutoRegisterElement autoRegister) {
		return autoRegister.getAutoRegisterType() == IAutoRegisterElement.TYPE_COMPONENT;
	}
	
	private IComponentCache[] getAllCaches() {
		List caches = new LinkedList(componentCaches);
		caches.addAll(autoRegisterCaches);
		return (IComponentCache[]) caches
				.toArray(new IComponentCache[caches.size()]);
	}

}
