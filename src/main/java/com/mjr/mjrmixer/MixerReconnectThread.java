package com.mjr.mjrmixer;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class MixerReconnectThread extends Thread {
	private CopyOnWriteArrayList<MixerBotBase> mixerBotsChat = new CopyOnWriteArrayList<MixerBotBase>();
	private CopyOnWriteArrayList<MixerBotBase> mixerBotsConstell = new CopyOnWriteArrayList<MixerBotBase>();

	private int mixerBotSleepTime;

	public MixerReconnectThread(int mixerBotSleepTime) {
		super("Mixer Framework Reconnect Thread");
		this.mixerBotSleepTime = mixerBotSleepTime;
	}

	@Override
	public void run() {
		while (true) {
			try {
				ExecutorService threadPool = Executors.newCachedThreadPool();
				if (mixerBotsChat.size() != 0) {
					Iterator<MixerBotBase> iterator = mixerBotsChat.iterator();
					int attempt = 0;
					while (iterator.hasNext()) {
						attempt = attempt + 1;
						MixerBotBase bot = iterator.next();
						do {
							if (bot.getLastReconnectCodeChat() != 1008 || (bot.getLastReconnectCodeChat() == 1008 && bot.getLastReconnectTimeChat() + 30000 <= System.currentTimeMillis())) {
								if (bot.isChatConnectionClosed()) {
									Callable<Boolean> callable = () -> {
										bot.reconnectChat();
										return true;
									};
									Future<Boolean> future = threadPool.submit(callable);
									try {
										future.get(30, TimeUnit.SECONDS);
									} catch (TimeoutException e) {
										MixerEventHooks.triggerOnInfoEvent(bot.getChannelName(), bot.getChannelID(), "[Mixer Framework Reconnect] Chat reconnect has timed out for taking to long will skip and retry!");
										if (!bot.isChatConnectionClosed())
											bot.disconnectChat();
									}
								}
								Thread.sleep((mixerBotSleepTime * 1000) * attempt);
								if (!bot.isChatConnectionClosed())
									mixerBotsChat.remove(bot);
							} else
								Thread.sleep(5 * 1000);
						} while (bot.isChatConnectionClosed());
						attempt = 0;
					}
				}
				if (mixerBotsConstell.size() != 0) {
					Iterator<MixerBotBase> iterator = mixerBotsConstell.iterator();
					int attempt = 0;

					while (iterator.hasNext()) {
						attempt = attempt + 1;
						MixerBotBase bot = iterator.next();

						do {
							if (bot.getLastReconnectCodeConstel() != 1008 || (bot.getLastReconnectCodeConstel() == 1008 && bot.getLastReconnectTimeConstel() + 30000 <= System.currentTimeMillis())) {
								if (bot.isConstellationConnectionClosed()) {
									Callable<Boolean> callable = () -> {
										bot.reconnectConstellation();
										return true;
									};
									Future<Boolean> future = threadPool.submit(callable);
									try {
										future.get(30, TimeUnit.SECONDS);
									} catch (TimeoutException e) {
										MixerEventHooks.triggerOnInfoEvent(bot.getChannelName(), bot.getChannelID(), "[Mixer Framework Reconnect] Constellation reconnect has timed out for taking to long will skip and retry!");
										if (!bot.isConstellationConnectionClosed())
											bot.disconnectConstellation();
									}
								}
								Thread.sleep((mixerBotSleepTime * 1000) * attempt);
								if (!bot.isConstellationConnectionClosed())
									mixerBotsConstell.remove(bot);
							} else
								Thread.sleep(5 * 1000);
						} while (bot.isConstellationConnectionClosed());
						attempt = 0;
					}
				}

				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					MixerEventHooks.triggerOnErrorEvent("Mixer Framework: ReconnectThread Errored", e);

				}
			} catch (Exception e) {
				MixerEventHooks.triggerOnErrorEvent("Mixer Framework: ReconnectThread Errored", e);
			}
		}

	}

	public List<MixerBotBase> getMixerBotChatBases() {
		return mixerBotsChat;
	}

	public List<MixerBotBase> getMixerBotConstellationBases() {
		return mixerBotsConstell;
	}

	public void addMixerBotChatBase(MixerBotBase bot) {
		if (!mixerBotsChat.contains(bot))
			this.mixerBotsChat.add(bot);
	}

	public void addMixerBotChatConstellation(MixerBotBase bot) {
		if (!mixerBotsConstell.contains(bot))
			this.mixerBotsConstell.add(bot);
	}

	public int getMixerBotBaseSleepTime() {
		return mixerBotSleepTime;
	}
}
