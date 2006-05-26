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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.model.IAutoRegisterElement;

public class AutoAspectedComponentCacheBuilder extends AbstractCacheBuilder {

	private String[] targetClassNames;
	private Set builtComponents = new HashSet();
	private Set aspectAutoRegisters =
		new HashSet(Arrays.asList(CacheConstants.ASPECT_AUTO_REGISTERS));
	private Set targetAutoRegisters = new HashSet();
	
	public AutoAspectedComponentCacheBuilder(String targetClassName) {
		this(new String[] {targetClassName});
	}
	
	public AutoAspectedComponentCacheBuilder(String[] targetClassNames) {
		if (targetClassNames == null) {
			throw new IllegalArgumentException();
		}
		this.targetClassNames = targetClassNames;
	}
	
	public void initialize() {
		// do nothing
	}

	public void build(IComponentElement[] components) {
		for (int i = 0; i < components.length; i++) {
			IComponentElement comp = components[i];
			if (AutoRegisterUtil.isAutoRegister(comp) &&
					isTargetAutoRegister((IAutoRegisterElement) comp)) {
				targetAutoRegisters.add(comp);
			} else {
				builtComponents.add(comp);
			}
		}
	}

	public void clear(IComponentElement[] components) {
		removeComponents(components);
	}

	public void finishBuild() {
		for (Iterator it = builtComponents.iterator(); it.hasNext();) {
			IComponentElement component = (IComponentElement) it.next();
			for (Iterator jt = targetAutoRegisters.iterator(); jt.hasNext();) {
				IAutoRegisterElement auto = (IAutoRegisterElement) jt.next();
				if (isApplied(auto, component)) {
					addComponent(component);
				}
			}
		}
		builtComponents.clear();
		targetAutoRegisters.clear();
	}
	
	private boolean isTargetAutoRegister(IAutoRegisterElement autoRegister) {
		String className = autoRegister.getComponentClassName();
		IRtti rtti = getManager().getRtti(className);
		if (aspectAutoRegisters.contains(className) &&
				rtti != null && rtti.getType() != null) {
			if (hasTargetInterceptor(autoRegister)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasTargetInterceptor(IAutoRegisterElement autoRegister) {
		for (int i = 0; i < targetClassNames.length; i++) {
			IRtti rtti = getManager().getRtti(targetClassNames[i]);
			if (rtti != null) {
				if (AutoRegisterUtil.hasInterceptor(autoRegister, rtti.getType())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isApplied(IAutoRegisterElement autoRegister, IComponentElement target) {
		IRtti rtti = (IRtti) target.getAdapter(IRtti.class);
		if (rtti == null) {
			return false;
		}
		// S2Containerの仕様 - http://s2container.seasar.org/ja/DIContainer.html#AspectAutoRegister
		// 同一Dicon内でかつ、対象がAutoRegisterより後に出現すること
		if (target.getStartLine() > autoRegister.getStartLine() &&
				autoRegister.isApplied(rtti.getType())) {
			return true;
		}
		return false;
	}

}
