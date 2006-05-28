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
package org.seasar.s2daoplugin.cache.builder.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;

public class ClassNameFilter extends AbstractComponentFilter {

	private Set classNameSet;
	
	public ClassNameFilter(String className) {
		this(new String[] {className});
	}
	
	public ClassNameFilter(String[] classNames) {
		if (classNames == null) {
			throw new IllegalArgumentException();
		}
		classNameSet = new HashSet(Arrays.asList(classNames));
	}
	
	public boolean isPassable(IComponentElement component) {
		String className = component.getComponentClassName();
		if (!classNameSet.isEmpty() && !classNameSet.contains(className)) {
			return false;
		}
		IRtti rtti = getRtti(className);
		return rtti != null && rtti.getType() != null;
	}

}