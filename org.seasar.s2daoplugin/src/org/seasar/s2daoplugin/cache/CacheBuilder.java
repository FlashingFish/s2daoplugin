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
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.seasar.s2daoplugin.util.JavaUtil;

public class CacheBuilder extends IncrementalProjectBuilder {

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		CacheNature nature = CacheNature.getInstance(getProject());
		if (nature != null) {
			nature.getDiconCacheBuilder().buildCache();
			IResourceDelta delta = getDelta(getProject());
			if (delta != null) {
				CacheDeltaVisitor visitor = new CacheDeltaVisitor();
				delta.accept(visitor);
				fireTypeChangeEvent(nature.getDeploymentDiconCache(),
						visitor.getEvents());
			}
		}
		return null;
	}
	
	private void fireTypeChangeEvent(ITypeChangeListener listener,
			ITypeChangeEvent[] events) {
		listener.typeChanged(events);
	}
	
	
	private static class CacheDeltaVisitor implements IResourceDeltaVisitor {

		private List events = new ArrayList();
		
		public ITypeChangeEvent[] getEvents() {
			return (ITypeChangeEvent[]) events.toArray(
					new ITypeChangeEvent[events.size()]);
		}
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			if (JavaUtil.isClassFile(delta.getResource())) {
				process(delta);
			}
			return true;
		}
		
		private void process(IResourceDelta delta) throws JavaModelException {
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				processAdded(delta);
				break;
			case IResourceDelta.REMOVED:
				processRemoved(delta);
				break;
			}
		}
		
		private void processAdded(IResourceDelta delta) {
			IType type = findTypeFromClassFile(delta);
			if (type != null) {
				events.add(new TypeAddedEvent(type));
			}
		}
		
		private void processRemoved(IResourceDelta delta) {
			IClassFile classFile = getClassFile(delta);
			String fqcn = JavaUtil.getFullyQualifiedNameFromClassFile(classFile);
			if (fqcn != null) {
				events.add(new TypeRemovedEvent(fqcn));
			}
		}

		private IType findTypeFromClassFile(IResourceDelta delta) {
			IClassFile classFile = getClassFile(delta);
			if (classFile == null) {
				return null;
			}
			IClassFileReader reader = ToolFactory.createDefaultClassFileReader(
					classFile, IClassFileReader.CLASSFILE_ATTRIBUTES);
			if (reader == null) {
				return null;
			}
			IJavaProject project = classFile.getJavaProject();
			try {
				IType type = project.findType(String.valueOf(reader
						.getClassName()).replace('/', '.').replace('$', '.'));
				return type;
			} catch (JavaModelException e) {
				return null;
			}
		}
		
		private IClassFile getClassFile(IResourceDelta delta) {
			IResource resource = delta.getResource();
			return resource instanceof IFile ? JavaCore.createClassFileFrom(
					(IFile) resource) : null;
		}
	}
	
	private static class TypeAddedEvent implements ITypeChangeEvent {

		private IType type;
		
		public TypeAddedEvent(IType type) {
			this.type = type;
		}
		
		public IType getType() {
			return type;
		}

		public String getFullyQualifiedClassName() {
			return type.getFullyQualifiedName();
		}
		
		public int getEventType() {
			return TYPE_ADDED;
		}
		
	}

	private static class TypeRemovedEvent implements ITypeChangeEvent {

		private String fullyQualifiedClassName;
		
		private TypeRemovedEvent(String fullyQualifiedClassName) {
			this.fullyQualifiedClassName = fullyQualifiedClassName;
		}
		
		public IType getType() {
			return null;
		}

		public String getFullyQualifiedClassName() {
			return fullyQualifiedClassName;
		}
		
		public int getEventType() {
			return TYPE_REMOVED;
		}
	}

}
