package bq_standard.client.gui;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import bq_standard.core.BQS_Settings;
import bq_standard.core.BQ_Standard;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class UpdateNotification
{
	boolean hasChecked = false;
	@SuppressWarnings("unused")
	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(!BQ_Standard.proxy.isClient() || hasChecked)
		{
			return;
		}
		
		hasChecked = true;
		
		if(BQ_Standard.VERSION == "BQS_VER_" + "KEY")
		{
			event.player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "THIS COPY OF " + BQ_Standard.NAME.toUpperCase() + " IS NOT FOR PUBLIC USE!"));
			return;
		}
		
		try
		{
			String[] data = getNotification("http://bit.ly/224cJ4H", true);
			
			if(BQS_Settings.hideUpdates)
			{
				return;
			}
			
			String version = data[0].trim();
			String link = data[1].trim();
			
			int verStat = compareVersions(BQ_Standard.VERSION, version);
			
			if(verStat == -1)
			{
				event.player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Update " + version + " of " + BQ_Standard.NAME + " available!"));
				event.player.addChatMessage(new ChatComponentText("Download:"));
				event.player.addChatMessage(new ChatComponentText("" + EnumChatFormatting.BLUE + EnumChatFormatting.UNDERLINE + link));
				
				for(int i = 2; i < data.length; i++)
				{
					if(i > 5)
					{
						event.player.addChatMessage(new ChatComponentText("and " + (data.length - 6) + " more..."));
						break;
					} else
					{
						event.player.addChatMessage(new ChatComponentText(data[i].trim()));
					}
				}
			} else if(verStat == 0)
			{
				event.player.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + BQ_Standard.NAME + " " + BQ_Standard.VERSION + " is up to date"));
			} else if(verStat == 1)
			{
				event.player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + BQ_Standard.NAME + " " + BQ_Standard.VERSION + " is a debug build"));
			} else if(verStat == -2)
			{
				event.player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "An error has occured while checking " + BQ_Standard.NAME + " version!"));
			}
			
		} catch(Exception e)
		{
			event.player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "An error has occured while checking " + BQ_Standard.NAME + " version!"));
			e.printStackTrace();
			return;
		}
	}
	
	public int compareVersions(String ver1, String ver2)
	{
		int[] oldNum;
		int[] newNum;
		String[] oldNumString;
		String[] newNumString;
		
		try
		{
			oldNumString = ver1.split("\\.");
			newNumString = ver2.split("\\.");
			
			oldNum = new int[]{Integer.valueOf(oldNumString[0]), Integer.valueOf(oldNumString[1]), Integer.valueOf(oldNumString[2])};
			newNum = new int[]{Integer.valueOf(newNumString[0]), Integer.valueOf(newNumString[1]), Integer.valueOf(newNumString[2])};
		} catch(Exception e)
		{
			return -2;
		}
		
		for(int i = 0; i < 3; i++)
		{
			if(oldNum[i] < newNum[i])
			{
				return -1; // New version available
			} else if(oldNum[i] > newNum[i])
			{
				return 1; // Debug version ahead of release
			}
		}
		
		return 0;
	}
	
	public static String[] getNotification(String link, boolean doRedirect) throws Exception
	{
		URL url = new URL(link);
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setDoOutput(false);
		con.setReadTimeout(20000);
		con.setRequestProperty("Connection", "keep-alive");
		
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0");
		((HttpURLConnection)con).setRequestMethod("GET");
		con.setConnectTimeout(5000);
		BufferedInputStream in = new BufferedInputStream(con.getInputStream());
		int responseCode = con.getResponseCode();
		HttpURLConnection.setFollowRedirects(true);
		if(responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_MOVED_PERM)
		{
			System.out.println("Update request returned response code: " + responseCode + " " + con.getResponseMessage());
		} else if(responseCode == HttpURLConnection.HTTP_MOVED_PERM)
		{
			if(doRedirect)
			{
				try
				{
					return getNotification(con.getHeaderField("location"), false);
				} catch(Exception e)
				{
					throw e;
				}
			} else
			{
				throw new Exception();
			}
		}
		StringBuffer buffer = new StringBuffer();
		int chars_read;
		//	int total = 0;
		while((chars_read = in.read()) != -1)
		{
			char g = (char)chars_read;
			buffer.append(g);
		}
		final String page = buffer.toString();
		
		String[] pageSplit = page.split("\\n");
		
		return pageSplit;
	}
}
