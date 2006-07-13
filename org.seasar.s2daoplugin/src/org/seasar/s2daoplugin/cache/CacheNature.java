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
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.cache.cache.factory.CacheRegistry;
import org.seasar.s2daoplugin.cache.deployment.DeploymentDiconCache;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentDiconCache;
import org.seasar.s2daoplugin.util.ProjectUtil;

public class CacheNature implements IProjectNature {

	private boolean initialized;
	private IProject project;
	private IRawDiconCache rawDiconCache;
	private IDeploymentDiconCache deploymentDiconCache =
		new DeploymentDiconCache();
	private CacheRegistry cacheRegistry = new CacheRegistry();
	
	private DiconCacheBuilder diconCacheBuilder;
	
	public synchronized static CacheNature getInstance(IProject project) {
		IProjectNature nature = null;
		try {
			nature = ProjectUtil.getNature(project, CacheConstants.ID_CACHE_NATURE);
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
		if (nature instanceof CacheNature) {
			CacheNature cacheNature = (CacheNature) nature;
			cacheNature.initialize();
			return cacheNature;
		}
		return null;
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
	
	public DiconCacheBuilder getDiconCacheBuilder() {
		return diconCacheBuilder;
	}
	
	public IDeploymentDiconCache getDeploymentDiconCache() {
		return deploymentDiconCache;
	}
	
	public CacheRegistry getCacheRegistry() {
		return cacheRegistry;
	}
	
	private void initialize() {
		if (initialized) {
			return;
		}
		initialized = true;
		rawDiconCache = new RawDiconCache();
		rawDiconCache.setProject(getProject());
		rawDiconCache.addDiconChangeListener("deploymentmodel", deploymentDiconCache);
		diconCacheBuilder = new DiconCacheBuilder(rawDiconCache);
		diconCacheBuilder.setProject(getProject());
		diconCacheBuilder.buildCache();
	}

}
