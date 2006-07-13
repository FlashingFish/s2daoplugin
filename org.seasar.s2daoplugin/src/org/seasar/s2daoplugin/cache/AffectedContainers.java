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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.seasar.kijimuna.core.dicon.model.IContainerElement;

public class AffectedContainers {

	private Map listeners = new HashMap();
	private List containers = new LinkedList(); 
	
	public boolean hasListener(String key) {
		return listeners.containsKey(key);
	}
	
	public void addListener(String key, IDiconChangeListener listener) {
		listeners.put(key, listener);
	}
	
	public void removeListener(String key) {
		listeners.remove(key);
	}
	
	public void addAddedContainer(final IContainerElement container) {
		containers.add(new EventFirer() {
			public void process(IDiconChangeListener listener) {
				listener.diconAdded(container);
			}
		});
	}
	
	public void addUpdatedContainer(final IContainerElement old,
			final IContainerElement young) {
		containers.add(new EventFirer() {
			public void process(IDiconChangeListener listener) {
				listener.diconUpdated(old, young);
			}
		});
	}
	
	public void addRemovedContainer(final IContainerElement container) {
		containers.add(new EventFirer() {
			public void process(IDiconChangeListener listener) {
				listener.diconRemoved(container);
			}
		});
	}
	
	public void clearContainers() {
		containers.clear();
	}
	
	public void fireEvents() {
		try {
			for (int i = 0; i < containers.size(); i++) {
				((EventFirer) containers.get(i)).fire();
			}
			if (containers.size() > 0) {
				for (Iterator it = listeners.values().iterator(); it.hasNext();) {
					((IDiconChangeListener) it.next()).finishChanged();
				}
			}
		} finally {
			clearContainers();
		}
	}
	
	
	private abstract class EventFirer {
		
		public void fire() {
			for (Iterator it = listeners.values().iterator(); it.hasNext();) {
				process((IDiconChangeListener) it.next());
			}
		}
		
		protected abstract void process(IDiconChangeListener listener);
	}

}
