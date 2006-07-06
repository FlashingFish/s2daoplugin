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
package org.seasar.s2daoplugin.cache.cache.builder;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.cache.filter.ClassNameFilter;
import org.seasar.s2daoplugin.cache.cache.filter.IComponentFilter;

public class ComponentCacheBuilder extends AbstractCacheBuilder {

	public ComponentCacheBuilder(String className) {
		this(new String[] {className});
	}
	
	public ComponentCacheBuilder(String[] classNames) {
		this(new ClassNameFilter(classNames));
	}
	
	public ComponentCacheBuilder(IComponentFilter filter) {
		super(filter);
	}
	
	public void build(IComponentElement[] components) {
		for (int i = 0; i < components.length; i++) {
			if (getFilter().isPassable(components[i])) {
				addComponent(components[i]);
			}
		}
	}
	
	public void finishBuild() {
		// do nothing
	}

}
