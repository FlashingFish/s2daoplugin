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

import org.seasar.kijimuna.core.dicon.model.IComponentElement;

public class ComponentCacheBuilder extends AbstractCacheBuilder {

	private IComponentFilter componentFilter;
	
	public ComponentCacheBuilder() {
		this("");
	}
	
	public ComponentCacheBuilder(String targetClassName) {
		this(new String[] {targetClassName});
	}
	
	public ComponentCacheBuilder(String[] targetClassNames) {
		if (targetClassNames == null) {
			throw new IllegalArgumentException();
		}
		componentFilter = new ComponentFilter(targetClassNames);
	}
	
	public void initialize() {
		initializeFilter(componentFilter);
	}
	
	public void build(IComponentElement[] components) {
		IComponentElement[] comp = filtering(components);
		for (int i = 0; i < comp.length; i++) {
			addComponent(comp[i]);
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
		// do nothing
	}
	
	protected void initializeFilter(IComponentFilter filter) {
		filter.setDiconModelManager(getManager());
		filter.initialize();
	}
	
	protected IComponentElement[] filtering(IComponentElement[] components) {
		return componentFilter.filtering(components);
	}

}
