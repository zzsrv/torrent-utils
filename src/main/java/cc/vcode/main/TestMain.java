package cc.vcode.main;

import java.io.File;

import cc.vcode.torrent.TOTorrentFactory;
import cc.vcode.torrent.TOTorrent;

public class TestMain
{

	public static void main(String[] args) throws Exception
	{
		File f = new File("/home/ubuntu/vcode.torrent");
		String tarPathString = "VCode";
		TOTorrent torrent = TOTorrentFactory.deserialiseFromBEncodedFile(f, tarPathString);
		torrent.serialiseToBEncodedFile(new File("/home/ubuntu/vcode-target.torrent"));
		System.out.println("success!");
	}
}