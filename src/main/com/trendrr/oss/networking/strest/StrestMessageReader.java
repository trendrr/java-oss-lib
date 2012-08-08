/**
 * 
 */
package com.trendrr.oss.networking.strest;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.json.stream.JSONStreamParser;
import com.trendrr.oss.DynMap;
import com.trendrr.oss.exceptions.TrendrrException;
import com.trendrr.oss.exceptions.TrendrrParseException;
import com.trendrr.oss.networking.ByteReadCallback;
import com.trendrr.oss.networking.SocketChannelWrapper;
import com.trendrr.oss.networking.StringReadCallback;
import com.trendrr.oss.networking.strest.models.StrestResponse;
import com.trendrr.oss.networking.strest.models.json.StrestJsonResponse;


/**
 * @author Dustin Norlander
 * @created Mar 14, 2011
 * 
 */
public class StrestMessageReader implements StringReadCallback {

	protected static Log log = LogFactory.getLog(StrestMessageReader.class);
	
	JSONStreamParser parser = new JSONStreamParser();
	
	AtomicReference<SocketChannelWrapper> socket = new AtomicReference<SocketChannelWrapper>();
	AtomicReference<StrestClient> client = new AtomicReference<StrestClient>();
	
	public void start(StrestClient client, SocketChannelWrapper socket) {
		this.client.set(client);
		this.socket.set(socket);
		this.readMore();
	}
	
	public void stop() {
		this.socket.set(null);
		this.client.set(null);
	}
	
	protected void readMore() {
		SocketChannelWrapper sock = this.socket.get();
		if (sock == null) {
			log.info("No socketchannelwrapper, returning");
		}
		sock.readUntil("}", Charset.forName("utf8"), false, this);
	}
	
	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.ChannelCallback#onError(com.trendrr.oss.exceptions.TrendrrException)
	 */
	@Override
	public void onError(TrendrrException ex) {
		StrestClient client = this.client.get();
		if (client == null)
			return;
		client.error(ex);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.networking.StringReadCallback#stringResult(java.lang.String)
	 */
	@Override
	public void stringResult(String result) {
		List<DynMap> val;
		try {
			val = this.parser.addString(result);
//			DynMap tmp = this.parser.addChar('}'); //need to add the delimiter back in
//			if (tmp != null) 
//				val.add(tmp);
			for (DynMap mp : val) {
				this.client.get().incoming(new StrestJsonResponse(mp));
			}
		} catch (TrendrrParseException e) {
			this.onError(e);
		}

		this.readMore();
	}
}
