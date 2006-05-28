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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.util.StringUtil;

public abstract class AbstractCacheComposite implements IComponentCache {

	private DiconModelManager manager;
	
	public IComponentElement[] getComponents(IType type) {
		return type != null ?
				getComponents(type.getFullyQualifiedName()) : EMPTY_COMPONENTS;
	}

	public IComponentElement[] getComponents(String fullyQualifiedClassName) {
		if (StringUtil.isEmpty(fullyQualifiedClassName)) {
			return EMPTY_COMPONENTS;
		}
		Set result = new HashSet();
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			add(result, caches[i].getComponents(fullyQualifiedClassName));
		}
		return toComponentArray(result);
	}
	
	protected IComponentElement[] toComponentArray(Collection collection) {
		return collection != null ? (IComponentElement[]) collection.toArray(
				new IComponentElement[collection.size()]) : EMPTY_COMPONENTS;
	}
	
	protected void add(Collection collection, Object[] array) {
		if (collection == null || array == null || array.length == 0) {
			return;
		}
		collection.addAll(Arrays.asList(array));
	}

	public IComponentElement[] getAllComponents() {
		Set result = new HashSet();
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			add(result, caches[i].getAllComponents());
		}
		return toComponentArray(result);
	}
	
	public void addComponent(IComponentElement component) {
		throw new UnsupportedOperationException();
	}

	public void removeComponent(IComponentElement component) {
		throw new UnsupportedOperationException();
	}
	
	public void setManager(DiconModelManager manager) {
		if (manager == null) {
			throw new IllegalArgumentException();
		}
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			caches[i].setManager(manager);
		}
		this.manager = manager;
	}

	public DiconModelManager getManager() {
		if (manager == null) {
			throw new IllegalStateException();
		}
		return manager;
	}

	public void initialize() {
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			caches[i].initialize();
		}
	}

	public void diconAdded(IContainerElement container) {
		if (container == null) {
			return;
		}
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			caches[i].diconAdded(container);
		}
	}

	public void diconUpdated(IContainerElement old, IContainerElement young) {
		if (old == null || young == null) {
			return;
		}
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			caches[i].diconUpdated(old, young);
		}
	}

	public void diconRemoved(IContainerElement container) {
		if (container == null) {
			return;
		}
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

	protected abstract IComponentCache[] getAllCaches();

}
