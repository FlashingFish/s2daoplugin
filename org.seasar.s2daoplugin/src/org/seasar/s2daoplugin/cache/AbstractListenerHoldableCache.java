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

import org.eclipse.core.resources.IProject;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.util.StringUtil;

public abstract class AbstractListenerHoldableCache implements
		IDiconChangeListenerAware {

	private AffectedContainers affectedContainers = new AffectedContainers();
	private IProject project;
	
	public void setProject(IProject project) {
		this.project = project;
	}
	
	public void addDiconChangeListener(String key, IDiconChangeListener listener) {
		if (StringUtil.isEmpty(key) || listener == null) {
			return;
		}
		assertProjectIsNotNull();
		listener.setProject(project);
		affectedContainers.addListener(key, listener);
		fireInitialEvent(listener);
	}

	public void removeDiconChangeListener(String key) {
		affectedContainers.removeListener(key);
	}

	public boolean hasDiconChangeListener(String key) {
		return affectedContainers.hasListener(key);
	}
	
	protected abstract IContainerElement[] getInitialTargetContainers();
	
	protected void fireInitialEvent(IDiconChangeListener listener) {
		AffectedContainers ac = new AffectedContainers();
		ac.addListener("init", listener);
		IContainerElement[] containers = getInitialTargetContainers();
		for (int i = 0; i < containers.length; i++) {
			ac.addAddedContainer(containers[i]);
		}
		ac.fireEvents();
	}
	
	protected AffectedContainers getAffectedContainers() {
		return affectedContainers;
	}
	
	protected IProject getProject() {
		return project;
	}
	
	protected void assertProjectIsNotNull() {
		if (project == null) {
			throw new IllegalStateException();
		}
	}

}
