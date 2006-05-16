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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;

public abstract class AbstractCacheComposite implements IComponentCache {

	private DiconModelManager manager;
	
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
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			result.addAll(Arrays.asList(caches[i].getComponents(type)));
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}

	public IComponentElement[] getAllComponents() {
		List result = new LinkedList();
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			result.addAll(Arrays.asList(caches[i].getAllComponents()));
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}
	
	public IPath[] getAllContainerPaths() {
		List result = new LinkedList();
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			result.addAll(Arrays.asList(caches[i].getAllContainerPaths()));
		}
		return (IPath[]) result.toArray(new IPath[result.size()]);
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
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			caches[i].clearCache();
		}
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
	
	protected abstract IComponentCache[] getAllCaches();

}
