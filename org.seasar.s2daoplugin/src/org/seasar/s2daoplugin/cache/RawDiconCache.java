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
import java.util.Map;

import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.cache.util.DiconUtil;

public class RawDiconCache extends AbstractListenerHoldableCache
		implements IRawDiconCache {

	// youngは更新されたdicon、oldは更新されていないdicon
	private Map oldContainerMap1 = new HashMap();
	private Map youngContainerMap1 = new HashMap();
	private Map oldContainerMap2 = new HashMap();
	private Map youngContainerMap2 = new HashMap();
	
	public RawDiconCache() {
	}
	
	public void buildModel(IContainerElement[] containers) {
		AffectedContainers ac = getAffectedContainers();
		ac.clearContainers();
		buildContainerMap(containers);
		ac.fireEvents();
	}
	
	public void clearModel() {
		AffectedContainers ac = getAffectedContainers();
		ac.clearContainers();
		IContainerElement[] containers = getAllContainers();
		for (int i = 0; i < containers.length; i++) {
			ac.addRemovedContainer(containers[i]);
		}
		ac.fireEvents();
		clearContainerMap();
	}
	
	protected IContainerElement[] getInitialTargetContainers() {
		return getAllContainers();
	}
	
	protected IContainerElement[] getAllContainers() {
		return DiconUtil.toContainerArray(getAllContainerMap().values());
	}
	
	protected IContainerElement[] getAddedAndUpdatedContainers() {
		return DiconUtil.toContainerArray(getAddedAndUpdatedContainerMap().values());
	}
	
	protected IContainerElement[] getUnaffectedContainers() {
		return DiconUtil.toContainerArray(getUnaffectedContainerMap().values());
	}

	private void clearContainerMap() {
		oldContainerMap1.clear();
		youngContainerMap1.clear();
		oldContainerMap2.clear();
		youngContainerMap2.clear();
	}

	private void buildContainerMap(IContainerElement[] containers) {
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
			buildContainerMap(containers);
		}
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

	private IContainerElement getContainer(Map map, IContainerElement container) {
		return (IContainerElement) map.get(container.getStorage().getFullPath());
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
			getAffectedContainers().addUpdatedContainer(oldContainer, newContainer);
		}
		removeContainer(inMap, newContainer);
	}
	
	private void addContainerMap(Map map, IContainerElement container) {
		addContainer(map, container);
		getAffectedContainers().addAddedContainer(container);
	}
	
	private void addContainer(Map map, IContainerElement container) {
		map.put(container.getStorage().getFullPath(), container);
	}
	
	private void removeContainer(Map map, IContainerElement container) {
		map.remove(container.getStorage().getFullPath());
	}
	
	private void removeContainerMap(Map map) {
		for (Iterator it = map.values().iterator(); it.hasNext();) {
			IContainerElement container = (IContainerElement) it.next();
			getAffectedContainers().addRemovedContainer(container);
		}
		map.clear();
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
	
	private Map getAddedAndUpdatedContainerMap() {
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
