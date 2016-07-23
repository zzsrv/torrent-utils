/*
 * File    : TOTorrentFileImpl.java
 * Created : 5 Oct. 2003
 * By      : Parg 
 * 
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package cc.vcode.torrent.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import cc.vcode.torrent.TOTorrentException;
import cc.vcode.torrent.TOTorrentFile;
import cc.vcode.util.Constants;

public class TOTorrentFileImpl implements TOTorrentFile
{
	protected long file_length;

	protected byte[][] path_components;

	protected Map<String, Object> additional_properties = new HashMap<String, Object>();



	protected TOTorrentFileImpl(long _len, String _path)

	throws TOTorrentException
	{
		try
		{

			file_length = _len;

			Vector<byte[]> temp = new Vector<byte[]>();

			int pos = 0;

			while (true)
			{

				int p1 = _path.indexOf(File.separator, pos);

				if (p1 == -1)
				{

					temp.add(_path.substring(pos).getBytes(Constants.DEFAULT_ENCODING));

					break;
				}

				temp.add(_path.substring(pos, p1).getBytes(Constants.DEFAULT_ENCODING));

				pos = p1 + 1;
			}

			path_components = new byte[temp.size()][];

			temp.copyInto(path_components);

		} catch (UnsupportedEncodingException e)
		{

			throw (new TOTorrentException("TOTorrentFile: unsupported encoding for '" + _path + "'", TOTorrentException.RT_UNSUPPORTED_ENCODING));
		}
	}



	protected TOTorrentFileImpl(long _len, byte[][] _path_components)
	{
		file_length = _len;
		path_components = _path_components;
	}



	public long getLength()
	{
		return (file_length);
	}



	public byte[][] getPathComponents()
	{
		return (path_components);
	}



	protected void setAdditionalProperty(String name, Object value)
	{
		additional_properties.put(name, value);
	}



	protected Map<String, Object> getAdditionalProperties()
	{
		return (additional_properties);
	}
}
