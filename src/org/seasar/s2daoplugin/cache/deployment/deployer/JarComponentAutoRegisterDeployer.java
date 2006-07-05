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
package org.seasar.s2daoplugin.cache.deployment.deployer;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IPropertyElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;
import org.seasar.s2daoplugin.cache.util.DiconUtil;
import org.seasar.s2daoplugin.util.JavaProjectUtil;
import org.seasar.s2daoplugin.util.StringUtil;

public class JarComponentAutoRegisterDeployer extends
		AbstractComponentAutoRegisterDeployer {

	private IRtti referenceClass;
	private Pattern[] jarFilePatterns;
	
	public JarComponentAutoRegisterDeployer(IDeploymentContainer container,
			IComponentElement autoRegister) {
		super(container, autoRegister);
		assertValidComponentName(JAR_COMPONENT_AUTO_REGISTER);
		buildReferenceClass();
		buildJarFileNames();
	}
	
	public void doDeploy() {
		if (referenceClass == null) {
			return;
		}
		IPackageFragmentRoot jar = JavaProjectUtil.findPackageFragmentRoot(
				referenceClass.getType());
		if (!jar.isArchive()) {
			return;
		}
		IContainer base = jar.getResource().getParent();
		IResource[] archives = null;
		try {
			archives = base.members();
		} catch (CoreException e) {
			return;
		}
		IJavaProject project = jar.getJavaProject();
		for (int i = 0; i < archives.length; i++) {
			if (!"jar".equalsIgnoreCase(archives[i].getFileExtension())) {
				continue;
			}
			jar = project.getPackageFragmentRoot(archives[i].getFullPath().toString());
			if (!jar.exists()) {
				continue;
			}
			if (isAppliedJar(jar)) {
				process(jar);
			}
		}
	}
	
	private void buildReferenceClass() {
		IPropertyElement prop = DiconUtil.getProperty(getAutoRegister(), "referenceClass");
		if (prop != null) {
			referenceClass = findRttiReferencedClassField(prop.getBody());
		}
	}
	
	private void buildJarFileNames() {
		IPropertyElement prop = DiconUtil.getProperty(getAutoRegister(), "jarFileNames");
		if (prop == null) {
			return;
		}
		String[] jarFileNames = StringUtil.split(trimQuote(prop.getBody()), ",");
		jarFilePatterns = new Pattern[jarFileNames.length];
		for (int i = 0; i < jarFileNames.length; i++) {
			try {
				jarFilePatterns[i] = Pattern.compile(jarFileNames[i]);
			} catch (PatternSyntaxException ignore) {
			}
		}
	}
	
	private boolean isAppliedJar(IPackageFragmentRoot jar) {
		if (jarFilePatterns == null) {
			return true;
		}
		for (int i = 0; i < jarFilePatterns.length; i++) {
			String jarWithoutExt = removeExtension(jar.getElementName());
			if (jarFilePatterns[i] != null &&
					jarFilePatterns[i].matcher(jarWithoutExt).matches()) {
				return true;
			}
		}
		return false;
	}
	
	private String removeExtension(String filename) {
		int index = filename.lastIndexOf('.');
		return index != -1 ? filename.substring(0, index) : filename;
	}

}
