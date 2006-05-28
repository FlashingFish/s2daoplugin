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

import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.DiconModelManager;
import org.seasar.s2daoplugin.cache.builder.IComponentFilter;

public abstract class AbstractComponentFilter implements IComponentFilter {

	private DiconModelManager manager;
	
	public void setManager(DiconModelManager manager) {
		if (manager == null) {
			throw new IllegalArgumentException();
		}
		if (this.manager == null) {
			this.manager = manager;
			onManagerSet();
		}
	}
	
	protected DiconModelManager getManager() {
		if (manager == null) {
			throw new IllegalStateException();
		}
		return manager;
	}
	
	protected void onManagerSet() {
	}
	
	protected IRtti getRtti(String fullyQualifiedClassName) {
		return getManager().getRtti(fullyQualifiedClassName);
	}

}
