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

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.S2DaoPlugin;

// TODO: リスナ別イベントに
public class AffectedContainers {

	private Map listeners = new HashMap();
	private List events = new LinkedList(); 
	
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
		events.add(new EventFirer() {
			public void process(IDiconChangeListener listener) {
				listener.diconAdded(container);
			}
		});
	}
	
	public void addUpdatedContainer(final IContainerElement old,
			final IContainerElement young) {
		events.add(new EventFirer() {
			public void process(IDiconChangeListener listener) {
				listener.diconUpdated(old, young);
			}
		});
	}
	
	public void addRemovedContainer(final IContainerElement container) {
		events.add(new EventFirer() {
			public void process(IDiconChangeListener listener) {
				listener.diconRemoved(container);
			}
		});
	}
	
	public void clearContainers() {
		events.clear();
	}
	
	public void fireEvents() {
		try {
			for (int i = 0; i < events.size(); i++) {
				((EventFirer) events.get(i)).fire();
			}
			if (events.isEmpty()) {
				return;
			}
			for (Iterator it = listeners.values().iterator(); it.hasNext();) {
				final IDiconChangeListener listener = (IDiconChangeListener) it.next();
				run(new ISafeRunnable() {
					public void handleException(Throwable t) {
						AffectedContainers.this.handleException(t, listener);
					}
					public void run() throws Exception {
						listener.finishChanged();
					}
				});
			}
		} finally {
			clearContainers();
		}
	}
	
	private void handleException(Throwable t, IDiconChangeListener listener) {
		S2DaoPlugin.log(t);
		if (listener instanceof IExceptionHandler) {
			try {
				((IExceptionHandler) listener).handleException(t);
			} catch (Throwable ignore) {
			}
		}
	}
	
	private void run(ISafeRunnable runnable) {
		Platform.run(runnable);
	}
	
	
	private abstract class EventFirer {
		
		public void fire() {
			for (Iterator it = listeners.values().iterator(); it.hasNext();) {
				final IDiconChangeListener listener =
					(IDiconChangeListener) it.next();
				run(new ISafeRunnable() {
					public void handleException(Throwable t) {
						AffectedContainers.this.handleException(t, listener);
					}
					public void run() throws Exception {
						process(listener);
					}
				});
			}
		}
		
		protected abstract void process(IDiconChangeListener listener);
	}

}
