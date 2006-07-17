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
package org.seasar.s2daoplugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.util.ProjectUtil;

public class S2DaoPlugin extends AbstractUIPlugin implements S2DaoConstants {

	private static S2DaoPlugin plugin;
	
	public S2DaoPlugin() {
		plugin = this;
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}
	
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}
	
	public void addS2DaoNature(IProject project) throws CoreException {
		ProjectUtil.addNature(project, CacheConstants.ID_CACHE_NATURE);
		ProjectUtil.addNature(project, ID_S2DAO_NATURE);
	}
	
	public void removeS2DaoNature(IProject project) throws CoreException {
		ProjectUtil.removeNature(project, CacheConstants.ID_CACHE_NATURE);
		ProjectUtil.removeNature(project, ID_S2DAO_NATURE);
	}
	
	public static S2DaoPlugin getDefault() {
		return plugin;
	}
	
	public static boolean isEnabled(IProject project) throws CoreException {
		return project.hasNature(ID_S2DAO_NATURE);
	}
	
	public static void log(Throwable t) {
		getDefault().getLog().log(new Status(IStatus.ERROR, ID_PLUGIN,
				IStatus.ERROR, t.getMessage(), t));
	}

}
