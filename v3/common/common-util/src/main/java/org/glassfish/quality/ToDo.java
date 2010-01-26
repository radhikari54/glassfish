/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.quality;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
    Annotation indicating the code needs attention for some reasson
 */
@Retention(RUNTIME) // could be CLASS if desired
@Target({ANNOTATION_TYPE, CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE})
@Documented
public @interface ToDo {
   public enum Priority{
    /** Needs prompt attention, a stop-ship issue */
    CRITICAL,
    /** Needs attention soon, could have important side-effects if not addressed */
    IMPORTANT,
    /** should be fixed, but side-effects are likely minor */
    MINOR,
    /** Use of this value is discouraged, choose one of the above and details() */
    UNKNOWN
   };
   public enum Kind{
    /** Code needs modification. Code means annotations, but not javadoc. */
    CODE,
    
    /** Documentation needed, javadoc or other forms */
    DOCS,
    
    /** Both code and documentation are needed */
    CODE_AND_DOCS,
    };

   /** How important */
   Priority priority() default Priority.UNKNOWN;
   
   /** What kind of activity is required */
   Kind kind() default Kind.CODE;

   /** concise summary of what's required */
   String details() default "unspecified";

   /**
     Optional info to locate responsible party, could be email, name, team, etc
     Could an IDE  insert ${user} when editing?
   */
   String contact() default "";
}

