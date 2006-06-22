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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;

public class DebugPrintCache implements IComponentCache {

	public void setManager(DiconModelManager manager) {
	}

	public DiconModelManager getManager() {
		return null;
	}

	public void initialize() {
		System.out.println("* initialize");
	}

	public void diconAdded(IComponentElement[] components) {
		System.out.println("-- add start --");
		for (int i = 0; i < components.length; i++) {
			System.out.println(components[i]);
		}
		System.out.println("-- add end --");
	}

	public void diconUpdated(IComponentElement[] olds,
			IComponentElement[] youngs) {
		System.out.println("-- update@old start --");
		for (int i = 0; i < olds.length; i++) {
			System.out.println(olds[i]);
		}
		System.out.println("-- update@old end --");
		System.out.println("-- update@young start --");
		for (int i = 0; i < youngs.length; i++) {
			System.out.println(youngs[i]);
		}
		System.out.println("-- update@young end --");
	}

	public void diconRemoved(IComponentElement[] components) {
		System.out.println("-- remove start --");
		for (int i = 0; i < components.length; i++) {
			System.out.println(components[i]);
		}
		System.out.println("-- remove end --");
	}

	public void finishChanged() {
		System.out.println("* finishChanged");
	}

	public IComponentElement[] getComponents(IType type) {
		return null;
	}

	public IComponentElement[] getComponents(String fullyQualifiedClassName) {
		return null;
	}

	public IComponentElement[] getAllComponents() {
		return null;
	}

	public void setContainerPath(IPath containerPath) {
	}

	public IPath getContainerPath() {
		return null;
	}

	public IType[] getAllAppliedTypes() {
		return null;
	}

	public boolean contains(IType type) {
		return false;
	}

	public boolean contains(String fullyQualifiedClassName) {
		return false;
	}

	public void addComponent(IComponentElement component) {
	}

	public void removeComponent(IComponentElement component) {
	}

	public void clearCache() {
	}

	public IComponentCache getComponentCache(IPath containerPath) {
		return null;
	}

}
