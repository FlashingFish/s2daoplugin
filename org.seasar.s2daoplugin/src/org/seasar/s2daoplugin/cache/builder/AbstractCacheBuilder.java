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
import org.seasar.s2daoplugin.cache.DiconModelManager;
import org.seasar.s2daoplugin.cache.IComponentCache;

public abstract class AbstractCacheBuilder implements ICacheBuilder {

	private IComponentCache cache;
	
	public void setComponentCache(IComponentCache cache) {
		if (cache == null) {
			throw new IllegalArgumentException();
		}
		this.cache = cache;
	}
	
	protected DiconModelManager getManager() {
		return cache.getManager();
	}
	
	protected void addComponent(IComponentElement component) {
		cache.addComponent(component);
	}
	
	protected void removeComponent(IComponentElement component) {
		cache.removeComponent(component);
	}
	
	protected void clearCacheAll() {
		cache.clearCache();
	}

}
