/*
 * BeDecoder.java
 *
 * Created on May 30, 2003, 2:44 PM
 */

package cc.vcode.util;

import java.util.*;
import java.io.*;

/**
 * A set of utility methods to decode a bencoded array of byte into a Map. integer are represented as Long, String as byte[], dictionnaries as Map, and list as List.
 * 
 * @author TdC_VgA
 * 
 */
public class BDecoder
{
	/** Creates a new instance of BeDecoder */
	private BDecoder()
	{
	}



	public static Map<?, ?> decode(byte[] data) throws IOException
	{
		return BDecoder.decode(new ByteArrayInputStream(data));
	}



	private static Map<?, ?> decode(ByteArrayInputStream data) throws IOException
	{
		// 因为对于ByteArrayInputStream来说，都是通过字节数组创建的，内部本身就保存了整个字节数组，mark只是标记一下数组下标位置，根本不用担心mark会创建太大的buffer字节数组缓存。
		return (Map<?, ?>) BDecoder.decodeInputStream(data, 0);

	}



	public static Map<?, ?> decode(BufferedInputStream data) throws IOException
	{
		// 调用mark方法会记下当前调用mark方法的时刻，InputStream被读到的位置。
		// 但是mark接口的参数readlimit究竟是干嘛的呢？
		// 我们知道InputStream是不支持mark的。要想支持mark子类必须重写这三个方法，我想说的是不同的实现子类，mark的参数readlimit作用不尽相同。
		// 常用的FileInputStream不支持mark。
		// 1. 对于BufferedInputStream，readlimit表示：InputStream调用mark方法的时刻起，在读取readlimit个字节之前，标记的该位置是有效的。如果读取的字节数大于readlimit，可能标记的位置会失效。
		//
		// 因为BufferedInputStream读取不是一个字节一个字节读取的，是一个字节数组一个字节数组读取的。
		// 例如，readlimit=35，第一次比较的时候buffer.length=0（没开始读）<readlimit
		// 然后buffer数组一次读取48个字节。这时的read方法只会简单的挨个返回buffer数组中的字节，不会做这次比较。直到读到buffer数组最后一个字节（第48个）后，才重新再次比较。这时如果我们读到buffer中第47个字节就reset。mark仍然是有效的。虽然47>35。
		return (Map<?, ?>) BDecoder.decodeInputStream(data, 0);
	}


	/*
	 * 解析重点
	 */
	private static Object decodeInputStream(InputStream bais, int nesting) throws IOException
	{
		if (!bais.markSupported())
		{
			throw new IOException("InputStream must support the mark() method");
		}

		// set a mark
		bais.mark(Integer.MAX_VALUE);

		// read a byte
		int tempByte = bais.read();

		// decide what to do
		switch (tempByte)
		{
			case 'd':
				// create a new dictionary object
				Map<String, Object> tempMap = new HashMap<String, Object>();

				// get the key
				byte[] tempByteArray = null;
				while ((tempByteArray = (byte[]) BDecoder.decodeInputStream(bais, nesting + 1)) != null)
				{
					// decode some more
					Object value = BDecoder.decodeInputStream(bais, nesting + 1);
					// add the value to the map
					tempMap.put(new String(tempByteArray, Constants.BYTE_ENCODING), value);
				}

				if (bais.available() < nesting)
				{

					throw (new IOException("BDecoder: invalid input data, 'e' missing from end of dictionary"));
				}

				// return the map
				return tempMap;

			case 'l':
				// create the list
				List<Object> tempList = new ArrayList<Object>();

				// create the key
				Object tempElement = null;
				while ((tempElement = BDecoder.decodeInputStream(bais, nesting + 1)) != null)
				{
					// add the element
					tempList.add(tempElement);
				}

				if (bais.available() < nesting)
				{

					throw (new IOException("BDecoder: invalid input data, 'e' missing from end of list"));
				}
				// return the list
				return tempList;

			case 'e':
			case -1:
				return null;

			case 'i':
				return new Long(BDecoder.getNumberFromStream(bais, 'e'));

			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				// move back one
				bais.reset();
				// get the string
				return BDecoder.getByteArrayFromStream(bais);

			default:
				throw new IOException("UNKNOWN COMMAND");
		}
	}


	/*
	 * 解析重点
	 */
	private static long getNumberFromStream(InputStream bais, char parseChar) throws IOException
	{
		int length = 0;

		// place a mark
		bais.mark(Integer.MAX_VALUE);

		int tempByte = bais.read();
		while ((tempByte != parseChar) && (tempByte >= 0))
		{
			tempByte = bais.read();
			length++;
		}

		// are we at the end of the stream?
		if (tempByte < 0)
		{
			return -1;
		}

		// reset the mark
		bais.reset();

		// get the length
		byte[] tempArray = new byte[length];
		int count = 0;
		int len = 0;

		// get the string
		while (count != length && (len = bais.read(tempArray, count, length - count)) > 0)
		{
			count += len;
		}

		// jump ahead in the stream to compensate for the :
		bais.skip(1);

		// return the value
		return Long.parseLong(new String(tempArray));
	}


	/*
	 * 解析重点
	 */
	private static byte[] getByteArrayFromStream(InputStream bais) throws IOException
	{
		int length = (int) BDecoder.getNumberFromStream(bais, ':');

		if (length < 0)
		{
			return null;
		}

		byte[] tempArray = new byte[length];
		int count = 0;
		int len = 0;
		// get the string
		while (count != length && (len = bais.read(tempArray, count, length - count)) > 0)
		{
			count += len;
		}

		if (count != tempArray.length)
		{
			throw (new IOException("BDecoder::getByteArrayFromStream: truncated"));
		}

		return tempArray;
	}
}
