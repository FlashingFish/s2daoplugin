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

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.deployment.IVirtualDiconChangeListener;

// FIXME: AffectedComponents‚ðŽg‚¤
public class SequentializedListenerChain implements IVirtualDiconChangeListener {

	private DiconModelManager manager;
	private List listeners = new ArrayList();
	private List addedComponentsList = new LinkedList();
	private List updatedComponentsList = new LinkedList();
	private List removedComponentsList = new LinkedList();
	
	public void setManager(DiconModelManager manager) {
		if (manager == null) {
			return;
		}
		for (int i = 0; i < listeners.size(); i++) {
			((IVirtualDiconChangeListener) listeners.get(i)).setManager(manager);
		}
		this.manager = manager;
	}
	
	public DiconModelManager getManager() {
		return manager;
	}
	
	public void initialize() {
		for (int i = 0; i < listeners.size(); i++) {
			((IVirtualDiconChangeListener) listeners.get(i)).initialize();
		}
	}
	
	public void diconAdded(IComponentElement[] components) {
		addedComponentsList.add(components);
	}

	public void diconUpdated(IComponentElement[] olds, IComponentElement[] youngs) {
		updatedComponentsList.add(new UpdatedContainerPair(olds, youngs));
	}

	public void diconRemoved(IComponentElement[] components) {
		removedComponentsList.add(components);
	}

	public void finishChanged() {
		try {
			for (int i = 0; i < listeners.size(); i++) {
				IVirtualDiconChangeListener listener = (IVirtualDiconChangeListener) listeners.get(i);
				fireAdded(listener);
				fireUpdated(listener);
				fireRemoved(listener);
				listener.finishChanged();
			}
		} finally {
			clearContainers();
		}
	}
	
	public SequentializedListenerChain addListener(IVirtualDiconChangeListener listener) {
		if (listener == null) {
			return this;
		}
		listeners.add(listener);
		return this;
	}
	
	private void fireAdded(IVirtualDiconChangeListener listener) {
		for (int i = 0; i < addedComponentsList.size(); i++) {
			listener.diconAdded((IComponentElement[]) addedComponentsList.get(i));
		}
	}
	
	private void fireUpdated(IVirtualDiconChangeListener listener) {
		for (int i = 0; i < updatedComponentsList.size(); i++) {
			UpdatedContainerPair pair = (UpdatedContainerPair) updatedComponentsList.get(i);
			listener.diconUpdated(pair.getOlds(), pair.getYoungs());
		}
	}
	
	private void fireRemoved(IVirtualDiconChangeListener listener) {
		for (int i = 0; i < removedComponentsList.size(); i++) {
			listener.diconRemoved((IComponentElement[]) removedComponentsList.get(i));
		}
	}
	
	private void clearContainers() {
		addedComponentsList.clear();
		updatedComponentsList.clear();
		removedComponentsList.clear();
	}
	
	
	private static class UpdatedContainerPair {
		
		private IComponentElement[] olds;
		private IComponentElement[] youngs;
		
		public UpdatedContainerPair(IComponentElement[] olds, IComponentElement[] youngs) {
			this.olds = olds;
			this.youngs = youngs;
		}
		
		public IComponentElement[] getOlds() {
			return olds;
		}
		
		public IComponentElement[] getYoungs() {
			return youngs;
		}
		
	}

}
