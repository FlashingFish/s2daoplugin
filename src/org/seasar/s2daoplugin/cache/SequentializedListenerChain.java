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

import org.seasar.kijimuna.core.dicon.model.IContainerElement;

public class SequentializedListenerChain implements IDiconChangeListener {

	private DiconModelManager manager;
	private List listeners = new ArrayList();
	private List addedContainers = new LinkedList();
	private List updatedContainers = new LinkedList();
	private List removedContainers = new LinkedList();
	
	public void setManager(DiconModelManager manager) {
		if (manager == null) {
			return;
		}
		for (int i = 0; i < listeners.size(); i++) {
			((IDiconChangeListener) listeners.get(i)).setManager(manager);
		}
		this.manager = manager;
	}
	
	public DiconModelManager getManager() {
		return manager;
	}
	
	public void startChanged() {
		// do nothing
	}
	
	public void diconAdded(IContainerElement container) {
		if (container == null) {
			return;
		}
		addedContainers.add(container);
	}

	public void diconUpdated(IContainerElement old, IContainerElement young) {
		if (old == null || young == null) {
			return;
		}
		updatedContainers.add(new UpdatedContainerPair(old, young));
	}

	public void diconRemoved(IContainerElement container) {
		if (container == null) {
			return;
		}
		removedContainers.add(container);
	}

	public void finishChanged() {
		try {
			for (int i = 0; i < listeners.size(); i++) {
				IDiconChangeListener listener = (IDiconChangeListener) listeners.get(i);
				listener.startChanged();
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
		for (int i = 0; i < addedContainers.size(); i++) {
			listener.diconAdded((IContainerElement) addedContainers.get(i));
		}
	}
	
	private void fireUpdated(IDiconChangeListener listener) {
		for (int i = 0; i < updatedContainers.size(); i++) {
			UpdatedContainerPair pair = (UpdatedContainerPair) updatedContainers.get(i);
			listener.diconUpdated(pair.getOld(), pair.getYoung());
		}
	}
	
	private void fireRemoved(IDiconChangeListener listener) {
		for (int i = 0; i < removedContainers.size(); i++) {
			listener.diconRemoved((IContainerElement) removedContainers.get(i));
		}
	}
	
	private void clearContainers() {
		addedContainers.clear();
		updatedContainers.clear();
		removedContainers.clear();
	}
	
	
	private static class UpdatedContainerPair {
		
		private IContainerElement old;
		private IContainerElement young;
		
		public UpdatedContainerPair(IContainerElement old, IContainerElement young) {
			this.old = old;
			this.young = young;
		}
		
		public IContainerElement getOld() {
			return old;
		}
		
		public IContainerElement getYoung() {
			return young;
		}
		
	}

}
