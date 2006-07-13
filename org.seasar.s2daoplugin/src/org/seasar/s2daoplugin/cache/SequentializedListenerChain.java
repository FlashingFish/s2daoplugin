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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;

public class SequentializedListenerChain implements IDiconChangeListener {

	private List listeners = new ArrayList();
	private List addedContainerList = new LinkedList();
	private List updatedContainerList = new LinkedList();
	private List removedContainerList = new LinkedList();
	
	public void setProject(IProject project) {
		for (int i = 0; i < listeners.size(); i++) {
			((IDiconChangeListener) listeners.get(i)).setProject(project);
		}
	}
	
	public void initialize() {
		for (int i = 0; i < listeners.size(); i++) {
			((IDiconChangeListener) listeners.get(i)).initialize();
		}
	}
	
	public void diconAdded(IContainerElement container) {
		addedContainerList.add(container);
	}

	public void diconUpdated(IContainerElement old, IContainerElement young) {
		updatedContainerList.add(new UpdatedContainerPair(old, young));
	}

	public void diconRemoved(IContainerElement container) {
		removedContainerList.add(container);
	}

	public void finishChanged() {
		try {
			for (int i = 0; i < listeners.size(); i++) {
				IDiconChangeListener listener = (IDiconChangeListener) listeners.get(i);
				fireAdded(listener);
				fireUpdated(listener);
				fireRemoved(listener);
				listener.finishChanged();
			}
		} finally {
			clearContainers();
		}
	}
	
	public SequentializedListenerChain addListener(IDiconChangeListener listener) {
		if (listener == null) {
			return this;
		}
		listeners.add(listener);
		return this;
	}
	
	private void fireAdded(IDiconChangeListener listener) {
		for (int i = 0; i < addedContainerList.size(); i++) {
			listener.diconAdded((IContainerElement) addedContainerList.get(i));
		}
	}
	
	private void fireUpdated(IDiconChangeListener listener) {
		for (int i = 0; i < updatedContainerList.size(); i++) {
			UpdatedContainerPair pair = (UpdatedContainerPair) updatedContainerList.get(i);
			listener.diconUpdated(pair.old, pair.young);
		}
	}
	
	private void fireRemoved(IDiconChangeListener listener) {
		for (int i = 0; i < removedContainerList.size(); i++) {
			listener.diconRemoved((IContainerElement) removedContainerList.get(i));
		}
	}
	
	private void clearContainers() {
		addedContainerList.clear();
		updatedContainerList.clear();
		removedContainerList.clear();
	}
	
	
	private static class UpdatedContainerPair {
		
		public final IContainerElement old;
		public final IContainerElement young;
		
		public UpdatedContainerPair(IContainerElement old, IContainerElement young) {
			this.old = old;
			this.young = young;
		}
	}

}
