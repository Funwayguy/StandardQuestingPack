package bq_standard.client.gui;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.Level;
import bq_standard.core.BQS_Settings;
import bq_standard.core.BQ_Standard;

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
		
		if(BQ_Standard.HASH == "CI_MOD_" + "HASH")
		{
			event.player.addChatMessage(new TextComponentString(TextFormatting.RED + "THIS COPY OF " + BQ_Standard.NAME.toUpperCase() + " IS NOT FOR PUBLIC USE!"));
			return;
		}
		
		try
		{
			String[] data = getNotification("https://goo.gl/N4pxwj", true).split("\\n");
			
			if(BQS_Settings.hideUpdates)
			{
				return;
			}
			
			ArrayList<String> changelog = new ArrayList<String>();
			boolean hasLog = false;
			
			for(String s : data)
			{
				if(s.equalsIgnoreCase("git_branch:" + BQ_Standard.BRANCH))
				{
					if(!hasLog)
					{
						hasLog = true;
						changelog.add(s);
						continue;
					} else
					{
						break;
					}
				} else if(s.toLowerCase().startsWith("git_branch:"))
				{
					if(hasLog)
					{
						break;
					} else
					{
						continue;
					}
				} else if(hasLog)
				{
					changelog.add(s);
				}
			}
			
			if(!hasLog || data.length < 2)
			{
				event.player.addChatMessage(new TextComponentString(TextFormatting.RED + "An error has occured while checking " + BQ_Standard.NAME + " version!"));
				BQ_Standard.logger.log(Level.ERROR, "An error has occured while checking " + BQ_Standard.NAME + " version! (hasLog: " + hasLog + ", data: " + data.length + ")");
				return;
			} else
			{
				// Only the relevant portion of the changelog is preserved
				data = changelog.toArray(new String[0]);
			}
			
			String hash = data[1].trim();
			
			boolean hasUpdate = !BQ_Standard.HASH.equalsIgnoreCase(hash);
			
			if(hasUpdate)
			{
				event.player.addChatMessage(new TextComponentString(TextFormatting.RED + "Update for " + BQ_Standard.NAME + " available!"));
				event.player.addChatMessage(new TextComponentString("Download: http://minecraft.curseforge.com/projects/better-questing-standard-expansion"));
				
				for(int i = 2; i < data.length; i++)
				{
					if(i > 5)
					{
						event.player.addChatMessage(new TextComponentString("and " + (data.length - 5) + " more..."));
						break;
					} else
					{
						event.player.addChatMessage(new TextComponentString("- " + data[i].trim()));
					}
				}
			}
			
		} catch(Exception e)
		{
			event.player.addChatMessage(new TextComponentString(TextFormatting.RED + "An error has occured while checking " + BQ_Standard.NAME + " version!"));
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
	
	public static String getNotification(String link, boolean doRedirect) throws Exception
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
		
		return page == null? "" : page;
	}
}
