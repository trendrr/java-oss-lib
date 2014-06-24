/**
 * 
 */
package com.trendrr.oss.networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.oss.exceptions.TrendrrException;


/**
 * @author Dustin Norlander
 * @created Mar 11, 2011
 * 
 */
public class Test {

	protected static Log log = LogFactory.getLog(Test.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SocketChannel channel;
		try {
			channel = SocketChannel.open();
		
			channel.connect(new InetSocketAddress("localhost", 8009));
			
			
			
			SocketChannelWrapper wrapper = new SocketChannelWrapper(channel);
			
			for (int i=0; i < 10; i++) {
				DynMap mp = new DynMap();
				mp.put("key1", "something");
				mp.put("index", i);
				mp.putWithDot("test1.test2.key", "300");

				mp.putWithDot("test1.test2.key22", "32200");
				
				String str = mp.toJSONString() + mp.toJSONString() + mp.toJSONString();
				wrapper.write(str.getBytes("utf8"));
				System.out.println(mp.toJSONString());

			}
			
			Sleep.seconds(10);
			
//			wrapper.write("GET /hello/jerk HTTP/1.0\r\n\r\n".getBytes());
//		
//			wrapper.readUntil("\r\n\r\n", Charset.forName("utf8"), new StringReadCallback(){
//				@Override
//				public void onError(TrendrrException ex) {
//					ex.printStackTrace();
//				}
//
//				@Override
//				public void stringResult(String result) {
//					System.out.println("RESULT: " + result);
//				}
//				
//			});
//			
//			byte[] bytes = wrapper.readBytes(197);
//			System.out.println("GOT BYTES! : " + bytes.length);
//			System.out.println(new String(bytes));
//			
//			wrapper.write("GET /echo HTTP/1.0\r\n\r\n".getBytes());
//			System.out.println(wrapper.readUntil("\r\n\r\n", Charset.forName("utf8")));
//			
//			System.out.println(wrapper.readUntil("PARAMS", Charset.forName("utf8")));
//			
			
			
//			
//			
//			wrapper.readBytes(197, new ByteReadCallback() {
//				@Override
//				public void onError(TrendrrException ex) {
//					// TODO Auto-generated method stub
//				}
//				
//				@Override
//				public void byteResult(byte[] result) {
//					System.out.println("GOT BYTES! : " + result.length);
//					System.out.println(new String(result));
////					log.info(new String(result, Charset.forName("utf8")));
////					for (byte b : result) {
////						System.out.println(b);
////					}
//				}
//			});						
//			Sleep.seconds(20);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
