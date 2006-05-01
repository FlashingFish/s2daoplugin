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
import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.builder.ICacheBuilder;
import org.seasar.s2daoplugin.cache.model.AutoRegisterElement;

public abstract class AbstractComponentCache implements IComponentCache {

	private static final String[] allAutoRegisters;
	
	static {
		allAutoRegisters = new String[CacheConstants.ASPECT_AUTO_REGISTERS.length + CacheConstants.COMPONENT_AUTO_REGISTERS.length];
		System.arraycopy(CacheConstants.COMPONENT_AUTO_REGISTERS, 0, allAutoRegisters, 0, CacheConstants.COMPONENT_AUTO_REGISTERS.length);
		System.arraycopy(CacheConstants.ASPECT_AUTO_REGISTERS, 0, allAutoRegisters, CacheConstants.COMPONENT_AUTO_REGISTERS.length, CacheConstants.ASPECT_AUTO_REGISTERS.length);
	}
	
	private ICacheBuilder builder;
	private DiconModelManager manager;
	private boolean initialized;
	
	public AbstractComponentCache(ICacheBuilder builder) {
		if (builder == null) {
			throw new IllegalArgumentException();
		}
		builder.setComponentCache(this);
		this.builder = builder;
	}
	
	public void setManager(DiconModelManager manager) {
		if (manager == null) {
			throw new IllegalArgumentException();
		}
		this.manager = manager;
	}
	
	public DiconModelManager getManager() {
		return manager;
	}
	
	public void initialize() {
		if (!initialized) {
			builder.initialize();
			initialized = true;
		}
	}

	public void diconAdded(IContainerElement container) {
		builder.build(wrap(container));
	}

	public void diconUpdated(IContainerElement old, IContainerElement young) {
		builder.clear(wrap(old));
		builder.build(wrap(young));
	}

	public void diconRemoved(IContainerElement container) {
		builder.clear(wrap(container));
	}

	public void finishChanged() {
		builder.finishBuild();
	}
	
	protected IRtti getRtti(IComponentElement component) {
		return component != null ? getRtti(component.getComponentClassName()) : null;
	}
	
	protected IRtti getRtti(String fullyQualifiedClassName) {
		return getManager().getRtti(fullyQualifiedClassName);
	}
	
	// TODO: newÇµÇ»Ç¢Ç≈AutoRegisterUtilÇ…à⁄Ç∑Ç©ÅH
	private IComponentElement[] wrap(IContainerElement container) {
		IComponentElement[] components = DiconUtil.getComponents(container);
		List result = new ArrayList();
		for (int i = 0; i < components.length; i++) {
			IComponentElement component = isAutoRegister(getRtti(components[i])) ?
					new AutoRegisterElement(components[i]) : components[i];
			result.add(component);
		}
		return (IComponentElement[]) result
				.toArray(new IComponentElement[result.size()]);
	}
	
	private boolean isAutoRegister(IRtti rtti) {
		if (rtti == null) {
			return false;
		}
		for (int i = 0; i < allAutoRegisters.length; i++) {
			IRtti r = getRtti(allAutoRegisters[i]);
			if (r != null && rtti.getType().equals(r.getType())) {
				return true;
			}
		}
		return false;
	}

}
