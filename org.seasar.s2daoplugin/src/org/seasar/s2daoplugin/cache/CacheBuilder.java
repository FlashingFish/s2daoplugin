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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.s2daoplugin.util.JavaUtil;

public class CacheBuilder extends IncrementalProjectBuilder {

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		CacheNature nature = CacheNature.getInstance(getProject());
		if (nature != null) {
			nature.getDiconModelManager().buildModel();
			IResourceDelta delta = getDelta(getProject());
			if (delta != null) {
				try {
					delta.accept(new CacheDeltaVisitor(nature.getDeploymentModelRegistry()));
				} catch (StopVisitingException ignore) {
				}
			}
		}
		return null;
	}
	
	
	private static class CacheDeltaVisitor implements IResourceDeltaVisitor {

		private ITypeChangeListener listener;

		public CacheDeltaVisitor(ITypeChangeListener listener) {
			this.listener = listener;
		}
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			if (delta.getKind() == IResourceDelta.ADDED ||
					delta.getKind() == IResourceDelta.REMOVED) {
				return process(delta);
			}
			return true;
		}
		
		private boolean process(IResourceDelta delta) throws JavaModelException {
			if (!JavaUtil.isClassFile(delta.getResource())) {
				return true;
			}
			// TODO: 内部クラスと匿名クラスを対象外にしたい
			listener.typeChanged();
			throw new StopVisitingException();
		}
	}
	
	private static class StopVisitingException extends RuntimeException {
	}

}
