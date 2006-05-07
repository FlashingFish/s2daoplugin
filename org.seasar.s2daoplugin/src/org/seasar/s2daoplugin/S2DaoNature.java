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

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.seasar.s2daoplugin.util.ArrayUtil;

public class S2DaoNature implements IProjectNature {

	private IProject project;
	
	public void configure() throws CoreException {
		addBuilder(S2DaoConstants.SQL_MARKER_BUILDER);
	}

	public void deconfigure() throws CoreException {
		removeBuilder(S2DaoConstants.SQL_MARKER_BUILDER);
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	
	private void addBuilder(String builder) throws CoreException {
		IProjectDescription desc = getProject().getDescription();
		ICommand[] commands = desc.getBuildSpec();
		if (getBuilder(commands, builder) != null) {
			return;
		}
		ICommand command = desc.newCommand();
		command.setBuilderName(builder);
		ICommand[] newCommands = (ICommand[]) ArrayUtil.add(commands, command);
		desc.setBuildSpec(newCommands);
		getProject().setDescription(desc, null);
	}
	
	private void removeBuilder(String builder) throws CoreException {
		IProjectDescription desc = getProject().getDescription();
		ICommand[] commands = desc.getBuildSpec();
		ICommand command = getBuilder(commands, builder);
		if (command != null) {
			ICommand[] newCommands = (ICommand[]) ArrayUtil.remove(commands, command);
			desc.setBuildSpec(newCommands);
			getProject().setDescription(desc, null);
		}
	}
	
	private ICommand getBuilder(ICommand[] commands, String builder) {
		for (int i = 0; i < commands.length; i++) {
			if (builder.equals(commands[i].getBuilderName())) {
				return commands[i];
			}
		}
		return null;
	}

}
