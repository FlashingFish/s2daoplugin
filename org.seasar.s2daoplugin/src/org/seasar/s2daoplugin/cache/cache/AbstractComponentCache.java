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
package org.seasar.s2daoplugin.cache.cache;

import org.eclipse.core.resources.IProject;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.kijimuna.core.rtti.RttiLoader;
import org.seasar.s2daoplugin.cache.cache.builder.IComponentCacheBuilder;
import org.seasar.s2daoplugin.cache.util.DiconUtil;
import org.seasar.s2daoplugin.cache.util.RttiUtil;

public abstract class AbstractComponentCache implements IComponentCache {

	private IComponentCacheBuilder builder;
	private IProject project;
	private boolean initialized;
	
	public AbstractComponentCache(IComponentCacheBuilder builder) {
		if (builder == null) {
			throw new IllegalArgumentException();
		}
		builder.setComponentCache(this);
		this.builder = builder;
	}
	
	public void setProject(IProject project) {
		this.project = project;
	}
	
	public void initialize() {
		if (!initialized) {
			builder.initialize();
			initialized = true;
		}
	}
	
	public void diconAdded(IContainerElement container) {
		builder.build(DiconUtil.getComponents(container));
	}

	public void diconUpdated(IContainerElement old, IContainerElement young) {
		builder.clear(DiconUtil.getComponents(old));
		builder.build(DiconUtil.getComponents(young));
	}
	
	public void diconRemoved(IContainerElement container) {
		builder.clear(DiconUtil.getComponents(container));
	}

	public void finishChanged() {
		builder.finishBuild();
	}
	
	protected IProject getProject() {
		return project;
	}
	
	protected IRtti getRtti(IComponentElement component) {
		return component != null ? getRtti(component.getComponentClassName()) : null;
	}
	
	protected IRtti getRtti(String fullyQualifiedClassName) {
		RttiLoader loader = RttiUtil.getRttiLoader(getProject());
		return loader != null ? loader.loadRtti(fullyQualifiedClassName) : null;
	}

}
