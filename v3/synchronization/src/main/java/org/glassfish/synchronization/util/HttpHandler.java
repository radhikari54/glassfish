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

package org.glassfish.synchronization.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.glassfish.synchronization.client.ClientHeaderInfo;
import org.glassfish.synchronization.client.SyncContext;

import com.sun.grizzly.tcp.http11.GrizzlyResponse;

/**
 * Sends and receives http messages
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class HttpHandler {
	private static HttpURLConnection syncConn = null;
	private static URL url;
	public static void send(Object req, HttpURLConnection conn) {
		try {
			ObjectOutputStream wr = new ObjectOutputStream(conn
					.getOutputStream());
			wr.writeObject(req);
			wr.flush();
			// wr.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static Object receive(HttpURLConnection conn)
			throws SocketTimeoutException, IOException, ClassNotFoundException {
		Object reply = null;
		ObjectInputStream objIn = null;
		try {
			objIn = new ObjectInputStream(conn.getInputStream());
			reply = objIn.readObject();
			// input stream MUST be closed in order to utilize HTTP persistent
			// connection
			objIn.close();
		} catch (IOException e) {
			int respCode = conn.getResponseCode();
			InputStream es = conn.getErrorStream();
			int ret = 0;
			byte[] buf = new byte[1024];
			// read the response body
			if (es != null) {
				while ((ret = es.read(buf)) > 0) {
				}
				// close the errorstream
				es.close();
			}

		} finally {
			if (objIn != null)
				objIn.close();
		}
		return reply;
	}
	public static void sendGrizzlyResponse(Object reply,
			GrizzlyResponse response) throws IOException {
		ObjectOutputStream wr = new ObjectOutputStream(response
				.getOutputStream());
		wr.writeObject(reply);
		wr.flush();
	}
	/**
	 * Sends status update messages to das. The occur when redirection occurs
	 * or when a synchronization process is completed
	 * @param mesg
	 * @param context
	 */
	public static synchronized void sendUpdate(Object mesg,
			SyncContext context) {
		if (context.getStaticSyncInfo().getDasUrl() == null)
			return;
		try {
			url = new URL(context.getStaticSyncInfo().getDasUrl());
			syncConn = (HttpURLConnection) url.openConnection();
			syncConn.setRequestMethod("POST");
			syncConn.setDoInput(true);
			syncConn.setDoOutput(true);
			syncConn.setRequestProperty(ClientHeaderInfo.SERVER_ADDRESS,
					context.getStaticSyncInfo().getServerAddress());
			syncConn.setRequestProperty(ClientHeaderInfo.SERVICE_LOAD, ""
					+ context.getBalancerLoad());
			syncConn.setRequestProperty(
					ClientHeaderInfo.MANIFEST_VERSION_HEADER, ""
							+ context.getManifestManager().getManVersion());
			syncConn.connect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		HttpHandler.send(mesg, syncConn);
		Object respnse = null;
		try {
			respnse = HttpHandler.receive(syncConn);
		} catch (SocketTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
