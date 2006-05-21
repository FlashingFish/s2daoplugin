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
package org.seasar.s2daoplugin.sqlmarker;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.S2DaoUtil;
import org.seasar.s2daoplugin.cache.DiconModelManager;
import org.seasar.s2daoplugin.cache.IComponentCache;
import org.seasar.s2daoplugin.cache.IDiconChangeListener;
import org.seasar.s2daoplugin.sqlmarker.SqlMarkerUtil.SqlMarkerCreator;

public abstract class AbstractSqlMarkerListener implements IDiconChangeListener {

	protected static final IType[] EMPTY_TYPES = new IType[0];
	
	private SqlMarkerCreator marker = SqlMarkerUtil.getCreator();
	private DiconModelManager manager;
	
	public void setManager(DiconModelManager manager) {
		this.manager = manager;
	}

	public DiconModelManager getManager() {
		return manager;
	}

	public void initialize() {
	}
	
	public void finishChanged() {
	}
	
	protected IProject getProject() {
		return manager.getProject();
	}
	
	protected SqlMarkerCreator getMarker() {
		return marker;
	}
	
	protected IType[] getAppliedTypes(IContainerElement container) {
		if (container == null) {
			return EMPTY_TYPES;
		}
		IComponentCache cache = S2DaoUtil.getS2DaoComponentCache(getProject());
		if (cache == null) {
			return EMPTY_TYPES;
		}
		return cache.getAppliedTypes(container.getStorage().getFullPath());
	}

}
