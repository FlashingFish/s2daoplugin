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

import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.S2DaoUtil;
import org.seasar.s2daoplugin.cache.cache.IComponentCache;

public class SqlMarkerUnmarkingListener extends AbstractSqlMarkerListener {

	public void diconAdded(IComponentElement[] components) {
	}

	public void diconUpdated(IComponentElement[] olds, IComponentElement[] youngs) {
		unmark(olds);
	}

	public void diconRemoved(IComponentElement[] components) {
		unmark(components);
	}
	
	private void unmark(IComponentElement[] components) {
		IType[] types = getAppliedTypes(components);
		IComponentCache cache = S2DaoUtil.getS2DaoComponentCache(getProject());
		if (cache == null) {
			return;
		}
		for (int i = 0; i < types.length; i++) {
			if (cache.getComponents(types[i]).length == 1) {
				getMarker().unmark(types[i]);
			}
		}
	}

}
