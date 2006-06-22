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
package org.seasar.s2daoplugin.cache.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.DiconModelManager;
import org.seasar.s2daoplugin.cache.IVirtualDiconModelRegistry;
import org.seasar.s2daoplugin.cache.VirtualDiconModelRegistry;
import org.seasar.s2daoplugin.cache.factory.CacheRegistry;
import org.seasar.s2daoplugin.util.ProjectUtil;

public class CacheNature implements IProjectNature {

	private IProject project;
	private DiconModelManager manager;
	private IVirtualDiconModelRegistry virtualModelRegistry =
		new VirtualDiconModelRegistry();
	private CacheRegistry registry = new CacheRegistry();
	
	public static CacheNature getInstance(IProject project) {
		IProjectNature nature = null;
		try {
			nature = ProjectUtil.getNature(project, CacheConstants.ID_CACHE_NATURE);
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
		return nature instanceof CacheNature ? (CacheNature) nature : null;
	}
	
	public void configure() throws CoreException {
		ProjectUtil.addBuilder(getProject(), CacheConstants.ID_CACHE_BUILDER);
	}

	public void deconfigure() throws CoreException {
		ProjectUtil.removeBuilder(getProject(), CacheConstants.ID_CACHE_BUILDER);
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	
	public IVirtualDiconModelRegistry getVirtualDiconModelRegistry() {
		createDiconModelManagerIfNecessary();
		return virtualModelRegistry;
	}
	
	public DiconModelManager getDiconModelManager() {
		createDiconModelManagerIfNecessary();
		return manager;
	}
	
	public CacheRegistry getCacheRegistry() {
		createDiconModelManagerIfNecessary();
		return registry;
	}
	
	private synchronized void createDiconModelManagerIfNecessary() {
		if (manager == null) {
			manager = new DiconModelManager(getProject());
			manager.addDiconChangeListener("virtual", virtualModelRegistry);
		}
	}

}