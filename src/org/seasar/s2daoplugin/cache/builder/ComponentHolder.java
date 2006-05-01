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
package org.seasar.s2daoplugin.cache.builder;

import java.util.HashSet;
import java.util.Set;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.DiconModelManager;

public class ComponentHolder implements IComponentHolder {

	private IComponentFilter filter;
	private DiconModelManager manager;
	private Set targetComponents = new HashSet();
	private boolean targetComponentsChanged;
	
	public ComponentHolder(String targetClassName) {
		this(new String[] {targetClassName});
	}
	
	public ComponentHolder(String[] targetClassNames) {
		filter = new ComponentFilter(targetClassNames);
	}
	
	public void setDiconModelManager(DiconModelManager manager) {
		if (manager == null) {
			throw new IllegalArgumentException();
		}
		this.manager = manager;
	}
	
	public IComponentElement[] getComponents() {
		return (IComponentElement[]) targetComponents
				.toArray(new IComponentElement[targetComponents.size()]);
	}
	
	public boolean isComponentChanged() {
		return targetComponentsChanged;
	}
	
	public boolean hasComponent(IComponentElement component) {
		return targetComponents.contains(component);
	}
	
	public boolean isEmpty() {
		return targetComponents.isEmpty();
	}
	
	public void initialize() {
		if (manager == null) {
			throw new IllegalStateException();
		}
		filter.setDiconModelManager(manager);
		filter.initialize();
	}

	public void build(IComponentElement[] components) {
		IComponentElement[] comps = filter.filtering(components);
		for (int i = 0; i < comps.length; i++) {
			addComponent(comps[i]);
		}
	}

	public void clear(IComponentElement[] components) {
		if (components == null) {
			return;
		}
		for (int i = 0; i < components.length; i++) {
			removeComponent(components[i]);
		}
	}

	public void finishBuild() {
		targetComponentsChanged = false;
	}
	
	private void addComponent(IComponentElement component) {
		targetComponents.add(component);
		targetComponentsChanged = true;
	}
	
	private void removeComponent(IComponentElement component) {
		targetComponents.remove(component);
		targetComponentsChanged = true;
	}

}
