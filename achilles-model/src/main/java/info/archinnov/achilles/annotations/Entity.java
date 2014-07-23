/*
 * Copyright (C) 2012-2014 DuyHai DOAN
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package info.archinnov.achilles.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  <p>
 * Marks a class as an entity
 *
 * <pre class="code"><code class="java">
 *
 *   <strong>{@literal @}Entity</strong>
 *   public class User
 *
 * </code></pre>
 * @see <a href="https://github.com/doanduyhai/Achilles/wiki/Achilles-Annotations#entity" target="_blank">@Entity</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Entity {

	/**
	 * (<strong>Optional</strong>) The name of the table. Defaults to the short class name. <br/>
     * Ex: for the class <em>info.archinnov.achilles.entity.UserEntity</em>, the
     * default table name will be <strong>userentity</strong> if the attribute <em>table</em> is not set.
     * <br/>
     *
     * <strong>Please note that table names by default are case-insensitive in Cassandra</strong>
     *
     * <pre class="code"><code class="java">
     *
     *   package info.archinnov.achilles.entity
     *
     *   <strong>{@literal @}Entity(table = "user")</strong>
     *   public class UserEntity {...}
     *
     * </code></pre>
     *
	 */
	String table() default "";
}
