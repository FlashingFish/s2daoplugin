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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;
import org.seasar.kijimuna.core.dicon.DiconNature;
import org.seasar.kijimuna.core.dicon.ModelManager;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.kijimuna.core.project.IProjectRecordChangeListener;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.util.StringUtil;

public class DiconModelManager implements IProjectRecordChangeListener {

	private static final Map managerMap = new WeakHashMap();	// IProject, DiconModelManager
	
	private IProject project;
	private boolean initialized;
	// youngは更新されたdicon、oldは更新されていないdicon
	private Map oldContainerMap1 = new HashMap();				// IContainerElement.storage.fullPath, IContainerElement
	private Map youngContainerMap1 = new HashMap();				// IContainerElement.storage.fullPath, IContainerElement
	private Map oldContainerMap2 = new HashMap();				// IContainerElement.storage.fullPath, IContainerElement
	private Map youngContainerMap2 = new HashMap();				// IContainerElement.storage.fullPath, IContainerElement
	private List listeners = new ArrayList();
	
	private DiconModelManager(IProject project) {
		this.project = project;
		initialize();
	}
	
	public static DiconModelManager getInstance(IProject project) { 
		if (project == null) {
			return null;
		}
		if (DiconNature.getInstance(project) == null) {
			if (managerMap.containsKey(project)) {
				managerMap.remove(project);
			}
			return null;
		}
		if (managerMap.containsKey(project)) {
			return (DiconModelManager) managerMap.get(project);
		} else {
			synchronized (managerMap) {
				if (managerMap.containsKey(project)) {
					return (DiconModelManager) managerMap.get(project);
				}
				DiconModelManager manager = new DiconModelManager(project);
				managerMap.put(project, manager);
				return manager;
			}
		}
	}
	
	public IContainerElement[] getAllContainers() {
		initialize();
		return DiconUtil.toContainerArray(getAllContainerMap().values());
	}
	
	public IContainerElement[] getAffectedContainers() {
		initialize();
		return DiconUtil.toContainerArray(getAffectedContainerMap().values());
	}
	
	public IContainerElement[] getUnaffectedContainers() {
		initialize();
		return DiconUtil.toContainerArray(getUnaffectedContainerMap().values());
	}
	
	public IProject getProject() {
		return project;
	}
	
	public IRtti getRtti(String fullyQualifiedClassName) {
		if (StringUtil.isEmpty(fullyQualifiedClassName)) {
			return null;
		}
		DiconNature nature = DiconNature.getInstance(project);
		return nature != null ?
				nature.getRttiLoader().loadRtti(fullyQualifiedClassName) : null;
	}
	
	public void addDiconChangeListener(IDiconChangeListener listener) {
		if (listener == null) {
			return;
		}
		listener.setManager(this);
		fireInitialEvent(listener);
		listeners.add(listener);
	}
	
	public void removeDiconListener(IDiconChangeListener listener) {
		listeners.remove(listener);
	}
	
	public void finishChanged() {
		DiconNature nature = DiconNature.getInstance(project);
		if (nature != null) {
			buildContainerMap(nature.getModel());
		}
	}
	
	private void initialize() {
		if (initialized) {
			return;
		}
		DiconNature nature = DiconNature.getInstance(project);
		if (nature == null) {
			return;
		}
		ModelManager model = nature.getModel();
		buildContainerMap(model);
		model.addRecordChangeListener(this);
		initialized = true;
	}
	
	private void clearContainerMap() {
		oldContainerMap1.clear();
		youngContainerMap1.clear();
		oldContainerMap2.clear();
		youngContainerMap2.clear();
	}
	
	private void buildContainerMap(ModelManager model) {
		IContainerElement[] containers = model.getContainers(null);
		if (oldContainerMap2.isEmpty() && youngContainerMap2.isEmpty()) {
			mashUpContainerMap(containers,
					youngContainerMap1, oldContainerMap1,
					youngContainerMap2, oldContainerMap2);
		} else if (oldContainerMap1.isEmpty() && youngContainerMap1.isEmpty()) {
			mashUpContainerMap(containers,
					youngContainerMap2, oldContainerMap2,
					youngContainerMap1, oldContainerMap1);
		} else {
			clearContainerMap();
			buildContainerMap(model);
		}
		fireFinishChangedAll();
	}
	
	private void mashUpContainerMap(IContainerElement[] containers,
			Map inYoung, Map inOld, Map outYoung, Map outOld) {
		for (int i = 0; i < containers.length; i++) {
			// 既存Diconの更新
			if (containsContainer(inYoung, containers[i])) {
				updateContainerMap(containers[i], inYoung, outYoung, outOld);
			} else if (containsContainer(inOld, containers[i])) {
				updateContainerMap(containers[i], inOld, outYoung, outOld);
			}
			// 新規Diconの追加
			else {
				addContainerMap(outYoung, containers[i]);
			}
		}
		// 存在しなくなったDiconを削除
		removeContainerMap(inYoung);
		removeContainerMap(inOld);
	}
	
