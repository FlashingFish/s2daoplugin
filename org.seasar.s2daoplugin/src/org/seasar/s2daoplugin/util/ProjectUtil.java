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
package org.seasar.s2daoplugin.util;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class ProjectUtil {

	public static void addNature(IProject project, String natureId) throws CoreException {
		if (project == null || project.hasNature(natureId)) {
			return;
		}
		IProjectDescription desc = project.getDescription();
		String[] newIds = (String[]) ArrayUtil.add(desc.getNatureIds(), natureId);
		desc.setNatureIds(newIds);
		project.setDescription(desc, null);
	}
	
	public static void removeNature(IProject project, String natureId) throws CoreException {
		if (project == null) {
			return;
		}
		IProjectDescription desc = project.getDescription();
		String[] ids = desc.getNatureIds();
		if (ArrayUtil.contains(ids, natureId)) {
			String[] newIds = (String[]) ArrayUtil.remove(ids, natureId);
			desc.setNatureIds(newIds);
			project.setDescription(desc, null);
		}
	}
	
	public static void addBuilder(IProject project, String builderId) throws CoreException {
		if (project == null || StringUtil.isEmpty(builderId)) {
			return;
		}
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		if (getBuilder(commands, builderId) != null) {
			return;
		}
		ICommand command = desc.newCommand();
		command.setBuilderName(builderId);
		ICommand[] newCommands = (ICommand[]) ArrayUtil.add(commands, command);
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
	}
	
	public static void removeBuilder(IProject project, String builderId) throws CoreException {
		if (project == null || StringUtil.isEmpty(builderId)) {
			return;
		}
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		ICommand command = getBuilder(commands, builderId);
		if (command != null) {
			ICommand[] newCommands = (ICommand[]) ArrayUtil.remove(commands, command);
			desc.setBuildSpec(newCommands);
			project.setDescription(desc, null);
		}
	}
	
	public static IProjectNature getNature(IProject project, String natureId) throws CoreException {
		if (project == null || StringUtil.isEmpty(natureId)) {
			return null;
		}
		return project.hasNature(natureId) ? project.getNature(natureId) : null;
	}
	
	private static ICommand getBuilder(ICommand[] commands, String builderId) {
		for (int i = 0; i < commands.length; i++) {
			if (builderId.equals(commands[i].getBuilderName())) {
				return commands[i];
			}
		}
		return null;
	}

}
