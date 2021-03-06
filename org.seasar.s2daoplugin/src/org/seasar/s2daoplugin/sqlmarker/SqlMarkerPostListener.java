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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.S2DaoUtil;
import org.seasar.s2daoplugin.cache.cache.IComponentCache;
import org.seasar.s2daoplugin.cache.util.TypeUtil;

public class SqlMarkerPostListener extends AbstractSqlMarkerListener {

	public SqlMarkerPostListener(SqlMarkerListenerContext context) {
		super(context);
	}
	
	public void diconAdded(IContainerElement container) {
		mark(container);
	}
	
	public void diconUpdated(IContainerElement old, IContainerElement young) {
		unmark();
		mark(young);
	}
	
	public void diconRemoved(IContainerElement container) {
		unmark();
	}
	
	private void mark(IContainerElement container) {
		getMarker().mark(getAllAppliedTypes(container));
	}
	
	private void unmark() {
		IComponentCache cache = S2DaoUtil.getS2DaoComponentCache(getProject());
		if (cache == null) {
			return;
		}
		IType[] types = getContext().getTypes();
		for (int i = 0; i < types.length; i++) {
			if (cache.getComponents(types[i]).length == 0) {
				getMarker().unmark(types[i]);
			} else {
				IMethod[] methods = TypeUtil.getMethods(types[i]);
				for (int j = 0; j < methods.length; j++) {
					if (!S2DaoUtil.isS2DaoInterceptorAppliedMethod(methods[j])) {
						getMarker().unmark(methods[j]);
					}
				}
			}
		}
	}

}