	private boolean containsContainer(Map map, IContainerElement container) {
		return map.containsKey(container.getStorage().getFullPath());
	}
	
	private void updateContainerMap(IContainerElement newContainer,
			Map inMap, Map outYoung, Map outOld) {
		IContainerElement oldContainer = getContainer(inMap, newContainer);
		// インスタンスが同一なら更新なし
		if (oldContainer == newContainer) {
			addContainer(outOld, oldContainer);
		}
		// インスタンスが違えば更新あり
		else {
			addContainer(outYoung, newContainer);
			fireUpdatedAll(oldContainer, newContainer);
		}
		removeContainer(inMap, newContainer);
	}
	
	private void addContainerMap(Map map, IContainerElement container) {
		addContainer(map, container);
		fireAddedAll(container);
	}
	
	private void addContainer(Map map, IContainerElement container) {
		map.put(container.getStorage().getFullPath(), container);
	}
	
	private IContainerElement getContainer(Map map, IContainerElement container) {
		return (IContainerElement) map.get(container.getStorage().getFullPath());
	}
	
	private void removeContainer(Map map, IContainerElement container) {
		map.remove(container.getStorage().getFullPath());
	}
	
	private void removeContainerMap(Map map) {
		for (Iterator it = map.values().iterator(); it.hasNext();) {
			IContainerElement container = (IContainerElement) it.next();
			fireRemovedAll(container);
		}
		map.clear();
	}
	
	private void fireInitialEvent(IDiconChangeListener listener) {
		listener.initialize();
		IContainerElement[] containers = getAllContainers();
		for (int i = 0; i < containers.length; i++) {
			fireAdded(listener, containers[i]);
		}
		fireFinishChanged(listener);
	}
	
	private void fireAddedAll(IContainerElement container) {
		for (int i = 0; i < listeners.size(); i++) {
			fireAdded((IDiconChangeListener) listeners.get(i), container);
		}
	}
	
	private void fireAdded(IDiconChangeListener listener, IContainerElement container) {
		listener.diconAdded(container);
	}
	
	private void fireUpdatedAll(IContainerElement old, IContainerElement young) {
		for (int i = 0; i < listeners.size(); i++) {
			fireUpdated((IDiconChangeListener) listeners.get(i), old, young);
		}
	}
	
	private void fireUpdated(IDiconChangeListener listener,
			IContainerElement old, IContainerElement young) {
		listener.diconUpdated(old, young);
	}
	
	private void fireRemovedAll(IContainerElement container) {
		for (int i = 0; i < listeners.size(); i++) {
			fireRemoved((IDiconChangeListener) listeners.get(i), container);
		}
	}
	
	private void fireRemoved(IDiconChangeListener listener, IContainerElement container) {
		listener.diconRemoved(container);
	}
	
	private void fireFinishChangedAll() {
		for (int i = 0 ; i < listeners.size(); i++) {
			fireFinishChanged((IDiconChangeListener) listeners.get(i));
		}
	}
	
	private void fireFinishChanged(IDiconChangeListener listener) {
		listener.finishChanged();
	}
	
	private Map getAllContainerMap() {
		Map currentMap = new HashMap();
		if (youngContainerMap1.isEmpty() && oldContainerMap1.isEmpty()) {
			currentMap.putAll(oldContainerMap2);
			currentMap.putAll(youngContainerMap2);
		} else if (youngContainerMap2.isEmpty() && oldContainerMap2.isEmpty()) {
			currentMap.putAll(oldContainerMap1);
			currentMap.putAll(youngContainerMap1);
		}
		return currentMap;
	}
	
	private Map getAffectedContainerMap() {
		Map affectedMap = new HashMap();
		if (youngContainerMap1.isEmpty() && oldContainerMap1.isEmpty()) {
			affectedMap.putAll(youngContainerMap2);
		} else if (youngContainerMap2.isEmpty() && oldContainerMap2.isEmpty()) {
			affectedMap.putAll(youngContainerMap1);
		}
		return affectedMap;
	}
	
	private Map getUnaffectedContainerMap() {
		Map unaffectedMap = new HashMap();
		if (youngContainerMap1.isEmpty() && oldContainerMap1.isEmpty()) {
			unaffectedMap.putAll(oldContainerMap2);
		} else if (youngContainerMap2.isEmpty() && oldContainerMap2.isEmpty()) {
			unaffectedMap.putAll(oldContainerMap1);
		}
		return unaffectedMap;
	}

}
